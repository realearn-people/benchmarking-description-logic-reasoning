package org.example;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AxiomGrouperTest {

    @Test
    void testGroupBySubject_basicGrouping() throws Exception {
        // Set up OWL API components
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

        // Create named classes
        OWLClass pizza = dataFactory.getOWLClass(IRI.create("http://example.org/Pizza"));
        OWLClass margherita = dataFactory.getOWLClass(IRI.create("http://example.org/Margherita"));
        OWLClass cheese = dataFactory.getOWLClass(IRI.create("http://example.org/Cheese"));

        // Add subclass axioms
        OWLAxiom axiom1 = dataFactory.getOWLSubClassOfAxiom(margherita, pizza);
        OWLAxiom axiom2 = dataFactory.getOWLSubClassOfAxiom(margherita, cheese);

        manager.addAxiom(ontology, axiom1);
        manager.addAxiom(ontology, axiom2);

        // Run the method
        Map<String, List<OWLAxiom>> grouped = AxiomGrouper.groupBySubject(ontology);

        // === Assertions ===
        assertEquals(1, grouped.size(), "Only Margherita should be grouped");

        assertTrue(grouped.containsKey("Margherita"));
        List<OWLAxiom> margheritaAxioms = grouped.get("Margherita");
        assertEquals(2, margheritaAxioms.size());
        assertTrue(margheritaAxioms.contains(axiom1));
        assertTrue(margheritaAxioms.contains(axiom2));
    }

    @Test
    void testGroupBySubject_ignoresAnonymousSubclasses() throws Exception {
        // Set up
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

        OWLClass topping = dataFactory.getOWLClass(IRI.create("http://example.org/Topping"));
        OWLObjectProperty hasIngredient = dataFactory.getOWLObjectProperty(IRI.create("http://example.org/hasIngredient"));
        OWLClass tomato = dataFactory.getOWLClass(IRI.create("http://example.org/Tomato"));

        // Anonymous subclass: ∃hasIngredient.Tomato ⊑ Topping
        OWLClassExpression anonSub = dataFactory.getOWLObjectSomeValuesFrom(hasIngredient, tomato);
        OWLAxiom axiom = dataFactory.getOWLSubClassOfAxiom(anonSub, topping);
        manager.addAxiom(ontology, axiom);

        // Run
        Map<String, List<OWLAxiom>> grouped = AxiomGrouper.groupBySubject(ontology);

        // It should be empty since the subclass is anonymous
        assertTrue(grouped.isEmpty(), "Anonymous subclasses should not be grouped");
    }
}