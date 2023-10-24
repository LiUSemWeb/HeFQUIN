package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;


/**
 * This interface captures the notion of a property name mapping that is part of the notion of an LPG-to-RDF configuration,
 * and that such a property mapping is an injective function from properties in LPGs to IRIs nodes as
 * can occur in RDF graphs.
 * This interface contains the functions:
 *  -map: map String of properties to IRIs properties
 *  -unmap: map IRIs properties to String
 *  -isPossibleResult: Check if the given RDF term is in the image of this property mapping
 */
public interface PropertyMapping {
    /**
     * Returns a URI property (in the form of a Jena {@link Node} object) for the given String.
     * It applies this property mapping to the given String.
     */
    Node map(String property);

    /**
     * Returns the String that corresponds to the given RDF term.
     * It applies the inverse of this property mapping to the given RDF term (which is assumed to be
     * an IRI) in order to obtain the corresponding String.
     */
    String unmap(Node node);

    /**
     * Check if the given RDF term is in the image of this property mapping and,
     * thus, may be one of the RDF terms returned by the {@link #map(String)}
     * function for some String.
     */
    boolean isPossibleResult(Node node);
}
