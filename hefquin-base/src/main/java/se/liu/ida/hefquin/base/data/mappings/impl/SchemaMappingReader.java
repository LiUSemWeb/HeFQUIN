package se.liu.ida.hefquin.base.data.mappings.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import se.liu.ida.hefquin.base.data.mappings.TermMapping;

public class SchemaMappingReader
{
	/**
	 * Parses the given RDF description of a schema mapping into a {@link Map}
	 * in which every global term of the schema mapping is a key that is
	 * associated with a set of all {@link TermMapping}s that have the
	 * global term as their {@link TermMapping#getGlobalTerm()}.
	 */
	public static Map<Node, Set<TermMapping>> read( final Graph mappingDescription ) {
		final Map<Node, Set<TermMapping>> g2lMap = new HashMap<>();

		// iterate over all triples of the given RDF graph
		final Iterator<Triple> it = mappingDescription.find();
		while ( it.hasNext() ) {
			final Triple triple = it.next();
			final Node predicate = triple.getPredicate();

			final TermMapping tm;
			if (    predicate.equals(OWL.equivalentClass.asNode())
			     || predicate.equals(OWL.equivalentProperty.asNode())
			     || predicate.equals(RDFS.subClassOf.asNode())
			     || predicate.equals(RDFS.subPropertyOf.asNode()) ) {
				tm = new TermMappingImpl( predicate, triple.getObject(), triple.getSubject() );
			}
			else if ( predicate.equals(OWL.unionOf.asNode()) ) {
				final Set<Node> localTerms = new HashSet<>();
				collectAllListElements( mappingDescription, triple.getObject(), localTerms );

				tm = new TermMappingImpl( predicate, triple.getSubject(), localTerms );
			}
			else {
				tm = null;
			}

			// add the new term mapping (if any) to the g2lMap
			if ( tm != null ) {
				final Node globalTerm = tm.getGlobalTerm();
				Set<TermMapping> g2lEntry = g2lMap.get(globalTerm);

				if ( g2lEntry == null ) {
					g2lEntry = new HashSet<>();
					g2lMap.put(globalTerm, g2lEntry);
				}

				g2lEntry.add(tm);
			}
		}

		return g2lMap;
	}

	protected static void collectAllListElements( final Graph g, final Node l, final Set<Node> sink ) {
		final Iterator<Triple> itFirst = g.find( l, RDF.first.asNode(), Node.ANY );
		while ( itFirst.hasNext() ) {
			final Node o = itFirst.next().getObject();
			if ( o.isURI() ) {
				sink.add(o);
			}
		}

		final Iterator<Triple> itRest = g.find( l, RDF.rest.asNode(), Node.ANY );
		while ( itRest.hasNext() ) {
			final Node o = itRest.next().getObject();
			if ( ! RDF.nil.asNode().equals(l) ) {
				collectAllListElements(g, o, sink);
			}
		}
	}

}
