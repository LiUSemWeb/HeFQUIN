package se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl;

import org.apache.jena.graph.Node;


/**
 * This interface captures the notion of an edge label mapping that is part of the notion of an LPG-to-RDF configuration,
 * and that such an edge Label mapping is an injective function from edge labels in LPGs to IRIs nodes as
 * can occur in RDF graphs.
 * This interface contains the functions:
 *  -map: map String of edge labels to IRIs edge Labels
 *  -unmap: map IRIs edge labels to String
 *  -isPossibleResult: Check if the given RDF term is in the image of this edge label mapping
 */
public interface EdgeLabelMapping {
    /**
     * Returns a URI edge label (in the form of a Jena {@link Node} object) for the given String.
     * It applies this edge label mapping to the given String.
     */
    Node map(String label);

    /**
     * Returns the String that corresponds to the given RDF term.
     * It applies the inverse of this edge Label mapping to the given RDF term (which is assumed to be
     * an IRI) in order to obtain the corresponding String.
     */
    String unmap(Node node);

    /**
     * Check if the given RDF term is in the image of this edgeLabel mapping and,
     * thus, may be one of the RDF terms returned by the {@link #map(String)}
     * function for some String.
     */
    boolean isPossibleResult(Node node);
}
