package se.liu.ida.hefquin.engine.wrappers.lpg.conf;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNode;

/**
 * Represents the notion of an LPG-to-RDF configuration as defined in
 * 
 * Olaf Hartig: "Foundations to Query Labeled Property Graphs using SPARQL*."
 * In the Proceedings of the 1st Int. Workshop on Approaches for Making Data
 * Interoperable (AMAR) at SEMANTiCS 2019.
 * https://ceur-ws.org/Vol-2447/paper3.pdf
 *
 * Such a configuration consist of the following five components, which are
 * captured by the methods of this interface.
 *   i) an injective function called node mapping that maps every given LPG node
 *      to either an IRI or a blank node,
 *  ii) an injective function called node label mapping that maps labels of LPG
 *      nodes to IRIs or literals,
 * iii) an injective function called edge label mapping that maps labels of LPG
 *      edges to IRIs,
 *  iv) an injective function called property name mapping that maps names of
 *      properties of LPG nodes and edge to IRIs, and
 *   v) an IRI to be used in the predicate position of any RDF triple that
 *      captures information about the label of some LPG node.
 */
public interface LPG2RDFConfiguration
{
	/**
	 * Applies the node mapping to the given LPG node and, thus, returns
	 * the IRI or blank node (in the form of a Jena {@link Node} object)
	 * that the LPG node is mapped to.
	 */
	Node getRDFTermForLPGNode( LPGNode node );

	/**
	 * Returns true if the given RDF term is in the image of the node mapping
	 * and, thus, may be returned by {@link #getRDFTermForLPGNode(LPGNode)}
	 * for some LPG node.
	 */
	boolean isRDFTermForLPGNode( Node term );

	/**
	 * Applies the inverse of the node mapping to the given RDF term and,
	 * thus, returns the LPG node that is mapped to the given RDF term
	 * (which must be an IRI or a blank node).
	 *
	 * If the given RDF term is not in the image of the node mapping (in
	 * which case {@link #isRDFTermForLPGNode(Node)} returns false), this
	 * method throws an {@link IllegalArgumentException}.
	 */
	LPGNode getLPGNodeForRDFTerm( Node term );

	/**
	 * Applies the node label mapping to the given node label and, thus,
	 * returns the IRI or literal (in the form of a Jena {@link Node}
	 * object) that the node label is mapped to.
	 */
	Node getRDFTermForNodeLabel( String nodeLabel );

	/**
	 * Returns true if the given RDF term is in the image of the node label mapping
	 * and, thus, may be returned by {@link #getRDFTermForNodeLabel(String)}
	 * for some node label.
	 */
	boolean isRDFTermForNodeLabel( Node term );

	/**
	 * Applies the inverse of the node label mapping to the given RDF term
	 * and, thus, returns the node label that is mapped to the given RDF term
	 * (which must be an IRI or a literal).
	 *
	 * If the given RDF term is not in the image of the node label mapping (in
	 * which case {@link #isRDFTermForNodeLabel(Node)} returns false), this
	 * method throws an {@link IllegalArgumentException}.
	 */
	String getNodeLabelForRDFTerm( Node term );

	/**
	 * Applies the edge label mapping to the given edge label and, thus,
	 * returns the IRI (in the form of a Jena {@link Node} object) that
	 * the edge label is mapped to.
	 */
	Node getIRIForEdgeLabel( String edgeLabel );

	/**
	 * Returns true if the given IRI is in the image of the edge label mapping
	 * and, thus, may be returned by {@link #getIRIForEdgeLabel(String)} for
	 * some edge label.
	 */
	boolean isIRIForEdgeLabel( Node iri );

	/**
	 * Applies the inverse of the edge label mapping to the given IRI
	 * and, thus, returns the edge label that is mapped to the given IRI.
	 *
	 * If the given IRI is not in the image of the edge label mapping (in
	 * which case {@link #isIRIForEdgeLabel(Node)} returns false), this
	 * method throws an {@link IllegalArgumentException}.
	 */
	String getEdgeLabelForIRI( Node iri );

	/**
	 * Applies the property name mapping to the given property name and, thus,
	 * returns the IRI (in the form of a Jena {@link Node} object) that the
	 * property name is mapped to.
	 */
	Node getIRIForPropertyName( String propertyName );

	/**
	 * Returns true if the given IRI is in the image of the property name mapping
	 * and, thus, may be returned by {@link #getIRIForPropertyName(String)} for
	 * some property name.
	 */
	boolean isIRIForPropertyName( Node iri );

	/**
	 * Applies the inverse of the property name mapping to the given IRI
	 * and, thus, returns the property name that is mapped to the given IRI.
	 *
	 * If the given IRI is not in the image of the property name mapping (in
	 * which case {@link #isIRIForPropertyName(Node)} returns false), this
	 * method throws an {@link IllegalArgumentException}.
	 */
	String getPropertyNameForIRI( Node iri );

	/**
	 * Returns the IRI (in the form of a Jena {@link Node} object) to be
	 * used in the predicate position of any RDF triple that captures
	 * information about the label of some LPG node.
	 */
	Node getLabelPredicate();
}
