package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BenchmarkRunner generates EL-profile reasoning benchmark test cases from an OWL ontology.
 *
 * It uses:
 * - {@link AxiomGrouper} to group axioms by subject
 * - {@link QueryGenerator} to create NL queries
 * - {@link ReasoningValidator} to check entailment
 * - {@link AxiomFormatter} to output human-readable symbolic/formal axiom representations
 *
 * The output is saved to `benchmark_output.json`, containing only MR-0 (original EL cases).
 */
public class BenchmarkRunner {

    /**
     * Main method to run the EL benchmark generation process.
     *
     * @param args Command-line arguments (unused)
     * @throws Exception if ontology loading or file writing fails
     */
    public static void main(String[] args) throws Exception {
        // load the ontology
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File("src/main/resources/ontology/pizza-el.owl");
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);

        // group axioms by named individual or class subject
        Map<String, List<OWLAxiom>> groups = AxiomGrouper.groupBySubject(ontology);

        // output container
        List<Map<String, Object>> output = new ArrayList<>();
        int idCounter = 1;

        for (Map.Entry<String, List<OWLAxiom>> entry : groups.entrySet()) {
            String subject = entry.getKey();
            List<OWLAxiom> axioms = entry.getValue();

            // generate natural language query from the group
            String query = QueryGenerator.generateQuery(axioms);

            // Identify the key axiom to validate (used in "expected" answer)
            OWLAxiom target = QueryGenerator.getQueryAxiom(axioms); // Axiom to validate

            if (target == null) continue; // Skip group if no target axiom found

            // Check if ontology entails this axiom
            boolean isValid = ReasoningValidator.isEntailed(ontology, target);

            // Format each axiom as {symbolic, functional}
            List<Map<String, String>> formatted = axioms.stream()
                    .map(AxiomFormatter::formatAxiom)
                    .collect(Collectors.toList());

            // Create a benchmark entry EL only
            Map<String, Object> base = new LinkedHashMap<>();
            base.put("id", "test_" + idCounter++);
            //base.put("mr", "MR-0");
            base.put("dl_profile", "EL");
            base.put("axioms", formatted);
            base.put("query", query);
            base.put("expected", isValid ? "Yes" : "No");

            output.add(base);  // Add to final output
        }

        // Write output to json file
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter writer = new FileWriter("benchmark_output.json");
        gson.toJson(output, writer);
        writer.close();

        System.out.println("Benchmark with only MR-0 (EL) exported to benchmark_output.json");
    }
}