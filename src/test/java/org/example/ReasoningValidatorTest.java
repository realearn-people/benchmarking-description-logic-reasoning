package org.example;

import org.junit.jupiter.api.Test;
import org.semanticweb.HermiT.Reasoner;
// delete unused(use indirectly) import
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReasoningValidatorTest {

    @Test
    void testEntailedAxiomReturnsTrue() throws Exception {
        // Setup OWL ontology
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

        OWLClass pizza = factory.getOWLClass(IRI.create("http://example.org/Pizza"));
        OWLClass margherita = factory.getOWLClass(IRI.create("http://example.org/Margherita"));

        // Add: Margherita ⊑ Pizza
        OWLAxiom assertedAxiom = factory.getOWLSubClassOfAxiom(margherita, pizza);
        manager.addAxiom(ontology, assertedAxiom);

        // Check entailment of the same axiom
        boolean result = ReasoningValidator.isEntailed(ontology, assertedAxiom);

        assertTrue(result, "Expected axiom to be entailed by ontology");
    }

    @Test
    void testNonEntailedAxiomReturnsFalse() throws Exception {
        // Setup OWL ontology
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

        OWLClass pizza = factory.getOWLClass(IRI.create("http://example.org/Pizza"));
        OWLClass cheese = factory.getOWLClass(IRI.create("http://example.org/Cheese"));

        // Nothing is asserted, so this should NOT be entailed
        OWLAxiom unassertedAxiom = factory.getOWLSubClassOfAxiom(cheese, pizza);

        boolean result = ReasoningValidator.isEntailed(ontology, unassertedAxiom);

        assertFalse(result, "Expected axiom to NOT be entailed by ontology");
    }
}