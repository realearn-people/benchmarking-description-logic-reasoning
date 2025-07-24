package org.example;

import com.google.gson.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class ELHExtender {

    public static void main(String[] args) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray input = JsonParser.parseReader(new FileReader("benchmark_output.json")).getAsJsonArray();
        JsonArray output = new JsonArray();

        // Find the maximum id from the existing benchmark_output.json
        int highestId = 0;
        for (JsonElement el : input) {
            JsonObject entry = el.getAsJsonObject();
            String id = entry.get("id").getAsString();
            int currentId = Integer.parseInt(id.replace("test_", ""));
            if (currentId > highestId) {
                highestId = currentId;
            }
        }

        // Start idCounter from the next available number
        int idCounter = highestId + 1;

        // Define role hierarchy extensions manually (ELH style)
        Map<String, String> roleHierarchy = Map.of(
                "hasMozzarella", "hasTopping",
                "hasSausage", "hasTopping",
                "hasSpiciness", "hasProperty"
        );

        // Process each entry in the input benchmark file
        for (JsonElement el : input) {
            JsonObject entry = el.getAsJsonObject();
            JsonArray axioms = entry.getAsJsonArray("axioms");
            Set<String> extraRoleAxioms = new HashSet<>();
            boolean isELH = false;

            // Check if the entry is ELH and handle accordingly
            for (JsonElement axiomEl : axioms) {
                JsonObject axiomObj = axiomEl.getAsJsonObject();
                String symbolic = axiomObj.get("symbolic").getAsString();

                // Check for ELH-specific constructs (existential quantification or role hierarchy)
                if (symbolic.contains("∃")) {
                    isELH = true;

                    // Add role hierarchy if applicable
                    int start = symbolic.indexOf("∃") + 1;
                    int dot = symbolic.indexOf(".", start);
                    if (dot > start) {
                        String role = symbolic.substring(start, dot);
                        if (roleHierarchy.containsKey(role)) {
                            String superRole = roleHierarchy.get(role);
                            extraRoleAxioms.add(role + " ⊑ " + superRole); // ELH-specific axiom
                        }
                    }
                }
            }

            // If it's ELH, add the role hierarchy to the existing axioms array
            if (isELH) {
                // Log the original axioms before adding new ones
                System.out.println("Original axioms for test case " + entry.get("id").getAsString() + ": " + axioms);

                // Add the extra role axioms to the existing axioms
                for (String added : extraRoleAxioms) {
                    JsonObject ax = new JsonObject();
                    ax.addProperty("symbolic", added);
                    ax.addProperty("functional", "SubObjectPropertyOf(" + added.replace(" ⊑ ", " ") + ")");
                    axioms.add(ax);  // Add new axioms to the existing axioms array
                }

                // Log the modified axioms array
                System.out.println("Modified axioms for test case " + entry.get("id").getAsString() + ": " + axioms);
            }

            // Update the test case with the modified axioms array (do not create new test case)
            JsonObject extended = new JsonObject();
            extended.addProperty("id", entry.get("id").getAsString()); // Keep the same ID
            extended.addProperty("mr", isELH ? "MR-H1" : "MR-0"); // Use MR-H1 for ELH, MR-0 for EL
            extended.addProperty("dl_profile", isELH ? "ELH" : "EL");
            extended.add("axioms", axioms);  // Add the modified axioms array
            extended.addProperty("query", entry.get("query").getAsString());
            extended.addProperty("expected", entry.get("expected").getAsString());
            output.add(extended); // Add the extended (or original) test case to the output
        }

        // Write the extended output to a new JSON file
        try (FileWriter writer = new FileWriter("benchmark_output_with_elh.json")) {
            gson.toJson(output, writer);
            System.out.println("Extended ELH benchmark saved to benchmark_output_with_elh.json");
        } catch (Exception e) {
            System.err.println("Error while writing the output file: " + e.getMessage());
        }
    }
}