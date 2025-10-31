package nl.bioinf.models;

class InteractionTest {
    public record Interaction(
            String geneClaimName,
            String interactionType,
            String interactionScore,
            String drugConceptId
    ) { }


}