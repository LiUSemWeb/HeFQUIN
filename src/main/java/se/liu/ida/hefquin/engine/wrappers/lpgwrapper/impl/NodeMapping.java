package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

/**
 * This interface captures the notion of a node mapping that is part of the notion of an LPG-to-RDF configuration,
 * and that such a node mapping is an injective function from nodes in LPGs to IRIs or blank nodes as
 * can occur in RDF graphs.
 * This interface contains the functions:
 *  -map: map nodes to IRIs/BNodes
 *  -unmap: map IRIs/BNodes to nodes
 */
public interface NodeMapping {


    /**
     * Returns a URI or a blank node (in the form of a Jena {@link Node} object) for the given LPG node.
     * It applies this node mapping to the given LPG node
     */
    Node map(LPGNode node);

    /**
     * Returns the LPG node that corresponds to the given RDF term.
     * It applies the inverse of this node mapping to the given RDF term (which is assumed to be a blank node or an IRI) in order to obtain the corresponding LPG node.
     */
    LPGNode unmap(Node node);


    /**
     *Check if the given node is in the image of corresponding
     * node mapping or not.
     */
    boolean isPossibleResult(Node node);
}
