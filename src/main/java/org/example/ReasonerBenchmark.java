package org.example;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;

import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

public class ReasonerBenchmark {

    public static void compareReasoners(OWLOntology ontology, OWLAxiom axiom) {
        // === Structural Reasoner ===
        System.out.println("\n StructuralReasoner:");
        OWLReasonerFactory structuralFactory = new StructuralReasonerFactory();
        OWLReasoner structuralReasoner = structuralFactory.createReasoner(ontology);
        try {
            long start = System.nanoTime();
            boolean entailed = structuralReasoner.isEntailed(axiom);
            long end = System.nanoTime();
            long durationNs = end - start;
            long durationMs = durationNs / 1_000_000;

            System.out.println("Entailed? " + entailed);
            System.out.println("Time taken: " + durationNs + " ns (" + durationMs + " ms)");
        } catch (Exception e) {
            System.err.println("StructuralReasoner error: " + e.getMessage());
        } finally {
            structuralReasoner.dispose();
        }

        // === HermiT Reasoner ===
        System.out.println("\n HermiT Reasoner:");
        OWLReasonerFactory hermitFactory = new Reasoner.ReasonerFactory();
        OWLReasoner hermitReasoner = hermitFactory.createReasoner(ontology);
        try {
            long start = System.nanoTime();
            hermitReasoner.precomputeInferences(); // Optional but recommended
            boolean entailed = hermitReasoner.isEntailed(axiom);
            long end = System.nanoTime();
            long durationNs = end - start;
            long durationMs = durationNs / 1_000_000;

            System.out.println("Entailed? " + entailed);
            System.out.println("Time taken: " + durationNs + " ns (" + durationMs + " ms)");
        } catch (Exception e) {
            System.err.println("HermiT error: " + e.getMessage());
        } finally {
            hermitReasoner.dispose();
        }
    }
}