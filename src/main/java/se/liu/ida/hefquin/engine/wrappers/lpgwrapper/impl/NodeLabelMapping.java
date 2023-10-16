package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;


/**
 * This interface captures the notion of a nodeLabel mapping that is part of the notion of an LPG-to-RDF configuration,
 * and that such a nodeLabel mapping is an injective function from nodeLabels in LPGs to IRIs or RDF-literal nodes as
 * can occur in RDF graphs.
 * This interface contains the functions:
 *  -map: map String of nodeLabels to IRIs/Literal NodeLabels
 *  -unmap: map IRIs/Literal NodeLabels to String
 *  -isPossibleResult: Check if the given RDF term is in the image of this nodeLabel mapping
 */
public interface NodeLabelMapping {
    /**
     * Returns a URI or a literal nodeLabel (in the form of a Jena {@link Node} object) for the given String.
     * It applies this nodeLabel mapping to the given String.
     */
    Node map(String label);

    /**
     * Returns the String that corresponds to the given RDF term.
     * It applies the inverse of this nodeLabel mapping to the given RDF term (which is assumed to be
     * a literal or an IRI) in order to obtain the corresponding String.
     */
    String unmap(Node node);

    /**
     * Check if the given RDF term is in the image of this nodeLable mapping and,
     * thus, may be one of the RDF terms returned by the {@link #map(String)}
     * function for some String.
     */
    boolean isPossibleResult(Node node);
}
