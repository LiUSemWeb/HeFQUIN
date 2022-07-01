package se.liu.ida.hefquin.engine.data.mappings.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.OWL2;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.mappings.EntityMapping;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class EntityMappingImpl implements EntityMapping
{
	protected final Map<Node, Set<Node>> g2lMap = new HashMap<>();
	protected final Map<Node, Set<Node>> l2gMap = new HashMap<>();

	public EntityMappingImpl( final Graph mappingDescription ) {
		parseMappingDescription(mappingDescription);
	}

	protected void parseMappingDescription( final Graph mappingDescription ) {
		// populate both g2lMap and l2gMap based on the owl:sameAs
		// statements in the given RDF graph; assume the the subject of
		// any owl:sameAs statement is a global URI and the object is the
		// corresponding local URI
		final Iterator<Triple> it = mappingDescription.find(Node.ANY, OWL2.sameAs.asNode(), Node.ANY);
		while ( it.hasNext() ) {
			if(g2lMap.containsKey(it.next().getSubject())) {
				// There is already at least one local URI for this global URI,
				// We don't want to override it, so we'll add the new one to the set.
				final Set<Node> locals = g2lMap.get(it.next().getSubject());
				locals.add(it.next().getObject());
				g2lMap.replace(it.next().getSubject(),locals);
			} else {
				// g2lmap doesn't contain any local URI for this global yet
				final Set<Node> locals = new HashSet<Node>();
				locals.add(it.next().getObject());
				g2lMap.put(it.next().getSubject(),locals);
			}
			
			// The same for l2g.
			if(l2gMap.containsKey(it.next().getObject())) {
				final Set<Node> globals = l2gMap.get(it.next().getObject());
				globals.add(it.next().getSubject());
				l2gMap.replace(it.next().getObject(),globals);
			} else {
				final Set<Node> globals = new HashSet<Node>();
				globals.add(it.next().getSubject());
				l2gMap.put(it.next().getObject(),globals);
			}
		}
	}

	@Override
	public Set<TriplePattern> applyToTriplePattern( final TriplePattern tp ) {
		// TODO: implement this function
		return Collections.singleton(tp);
	}

	@Override
	public Set<SolutionMapping> applyToSolutionMapping( final SolutionMapping sm ) {
		// TODO: implement this function
		return Collections.singleton(sm);
	}

	@Override
	public Set<SolutionMapping> applyInverseToSolutionMapping( final SolutionMapping sm ) {
		// TODO: implement this function
		return Collections.singleton(sm);
	}

}
