package se.liu.ida.hefquin.engine.data.mappings;

import org.apache.jena.graph.Node;

import java.util.Set;

public interface TermMapping {

    /**
     * Get the type of the mapping rule, e.g., equivalentClass, equivalentProperty, subClassOf, subPropertyOf, unionOf
     */
    Node getTypeOfRule();

    /**
     * Apply the TermMapping and return the translated RDF term
     */
    Set<Node> getTranslatedTerms();

}
