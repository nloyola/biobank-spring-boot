package edu.ualberta.med.biobank.dtos;

import java.util.Date;

public record CollectionEventSummaryDTO(
    Integer id,
    Integer visitNumber,
    Integer specimenCount,
    Integer aliquotCount,
    Date createdAt,
    String status
) {}
