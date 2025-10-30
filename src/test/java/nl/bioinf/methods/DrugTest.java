package nl.bioinf.methods;

import static org.junit.jupiter.api.Assertions.*;

class DrugTest {
    public record Drug(String drugClaimName, String conceptId) { }
}