package se.liu.ida.hefquin.federation.wrappers;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;

/**
 * This interface captures any type of wrapper that can be used to
 * request data from the federation member that it wraps and, then,
 * convert this data into RDF.
 */
public interface MaterializingWrapper extends Wrapper
{
	/**
	 * Returns the RDF representation of the data obtained by performing
	 * a request to the wrapped federation member using the given list of
	 * arguments for the request.
	 * <p>
	 * A SPARQL graph pattern can be passed as an optional parameter to let
	 * the wrapper know which pattern is intended to be evaluated over the
	 * returned data. Some wrapper implementations may use this pattern to
	 * reduce their effort of converting the retrieved data into RDF by
	 * considering only the conversion rules that may produce RDF triples
	 * relevant to the given pattern. Wrapper implementations that do so
	 * have to guarantee that this does not have any effect on the result
	 * of evaluating the given pattern over the returned RDF triples.
	 * 
	 * @param args - arguments to be used for the request
	 * @param pattern - the pattern that is intended to be evaluated over
	 *                  the returned RDF data; may be {@code null}
	 * @return an RDF graph that represents the data obtained by the
	 *         request in the form of RDF triples
	 */
	Graph obtainData( List<Node> args, SPARQLGraphPattern pattern );
}
