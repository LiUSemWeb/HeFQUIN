package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

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
     * Returns an IRI Node for the given node identifier.
     * @param node the id of the node to be mapped
     */
    Node mapNode(final LPGNode node);

    /**
     * Returns the corresponding LPGNode of a given IRI
     * @param iri the IRI to be unmapped
     */
    LPGNode unmapNode(final String iri);

    /**
     * Returns an IRI or Literal Node for the given node label
     * @param label the label to be mapped
     */
    Node mapNodeLabel(final String label);

    /**
     * Returns the original node label for the given mapped label
     * @param label the mapped IRI or literal of a node label
     */
    String unmapNodeLabel(final String label);

    /**
     * Returns an IRI or Literal Node for a given edge label
     * @param label the edge label to map
     */
    Node mapEdgeLabel(final String label);

    /**
     * Returns the original edge label for the given mapped label
     * @param label the mapped IRI or literal of an edge label
     */
    String unmapEdgeLabel(final String label);

    /**
     * Returns an IRI Node for a given property
     * @param property the name of the property to map
     */
    Node mapProperty(final String property);

    /**
     * Returns the original property name of the given mapped IRI
     * @param iri the mapped IRI of a property
     */
    String unmapProperty(final String iri);

    /**
     * Returns the IRI Node of the property defined as u_label
     */
    Node getLabel();

}
