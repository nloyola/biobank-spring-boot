package edu.ualberta.med.biobank.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import edu.ualberta.med.biobank.applicationevents.BiobankEventPublisher;
import edu.ualberta.med.biobank.domain.Comment;
import edu.ualberta.med.biobank.domain.Patient;
import edu.ualberta.med.biobank.domain.User;
import edu.ualberta.med.biobank.dtos.CollectionEventSummaryDTO;
import edu.ualberta.med.biobank.dtos.CommentAddDTO;
import edu.ualberta.med.biobank.dtos.CommentDTO;
import edu.ualberta.med.biobank.dtos.PatientAddDTO;
import edu.ualberta.med.biobank.dtos.PatientDTO;
import edu.ualberta.med.biobank.errors.AppError;
import edu.ualberta.med.biobank.errors.EntityNotFound;
import edu.ualberta.med.biobank.errors.PermissionError;
import edu.ualberta.med.biobank.errors.ValidationError;
import edu.ualberta.med.biobank.permission.patient.PatientCreatePermission;
import edu.ualberta.med.biobank.permission.patient.PatientReadPermission;
import edu.ualberta.med.biobank.permission.patient.PatientUpdatePermission;
import edu.ualberta.med.biobank.repositories.CollectionEventRepository;
import edu.ualberta.med.biobank.repositories.PatientRepository;
import edu.ualberta.med.biobank.repositories.StudyRepository;
import edu.ualberta.med.biobank.util.LoggingUtils;
import io.jbock.util.Either;
import jakarta.persistence.Tuple;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class PatientService {

    Logger logger = LoggerFactory.getLogger(PatientService.class);

    private PatientRepository patientRepository;

    private CollectionEventRepository collectionEventRepository;

    private BiobankEventPublisher eventPublisher;

    private StudyRepository studyRepository;

    private UserService userService;

    private Validator validator;

    public PatientService(
        PatientRepository patientRepository,
        CollectionEventRepository collectionEventRepository,
        StudyRepository studyRepository,
        UserService userService,
        Validator validator,
        BiobankEventPublisher eventPublisher
    ) {
        this.patientRepository = patientRepository;
        this.collectionEventRepository = collectionEventRepository;
        this.studyRepository = studyRepository;
        this.userService = userService;
        this.validator = validator;
        this.eventPublisher = eventPublisher;
    }

    public Either<AppError, Patient> getByPatientId(Integer id) {
        return patientRepository
            .findById(id)
            .map(Either::<AppError, Patient>right)
            .orElseGet(() -> Either.left(new EntityNotFound("patient")));
    }

    public Either<AppError, PatientDTO> findByPnumber(String pnumber) {
        var found = patientRepository.findByPnumber(pnumber, Tuple.class).stream().findFirst();

        if (found.isEmpty()) {
            return Either.left(new EntityNotFound("patient"));
        }

        var patient = PatientDTO.fromTuple(found.get());

        var permission = new PatientReadPermission(patient.studyId());
        var allowedMaybe = permission.isAllowed();
        return allowedMaybe
            .flatMap(allowed -> {
                logger.debug("patient study id: {}, allowed: {}", patient.studyId(), allowed);
                if (!allowed) {
                    return Either.left(new PermissionError("study"));
                }
                return Either.right(patient);
            })
            .map(p -> {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                eventPublisher.publishPatientRead(auth.getName(), pnumber);

                final var ceSummary = collectionEventRepository
                    .findSummariesByPatient(pnumber, Tuple.class)
                    .stream()
                    .map(row -> CollectionEventSummaryDTO.fromTuple(row))
                    .toList();

                return p.withCollectionEvents(ceSummary);
            });
    }

    public Either<AppError, Collection<CommentDTO>> patientComments(String pnumber) {
        return patientExists(pnumber)
            .flatMap(patient -> {
                var permission = new PatientReadPermission(patient.studyId());
                return permission.isAllowed();
            })
            .flatMap(allowed -> {
                if (!allowed) {
                    return Either.left(new PermissionError("study"));
                }
                return Either.right(allowed);
            })
            .map(allowed -> {
                Map<Integer, CommentDTO> comments = new HashMap<>();

                patientRepository
                    .patientComments(pnumber, Tuple.class)
                    .stream()
                    .forEach(row -> {
                        var commentId = row.get("commentId", Integer.class);
                        if (commentId != null) {
                            comments.computeIfAbsent(commentId, id -> CommentDTO.fromTuple(row));
                        }
                    });


                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                eventPublisher.publishPatientRead(auth.getName(), pnumber);
                return comments.values();
            });
    }

    public Either<AppError, PatientDTO> save(PatientAddDTO dto) {
        var violations = validator.validate(dto);

        if (!violations.isEmpty()) {
            List<String> errors = new ArrayList<>();
            for (ConstraintViolation<PatientAddDTO> constraintViolation : violations) {
                errors.add(constraintViolation.getMessage());
            }
            return Either.left(new ValidationError(String.join(", ", errors)));
        }

        // does patient already exist?
        var found = patientRepository.findByPnumber(dto.pnumber(), Tuple.class);
        if (!found.isEmpty()) {
            return Either.left(new ValidationError("patient already exists"));
        }

        var studyFound = studyRepository.findByNameShort(dto.studyNameShort(), Tuple.class).stream().findFirst();
        if (studyFound.isEmpty()) {
            return Either.left(
                new EntityNotFound("study with short name not found: %s".formatted(dto.studyNameShort()))
            );
        }

        var studyId = studyFound.get().get("id", Integer.class);
        var permission = new PatientCreatePermission(studyId);
        var allowedMaybe = permission.isAllowed();
        return allowedMaybe
            .flatMap(allowed -> {
                logger.debug("patient study id: {}, allowed: {}", studyId, allowed);
                if (!allowed) {
                    return Either.left(new PermissionError("study"));
                }
                return Either.right(allowed);
            })
            .flatMap(allowed -> {
                var study = studyRepository.getReferenceById(studyId);

                Patient patient = new Patient();
                patient.setPnumber(dto.pnumber());
                patient.setStudy(study);
                patient.setCreatedAt(new Date());
                Patient savedPatient = patientRepository.save(patient);

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                eventPublisher.publishPatientCreated(auth.getName(), dto.pnumber());
                return Either.right(PatientDTO.fromPatient(savedPatient));
            });
    }

    public Either<AppError, CommentDTO> addPatientComment(String pnumber, CommentAddDTO dto) {
        var violations = validator.validate(dto);

        if (!violations.isEmpty()) {
            List<String> errors = new ArrayList<>();
            for (ConstraintViolation<CommentAddDTO> violation : violations) {
                errors.add(violation.getMessage());
            }
            return Either.left(new ValidationError(String.join(", ", errors)));
        }

        return patientExists(pnumber)
            .flatMap(patient -> {
                var permission = new PatientUpdatePermission(patient.studyId());
                return permission.isAllowed()
                    .flatMap(allowed -> {
                        if (!allowed) {
                            return Either.left(new PermissionError("study"));
                        }
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        return userService.findOneWithMemberships(auth.getName());
                    })
                    .flatMap(userDto -> {
                        User user = userService.getById(userDto.userId());

                        Comment comment = new Comment();
                        comment.setUser(user);
                        comment.setCreatedAt(new Date());
                        comment.setMessage(dto.message().trim());

                        Patient patientToUpdate = patientRepository.getReferenceById(patient.id());
                        patientToUpdate.getComments().add(comment);
                        patientRepository.save(patientToUpdate);

                        eventPublisher.publishPatientUpdated(userDto.username(), pnumber);
                        return Either.right(CommentDTO.fromComment(comment));
                    });
            });
    }

    private Either<AppError, PatientDTO> patientExists(String pnumber) {
        var found = patientRepository.findByPnumber(pnumber, Tuple.class).stream().findFirst();
        if (found.isEmpty()) {
            return Either.left(new ValidationError("patient does not exists"));
        }
        var patient = PatientDTO.fromTuple(found.get());
        return Either.right(patient);
    }
}
