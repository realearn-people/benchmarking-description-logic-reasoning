package org.example;

import com.google.gson.*;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ELHExtenderTest {

    private static final String TEST_INPUT_FILE = "test_benchmark_input.json";
    private static final String TEST_OUTPUT_FILE = "test_benchmark_output_with_elh.json";

    @BeforeEach
    void setUp() throws IOException {
        // Sample EL input: one axiom with existential role ∃hasPart.Engine
        JsonArray testCases = new JsonArray();

        JsonObject testCase = new JsonObject();
        testCase.addProperty("id", 1);
        testCase.addProperty("dl_profile", "EL");

        JsonArray axioms = new JsonArray();
        JsonObject axiom = new JsonObject();
        axiom.addProperty("symbolic-format", "Car ⊑ ∃hasPart.Engine");
        axiom.addProperty("OWL-format", "SubClassOf(Car ObjectSomeValuesFrom(hasPart Engine))");
        axioms.add(axiom);

        testCase.add("axioms", axioms);
        testCases.add(testCase);

        try (FileWriter writer = new FileWriter(TEST_INPUT_FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(testCases, writer);
        }
    }

    @Test
    void testELHExtensionAddsRoleHierarchy() throws Exception {
        // Call method under test (after refactoring ELHExtender)
        ELHExtender.extendToELH(TEST_INPUT_FILE, TEST_OUTPUT_FILE);

        // Read the output JSON file
        JsonArray result = JsonParser.parseReader(new FileReader(TEST_OUTPUT_FILE)).getAsJsonArray();
        assertEquals(1, result.size());

        JsonObject updatedTestCase = result.get(0).getAsJsonObject();
        assertEquals("ELH", updatedTestCase.get("dl_profile").getAsString());

        JsonArray updatedAxioms = updatedTestCase.getAsJsonArray("axioms");
        assertEquals(2, updatedAxioms.size());

        boolean foundRoleHierarchy = false;
        for (JsonElement axEl : updatedAxioms) {
            String sym = axEl.getAsJsonObject().get("symbolic-format").getAsString();
            if (sym.equals("hasPart ⊑ superOfHasPart")) {
                foundRoleHierarchy = true;
                break;
            }
        }

        assertTrue(foundRoleHierarchy, "Expected role hierarchy axiom was not found");
    }

    @AfterEach
    void tearDown() {
        new File(TEST_INPUT_FILE).delete();
        new File(TEST_OUTPUT_FILE).delete();
    }
}