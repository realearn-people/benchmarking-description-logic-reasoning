package org.example;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AxiomFormatterTest {

    @Test
    void testFormatSubClassAxiom() throws Exception {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass A = df.getOWLClass(IRI.create("http://ex.com#A"));
        OWLClass B = df.getOWLClass(IRI.create("http://ex.com#B"));

        OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(A, B);
        Map<String, String> result = AxiomFormatter.formatAxiom(ax);

        assertEquals("A ⊑ B", result.get("symbolic-format"));
        assertTrue(result.get("OWL-format").contains("SubClassOf"));
    }
}