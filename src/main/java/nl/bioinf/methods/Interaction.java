package nl.bioinf.methods;

public record Interaction(
        String geneClaimName,
        String interactionType,
        String interactionScore,
        String drugConceptId
) {}

