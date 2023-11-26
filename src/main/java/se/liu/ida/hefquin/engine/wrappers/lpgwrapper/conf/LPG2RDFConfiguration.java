package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;

/**
 * Represents an LPG-to-RDF configuration as defined in Hartig, Olaf. "Foundations to Query Labeled Property
 * Graphs using SPARQL." SEM4TRA-AMAR@ SEMANTICS. 2019.
 *
 * This interface contains the functions:
 * -nm to map nodes to IRIs/BNodes
 * -nlm to map node labels to IRIs
 * -elm to map edge labels to IRIs
 * -pm to map property names to IRIs
 * -getULabel to obtain the value of u_label
 *
 * The mappings return Jena Node objects, to give flexibility if IRIs or Literals are needed
 */
public interface LPG2RDFConfiguration {

    /**
     * Returns a URI or a blank node (in the form of a Jena {@link Node} object) for the given LPG node.
     */
    Node mapNode(final LPGNode node);

    /**
     * Returns true if the given RDF term is in the image of the mapNode function and, thus, in the domain of the unmapNode function.
     */
    boolean mapsToNode(final Node n);

    /**
     * Returns the LPG node that corresponds to the given RDF term.
     * This function is the inverse of mapNode. As such, it is defined only for the RDF terms that may be returned by the mapNode function and, thus, throws an {@link IllegalArgumentException} if any other RDF term is given to it.
     */
    LPGNode unmapNode(final Node node);

    /**
     * Returns a Jena Node for the given node label
     * @param label the node label to be mapped
     */
    Node mapNodeLabel(final String label);

    /**
     * Checks if the given node is the mapping of a
     * Node Label using this configuration
     * @param n the Node to be tested
     */
    boolean mapsToLabel(final Node n);

    /**
     * Returns the original node label for the given mapped label
     * @param node the mapped Jena Node of a node label
     */
    String unmapNodeLabel(final Node node);

    /**
     * Returns a Jena Node for a given edge label
     * @param label the edge label to map
     */
    Node mapEdgeLabel(final String label);

    /**
     * Checks if the given node is the mapping of an
     * Edge Label using this configuration
     * @param n the Node to be tested
     */
    boolean mapsToEdgeLabel(final Node n);

    /**
     * Returns the original edge label for the given mapped label
     * @param node the mapped Jena Node of an edge label
     */
    String unmapEdgeLabel(final Node node);

    /**
     * Returns a Jena Node for a given property
     * @param property the name of the property to map
     */
    Node mapProperty(final String property);

    /**
     * Checks if the given node is the mapping of a property using
     * this configuration or not.
     * @param n the Node to be tested
     */
    boolean mapsToProperty(final Node n);

    /**
     * Returns the original property name of the given mapped IRI
     * @param node the mapped Jena Node of a property
     */
    String unmapProperty(final Node node);

    /**
     * Returns the Jena Node of the property defined as u_label
     */
    Node getLabel();

    /**
     * Checks if the given node is equal to the
     * Label Node of this configuration or not.
     * @param n the Node to be tested
     */
    boolean isLabelIRI(final Node n);

}
