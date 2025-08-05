package org.example;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueryGeneratorTest {

    @Test
    void testGenerateQuery_ELForm() throws Exception {
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        OWLClass clsA = df.getOWLClass(IRI.create("http://ex.com#A"));
        OWLClass clsB = df.getOWLClass(IRI.create("http://ex.com#B"));
        OWLObjectProperty hasPart = df.getOWLObjectProperty(IRI.create("http://ex.com#hasPart"));

        OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(
                clsA,
                df.getOWLObjectSomeValuesFrom(hasPart, clsB)
        );

        String query = QueryGenerator.generateQuery(Collections.singletonList(ax));

        assertEquals("Does every A have hasPart.B?", query);
    }

    @Test
    void testGetQueryAxiom_returnsCorrectAxiom() throws Exception {
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        OWLClass clsA = df.getOWLClass(IRI.create("http://ex.com#A"));
        OWLClass clsB = df.getOWLClass(IRI.create("http://ex.com#B"));
        OWLObjectProperty hasPart = df.getOWLObjectProperty(IRI.create("http://ex.com#hasPart"));

        OWLSubClassOfAxiom key = df.getOWLSubClassOfAxiom(
                clsA,
                df.getOWLObjectSomeValuesFrom(hasPart, clsB)
        );

        OWLSubClassOfAxiom filler = df.getOWLSubClassOfAxiom(clsB, clsA);

        OWLAxiom result = QueryGenerator.getQueryAxiom(List.of(filler, key));

        assertEquals(key, result);
    }
}