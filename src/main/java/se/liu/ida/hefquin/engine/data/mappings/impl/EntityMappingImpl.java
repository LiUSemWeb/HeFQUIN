package se.liu.ida.hefquin.engine.data.mappings.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.OWL2;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.data.mappings.EntityMapping;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

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
			final Triple workingTriple = it.next();
			if ( ! workingTriple.getSubject().isURI() || ! workingTriple.getObject().isURI() ) {
				continue;
			}
			final Set<Node> locals = g2lMap.get(workingTriple.getSubject());
			if(locals != null) {
				// There is already at least one local URI for this global URI,
				// We don't want to override it, so we'll add the new one to the set.
				locals.add(workingTriple.getObject());
			} else {
				// g2lmap doesn't contain any local URI for this global yet
				final Set<Node> localSet = new HashSet<>();
				g2lMap.put( workingTriple.getSubject(), localSet );
				localSet.add(workingTriple.getObject());
			}
			
			// The same for l2g.
			final Set<Node> globals = l2gMap.get(workingTriple.getObject());
			if(globals != null) {
				globals.add(workingTriple.getSubject());
			} else {
				final Set<Node> newGlobalSet = new HashSet<>();
				l2gMap.put( workingTriple.getObject(), newGlobalSet );
				newGlobalSet.add(workingTriple.getSubject());
			}
		}
	}

	@Override
	public Set<TriplePattern> applyToTriplePattern( final TriplePattern tp ) {
		final Set<Triple> workingSet = new HashSet<Triple>();
		if(tp.asJenaTriple().getSubject().isURI()) {
			final Set<Node> localSubjects = g2lMap.get( tp.asJenaTriple().getSubject() );
			if( localSubjects != null ) {
				for (final Node localSubject : localSubjects) {
					final Triple workingTriple = new Triple(localSubject,tp.asJenaTriple().getPredicate(), tp.asJenaTriple().getObject());
					workingSet.add(workingTriple);
				}
			} else {
				workingSet.add(tp.asJenaTriple());
			}
		} else {
			workingSet.add(tp.asJenaTriple());
		}
		final Set<TriplePattern> resultSet = new HashSet<TriplePattern>();
			if(tp.asJenaTriple().getObject().isURI()) {
			final Set<Node> localObjects = g2lMap.get(tp.asJenaTriple().getObject());
			if(g2lMap.containsKey(tp.asJenaTriple().getObject())) {
				for (final Triple workingTriple : workingSet) {
					for (final Node localObject : localObjects) {
						final Triple resultTriple = new Triple(workingTriple.getSubject(), workingTriple.getPredicate(), localObject);
						resultSet.add(new TriplePatternImpl(resultTriple));
					}
				}
				return resultSet;
			} else {
				for (final Triple workingTriple : workingSet ) {
					resultSet.add(new TriplePatternImpl(workingTriple));
				}
				return resultSet;
			}
		} else {
			for (final Triple workingTriple : workingSet ) {
				resultSet.add(new TriplePatternImpl(workingTriple));
			}
			return resultSet;
		}
	}

	@Override
	public Set<SolutionMapping> applyToSolutionMapping( final SolutionMapping sm ) {
		final Set<SolutionMapping> newMappings = new HashSet<SolutionMapping>();
		final Binding binding = sm.asJenaBinding();
		final Iterator<Var> it = binding.vars();
		while (it.hasNext()) {
			final Var var = it.next();
			final Node node = binding.get(var);
			final Set<Node> mappedNodes = g2lMap.get(node);
			if (mappedNodes == null) { // Local Node not different from global. 
				final Binding newBinding = BindingFactory.binding(var,node);
				final SolutionMapping newMapping = new SolutionMappingImpl(newBinding);
				newMappings.add(newMapping);
			} else { // Local different from global exists, use that.
				for (final Node localNode : mappedNodes) {
					final Binding newBinding = BindingFactory.binding(var,localNode);
					final SolutionMapping newMapping = new SolutionMappingImpl(newBinding);
					newMappings.add(newMapping);
				}
			}
		}
		return newMappings;
	}

	@Override
	public Set<SolutionMapping> applyInverseToSolutionMapping( final SolutionMapping sm ) {
		// TODO: implement this function
		return Collections.singleton(sm);
	}

}
