package se.liu.ida.hefquin.engine.data.mappings.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.OWL2;

public class EntityMappingReader
{
	/**
	 * Parses the given RDF description of an entity mapping into
	 * a {@link Map} in which the global terms of the entity mapping
	 * are the keys, each of them associated with a set of its
	 * corresponding local terms.
	 *
	 * Assumes that the subject of every owl:sameAs statement in the
	 * given RDF description is a local URI and the object is the
	 * corresponding global URI.
	 */
	public static Map<Node, Set<Node>> read( final Graph mappingDescription ) {
		final Map<Node, Set<Node>> g2lMap = new HashMap<>();

		final Iterator<Triple> it = mappingDescription.find(Node.ANY, OWL2.sameAs.asNode(), Node.ANY);
		while ( it.hasNext() ) {
			final Triple workingTriple = it.next();
			final Node l = workingTriple.getSubject();
			final Node g = workingTriple.getObject();
			if ( ! g.isURI() || ! l.isURI() ) {
				continue;
			}

			final Set<Node> locals = g2lMap.get(g);
			if ( locals != null ) {
				// There is already at least one local URI for this global URI,
				// We don't want to override it, so we'll add the new one to the set.
				locals.add(l);
			}
			else {
				// g2lmap doesn't contain any local URI for this global yet
				final Set<Node> newLocalSet = new HashSet<>();
				g2lMap.put(g, newLocalSet);
				newLocalSet.add(l);
			}
		}

		return g2lMap;
	}

}
