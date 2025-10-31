package nl.bioinf.models;

public record Interaction(
        String geneClaimName,
        String interactionType,
        String interactionScore,
        String drugConceptId
) {}

