package edu.ualberta.med.biobank.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import edu.ualberta.med.biobank.domain.CollectionEvent;
import edu.ualberta.med.biobank.domain.Patient;
import edu.ualberta.med.biobank.dtos.CollectionEventSummaryDTO;
import edu.ualberta.med.biobank.dtos.CommentDTO;
import edu.ualberta.med.biobank.dtos.PatientDTO;
import edu.ualberta.med.biobank.dtos.PatientSummaryDTO;
import edu.ualberta.med.biobank.repositories.CollectionEventCustomRepository;
import edu.ualberta.med.biobank.repositories.CollectionEventRepository;
import edu.ualberta.med.biobank.repositories.PatientCustomRepository;
import edu.ualberta.med.biobank.repositories.PatientRepository;
import io.jbock.util.Either;

@Service
public class PatientService {

    Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    PatientCustomRepository patientCustomRepository;

    @Autowired
    CollectionEventRepository collectionEventRepository;

    @Autowired
    CollectionEventCustomRepository collectionEventCustomRepository;

    public void save(Patient patient) {
        patientRepository.save(patient);
    }

    public Either<String, PatientDTO> findByPnumber(String pnumber) {
        return patientCustomRepository.findByPnumber(pnumber)
                .map(p -> {
                    final var counts = collectionEventCustomRepository.collectionEventSpecimenCounts(p.getId());
                    var collectionEvents = collectionEventRepository.findByPatientId(p.getId()).stream()
                        .map(ce -> toCollectionEventDTO(ce, counts.get(ce.getId())))
                        .toList();
                    return p.withCollectionEvents(collectionEvents);
                });
    }

    public Page<PatientSummaryDTO> patientPagination(Integer pageNumber, Integer pageSize, String sort) {
        Pageable pageable = null;
        if (sort != null) {
            pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.ASC, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }
        Page<Patient> data = patientRepository.findAll(pageable);
        return data.map(p -> new PatientSummaryDTO(p.getPnumber(), p.getStudy().getId(), p.getStudy().getNameShort()));
    }

    private static CollectionEventSummaryDTO toCollectionEventDTO(CollectionEvent cevent, List<Integer> counts) {
        return new CollectionEventSummaryDTO(
            cevent.getId(),
            cevent.getVisitNumber(),
            counts.get(0),
            counts.get(1),
            cevent.getActivityStatus().getName(),
            cevent
                .getComments()
                .stream()
                .map(comment ->
                    new CommentDTO(
                        comment.getId(),
                        comment.getMessage(),
                        comment.getUser().getFullName(),
                        comment.getCreatedAt().toString()
                    )
                )
                .toList()
        );
    }
}
