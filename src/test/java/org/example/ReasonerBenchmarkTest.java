package org.example;

import org.junit.jupiter.api.Test;
import org.semanticweb.HermiT.Reasoner;
// did we not use hermit here?
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReasonerBenchmarkTest {

    @Test
    void testCompareReasoners_withSimpleEntailment() throws Exception {
        // === Setup OWL API ===
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

        // === Classes ===
        OWLClass pizza = factory.getOWLClass(IRI.create("http://example.org/Pizza"));
        OWLClass margherita = factory.getOWLClass(IRI.create("http://example.org/Margherita"));

        // Margherita ⊑ Pizza
        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(margherita, pizza);
        manager.addAxiom(ontology, axiom);

        // === Query Axiom (already entailed): Margherita ⊑ Pizza
        OWLAxiom query = factory.getOWLSubClassOfAxiom(margherita, pizza);

        // === Act ===
        // This will run both reasoners and print entailment results
        assertDoesNotThrow(() -> ReasonerBenchmark.compareReasoners(ontology, query));
    }

    // Optional: Capture and assert System.out
}