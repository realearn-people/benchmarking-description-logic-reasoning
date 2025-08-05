package org.example;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

//import org.semanticweb.owlapi.model.*;
//import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
//import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

public class ReasoningValidator {

    /**
     * Validates if an axiom is entailed by the ontology using a Structural Reasoner/ HermiT Reasoner.
     *
     * @param ontology the OWL ontology
     * @param axiom the OWL axiom to check
     * @return true if the axiom is entailed, false otherwise
     */
    public static boolean isEntailed(OWLOntology ontology, OWLAxiom axiom) {
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory(); // Structural Reasoner: new StructuralReasonerFactory()
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        boolean entailed = false;

        try {
            reasoner.precomputeInferences(); // Optional but helps for performance
            // Check entailment of the given axiom
            entailed = reasoner.isEntailed(axiom);
        } catch (Exception e) {
            // Handle reasoning errors or exceptions (e.g., memory issues, ontology parsing errors)
            System.err.println("Reasoning error: " + e.getMessage());
            e.printStackTrace();  // print stack trace for debugging
        } finally {
            // Ensure the reasoner is disposed of after use
            reasoner.dispose();
        }

        return entailed;
    }
}