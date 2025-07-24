package org.example;

import org.semanticweb.owlapi.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * QureyGenerator generates natural language query from a list of OWL axioms,
 * focusing on EL and ELH subsets of Description Logic.
 * It supports query generation, paraphrasing, and identification of target axioms.
 */
public class QueryGenerator {

    // Returns the NL question for both EL and ELH axioms

    /**
     * generates natural language query from a list of OWL axioms
     * Supports EL/ELH-style subclass axioms involving existential restrictions.
     *
     * Example: From axiom "Cat ⊑ ∃hasPart.Tail", generate:
     * "Does every Cat have hasPart.Tail?"
     *
     * @param axioms the list of OWL axioms
     * @return a natural language query, or a fallback message if no suitable axiom found
     */
    public static String generateQuery(List<OWLAxiom> axioms) {
        // Loop through each OWLAxiom in the list.
        for (OWLAxiom axiom : axioms) {
            // If it's an OWLSubClassOfAxiom, extract the subclass and superclass
            if (axiom instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) axiom;
                OWLClassExpression superClass = sca.getSuperClass();
                OWLClassExpression subClass = sca.getSubClass();

                // If the superclass is an existential restriction (∃R.C), extract the property (R) and filler (C)
                if (superClass instanceof OWLObjectSomeValuesFrom) {
                    OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) superClass;
                    OWLObjectPropertyExpression property = restriction.getProperty();
                    OWLClassExpression filler = restriction.getFiller();

                    // Check if the property is inverted (ELH-specific)
                    // remove inversion
                    String propertyName = extractName(property);
                    if (property instanceof OWLObjectInverseOf) {
                        OWLObjectPropertyExpression inverseProperty = ((OWLObjectInverseOf) property).getInverse();
                        propertyName = extractName(inverseProperty) + "^-1"; // Inverted role
                    }

                    // Construct the query
                    return "Does every " + extractName(subClass) + " have " +
                            propertyName + "." + extractName(filler) + "?";
                }
            }
            // Add more axiom types if needed, for example OWLClassAssertionAxiom, etc.
        }
        return "No suitable query generated.";
    }

    // Get the corresponding axiom to generate the query

    /***
     * axiom list is from
     * Retrieves the first subclass axiom with an existential restriction.
     * This is useful for identifying the "key axiom" associated with a query (e.g. for MR-9).
     *
     * @param axioms the list of OWL axioms
     * @return the first matching OWLSubClassOfAxiom, or null if none found
     */
    public static OWLAxiom getQueryAxiom(List<OWLAxiom> axioms) {
        for (OWLAxiom axiom : axioms) {
            if (axiom instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) axiom;
                if (sca.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
                    return sca;
                }
            }
        }
        return null;
    }

    // Paraphrase query to add variation

    /**
     * Paraphrases the generated query to add linguistic variety.
     * This is useful for testing robustness of LLM reasoning under prompt variation (e.g. MR-1).
     *
     * @param original the original query
     * @return a paraphrased version of the query
     */
    public static String paraphraseQuery(String original) {
        return original.replace("Does every", "Is it true that all")
                .replace("have", "contain");
    }

    /**
     * Removes a specific axiom from the list.
     * Useful for metamorphic testing (e.g. MR-9: Remove Key Axiom).
     *
     * @param axioms the original list of axioms
     * @param toRemove the axiom to remove
     * @return a new list with the specified axiom excluded
     */
    // Return a new list of axioms with the target axiom removed for MR-9 (Remove Key Axiom).
    public static List<OWLAxiom> removeQueryAxiom(List<OWLAxiom> axioms, OWLAxiom toRemove) {
        List<OWLAxiom> result = new ArrayList<>();
        for (OWLAxiom ax : axioms) {
            if (!ax.equals(toRemove)) {
                result.add(ax);
            }
        }
        return result;
    }


    /**
     * Extracts the short form (local name) of an OWLEntity from its IRI.
     *
     * @param entity the OWLEntity
     * @return the short name (e.g., "Person" from http://example.org#Person)
     */
    private static String extractName(OWLEntity entity) {
        return entity.getIRI().getShortForm();
    }


    /**
     * Extracts a readable name from an OWLClassExpression.
     * Falls back to "AnonymousClass" for anonymous expressions.
     *
     * @param expr the class expression
     * @return the extracted name or fallback
     */
    private static String extractName(OWLClassExpression expr) {
        if (!expr.isAnonymous()) return expr.toString().replaceAll(".*[#<](.*?)[>'].*", "$1");
        return "AnonymousClass";
    }


    /**
     * Extracts the short name of an object property expression.
     * For anonymous properties, returns a placeholder string.
     *
     * @param propExpr the property expression
     * @return the property name
     */
    private static String extractName(OWLObjectPropertyExpression propExpr) {
        if (!propExpr.isAnonymous()) return propExpr.asOWLObjectProperty().getIRI().getShortForm();
        return "anonymousProperty";
    }
}