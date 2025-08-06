package org.example;

import com.google.gson.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkRunnerTest {

    private static final String OUTPUT_PATH = "output_HermitReasoner/benchmark_output.json";

    @BeforeEach
    void deletePreviousOutput() {
        File file = new File(OUTPUT_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testBenchmarkRunnerGeneratesValidOutput() throws Exception {
        // Run main() – this executes the full pipeline
        BenchmarkRunner.main(new String[0]);

        // Check if file was created
        File outputFile = new File(OUTPUT_PATH);
        assertTrue(outputFile.exists(), "Output JSON file should be created");

        // Parse the JSON
        Gson gson = new Gson();
        JsonArray jsonArray = JsonParser.parseReader(new FileReader(outputFile)).getAsJsonArray();

        assertFalse(jsonArray.isEmpty(), "Output JSON should not be empty");

        for (JsonElement element : jsonArray) {
            JsonObject entry = element.getAsJsonObject();

            assertTrue(entry.has("id"), "Each entry should have an id");
            assertTrue(entry.has("axioms"), "Each entry should have axioms");
            assertTrue(entry.has("query"), "Each entry should have query");
            assertTrue(entry.has("expected"), "Each entry should have expected");

            // Additional checks
            assertTrue(entry.get("expected").getAsString().matches("Yes|No"),
                    "'expected' should be either 'Yes' or 'No'");
        }
    }
}