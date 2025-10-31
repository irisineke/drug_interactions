package nl.bioinf.methods;

import static org.junit.jupiter.api.Assertions.*;

class InteractionTest {
    public record Interaction(
            String geneClaimName,
            String interactionType,
            String interactionScore,
            String drugConceptId
    ) { }


}