package org.example;

import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.logging.*;

/**
 * Groups axioms by their subclass (LHS) to help create coherent examples for each concept.
 */
public class AxiomGrouper {

    private static final Logger logger = Logger.getLogger(AxiomGrouper.class.getName());

    /**
     * Groups OWL axioms by their subclass (left-hand side of a SubClassOf axiom).
     * Only groups SubClassOf axioms that are in EL form.
     *
     * @param ontology the loaded OWLOntology
     * @return a map from subclass name to a list of its related axioms
     */
    // group axioms by their subclass (LHS of ⊑) so that we can form coherent batches per concept
    public static Map<String, List<OWLAxiom>> groupBySubject(OWLOntology ontology) {
        Map<String, List<OWLAxiom>> groups = new HashMap<>();

        // loop through all axioms
        for (OWLAxiom ax : ontology.getAxioms()) {
            // filters for those of type OWLSubClassOfAxiom
            if (ax instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom subAxiom = (OWLSubClassOfAxiom) ax;
                OWLClassExpression lhs = subAxiom.getSubClass();

                // checks that the subclass (lhs) is a named class, not an anonymous one like intersections or unions
                if (!lhs.isAnonymous()) {
                    // uses the class name (like MargheritaPizza) as a key
                    String className = lhs.asOWLClass().getIRI().getShortForm();
                    groups.computeIfAbsent(className, k -> new ArrayList<>()).add(ax);
                } else {
                    // If the subclass is anonymous, log the case and skip or handle differently
                    logger.warning("Anonymous class detected in SubClassOfAxiom: " + lhs);
                }
            }
        }

        // Log the size of the resulting groups
        logger.info("Axioms grouped by subclass. Total groups: " + groups.size());
        return groups;
    }
}