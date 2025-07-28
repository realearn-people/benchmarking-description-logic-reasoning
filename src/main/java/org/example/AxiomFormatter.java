package org.example;

import org.semanticweb.owlapi.model.*;
import java.util.*;

/**
 * AxiomFormatter provides dual-format output for OWL axioms:
 * - Symbolic: A compact, human-readable representation (e.g., ∃hasPart.Tail)
 * - Functional: OWL functional syntax (e.g., SubClassOf(:Cat ObjectSomeValuesFrom(:hasPart :Tail)))
 *
 * This is primarily used for DL benchmarks to test human and LLM understanding.
 */
public class AxiomFormatter {

    /**
     * Converts an OWLAxiom into both symbolic and functional formats.
     *
     * @param axiom the OWLAxiom to format
     * @return a map with keys "symbolic" and "functional"
     */
    public static Map<String, String> formatAxiom(OWLAxiom axiom) {
        Map<String, String> formats = new HashMap<>();

        // Custom symbolic format
        formats.put("symbolic", toSymbolicString(axiom));

        // Functional (default OWL)
        formats.put("functional", axiom.toString());

        return formats;
    }

    /**
     * Returns a symbolic (human-readable) string for supported axiom types.
     * Falls back to OWL functional syntax for unsupported types.
     *
     * @param axiom the axiom to convert
     * @return symbolic string representation
     */
    private static String toSymbolicString(OWLAxiom axiom) {
        if (axiom instanceof OWLSubClassOfAxiom) {
            return formatSubClassOfAxiom((OWLSubClassOfAxiom) axiom);
        } else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
            return formatObjectPropertyAssertionAxiom((OWLObjectPropertyAssertionAxiom) axiom);
        } else if (axiom instanceof OWLClassAssertionAxiom) {
            return formatClassAssertionAxiom((OWLClassAssertionAxiom) axiom);
        }
        // Extend for other axiom types as needed
        return axiom.toString(); // Default to functional string
    }

    /**
     * Formats a subclass axiom (e.g., Cat ⊑ ∃hasPart.Tail).
     */
    //uses the extractName method to extract the class names and the formatClassExpression method to format the superclass
    private static String formatSubClassOfAxiom(OWLSubClassOfAxiom sca) {
        String sub = extractName(sca.getSubClass());
        OWLClassExpression superExpr = sca.getSuperClass();
        String sup = formatClassExpression(superExpr);
        return sub + " ⊑ " + sup;
    }

    /**
     * Formats a class assertion axiom (e.g., pizza1 : Pizza).
     */
    // extracts the individual and class name using extractName
    private static String formatClassAssertionAxiom(OWLClassAssertionAxiom axiom) {
        String individual = extractName(axiom.getIndividual());
        String className = extractName(axiom.getClassExpression());
        return individual + " : " + className;
    }


    /**
     * Formats an object property assertion (e.g., pizza1 hasTopping Mozzarella).
     */
    // extracts the subject, property, and object using extractName
    private static String formatObjectPropertyAssertionAxiom(OWLObjectPropertyAssertionAxiom axiom) {
        String individual = extractName(axiom.getSubject());
        String property = extractName(axiom.getProperty());
        String object = extractName(axiom.getObject());
        return individual + " " + property + " " + object;
    }


    /**
     * Formats an OWL class expression into symbolic form.
     * supports:
     * - Existential quantification: ∃property.Class
     *
     * For all other expressions (e.g. named class), returns the short name or fallback label.
     *
     * @param expr the OWLClassExpression to format
     * @return symbolic string representation of the class expression
     */
    // formats class expressions, such as existential quantification (∃property.class) or role inversions (property^-1)
    private static String formatClassExpression(OWLClassExpression expr) {
        // // Check for existential quantification: ∃R.C
        if (expr instanceof OWLObjectSomeValuesFrom some) {
            String prop = extractName(some.getProperty()); // e.g., hasTopping
            String filler = extractName(some.getFiller()); // e.g., Mozzarella
            return "∃" + prop + "." + filler;
        }

        // For named classes or unsupported expressions, return readable name or fallback
        return extractName(expr);
    }

    /**
     * Extracts the local name of a class expression.
     * If the expression is anonymous, returns a placeholder.
     */
    // extracting human-readable names from various OWL objects
    private static String extractName(OWLClassExpression expr) {
        if (!expr.isAnonymous()) return expr.asOWLClass().getIRI().getShortForm();
        return "AnonymousClass";
    }

    /**
     * Extracts the local name of an object property expression.
     * If anonymous, returns a placeholder.
     */
    private static String extractName(OWLObjectPropertyExpression propExpr) {
        if (!propExpr.isAnonymous()) return propExpr.asOWLObjectProperty().getIRI().getShortForm();
        return "AnonymousProperty";
    }

    /**
     * Extracts the local name of an OWL individual.
     * If anonymous, returns a placeholder.
     */
    private static String extractName(OWLIndividual individual) {
        if (individual instanceof OWLNamedIndividual namedInd) {
            return namedInd.getIRI().getShortForm();
        }
        return "AnonymousIndividual";  // If the individual is anonymous, provide a fallback name
    }
}