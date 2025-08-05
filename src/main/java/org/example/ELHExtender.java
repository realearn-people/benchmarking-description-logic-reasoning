package org.example;

import com.google.gson.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * ELHExtender reads a JSON file of Description Logic (DL) test cases in the EL profile,
 * identifies existential roles (∃R.C), and adds role hierarchy axioms (R ⊑ superOfR),
 * effectively extending the test case into ELH profile.
 *
 * The resulting ELH-extended benchmark is written to a new JSON file.
 */
public class ELHExtender {

    /**
     * Main method to execute the EL → ELH transformation.
     * Reads 'benchmark_output.json', extends role hierarchy axioms,
     * and writes the result to 'benchmark_output_with_elh.json'.
     *
     * @param args CLI arguments (not used)
     * @throws Exception if file operations fail
     */
    public static void main(String[] args) throws Exception {
        extendToELH("output_HermitReasoner/benchmark_output.json", "output_HermitReasoner/benchmark_output_with_elh.json");
    }

    public static void extendToELH(String inputFilePath, String outputFilePath) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray input = JsonParser.parseReader(new FileReader(inputFilePath)).getAsJsonArray();
        JsonArray output = new JsonArray();

        for (JsonElement el : input) {
            JsonObject entry = el.getAsJsonObject();
            JsonArray originalAxioms = entry.getAsJsonArray("axioms");
            JsonArray modifiedAxioms = new JsonArray();
            Set<String> foundRoles = new HashSet<>();
            boolean isELH = false;

            for (JsonElement axEl : originalAxioms) {
                JsonObject ax = axEl.getAsJsonObject();
                String symbolic = ax.get("symbolic-format").getAsString();
                modifiedAxioms.add(ax);

                if (symbolic.contains("∃")) {
                    int start = symbolic.indexOf("∃") + 1;
                    int dot = symbolic.indexOf(".", start);
                    if (dot > start) {
                        String role = symbolic.substring(start, dot).trim();
                        foundRoles.add(role);
                    }
                }
            }

            for (String role : foundRoles) {
                String superRole = "superOf" + capitalize(role);
                String symbolic = role + " ⊑ " + superRole;

                JsonObject ax = new JsonObject();
                ax.addProperty("symbolic-format", symbolic);
                ax.addProperty("OWL-format", "SubObjectPropertyOf(" + role + " " + superRole + ")");
                modifiedAxioms.add(ax);
                isELH = true;
            }

            JsonObject extended = entry.deepCopy();
            extended.add("axioms", modifiedAxioms);
            extended.addProperty("dl_profile", isELH ? "ELH" : "EL");
            output.add(extended);
        }

        try (FileWriter writer = new FileWriter(outputFilePath)) {
            gson.toJson(output, writer);
            System.out.println("Extended ELH benchmark saved to " + outputFilePath);
        }
    }

    /**
     * Capitalizes the first letter of a role name.
     * Used when constructing new super-role names (e.g., superOfHasPart).
     *
     * @param role the original role name
     * @return the capitalized role name
     */
    private static String capitalize(String role) {
        return role.length() > 0 ? role.substring(0, 1).toUpperCase() + role.substring(1) : role;
    }
}