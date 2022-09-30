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
import org.apache.jena.sparql.engine.binding.BindingBuilder;
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
	
	public EntityMappingImpl ( final Map<Node, Set<Node>> g2l, final Map<Node, Set<Node>> l2g) {
		g2lMap.putAll(g2l);
		l2gMap.putAll(l2g);
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

	private Set<SolutionMapping> applyMapToSolutionMapping(final SolutionMapping sm, final Map<Node, Set<Node>> translationMap ) {

		final Binding binding = sm.asJenaBinding();
		
		// If the given solution mapping is the empty mapping, simply return it unchanged.
		if ( binding.isEmpty() ) {
			return Collections.singleton(sm);
		}

		final Set<Map<Var,Node>> cartesianProduct = new HashSet<>();
		cartesianProduct.add(new HashMap<Var,Node>());
		final Iterator<Var> it = binding.vars();
		
		while (it.hasNext()) {
			final Var var = it.next();
			final Node node = binding.get(var);
			if (node.isURI()) {
				final Set<Node> mappedNodes = translationMap.get(node);
				if (mappedNodes == null) { // Local/global Node not different from global/local. 
					for(Map<Var,Node> map : cartesianProduct) { // Multiply by one by adding the pair to all existing combinations.
						map.put(var, node);
					}
				} else { // Local/global different from global/local exists, use that.
					final Set<Map<Var,Node>> newCartesianProducts = new HashSet<>();
					for(Map<Var,Node> map : cartesianProduct) {
						final Iterator<Node> nodeIt = mappedNodes.iterator();
						final Node newNode = nodeIt.next(); // Add the first of the combinations to each map.
						map.put(var, newNode);
						
						while (nodeIt.hasNext()) { // Add the rest of the multiplications from 2 thru n, if such exists.
							final Node anotherNewNode = nodeIt.next();
							final Map<Var,Node> newMap = new HashMap<>(map);
							newMap.replace(var, anotherNewNode);
							newCartesianProducts.add(newMap);
						}
					}
					cartesianProduct.addAll(newCartesianProducts); // Merge the two to create the next step cartesian product.
				}
			} else {
				for(Map<Var,Node> map : cartesianProduct) { // Multiply by one by adding the pair to all existing combinations.
					map.put(var, node);
				}
			}
		}

		final Set<SolutionMapping> newMappings = new HashSet<SolutionMapping>();
		
		for (Map<Var,Node> map : cartesianProduct) { // For each entry in the product, create a builder, build the binding, and make it a solution mapping.
			final BindingBuilder builder = BindingBuilder.create();
			map.forEach((var,node) -> builder.add(var, node));
			final Binding newBinding = builder.build();
			final SolutionMapping newMapping = new SolutionMappingImpl(newBinding);
			newMappings.add(newMapping);
		}
		
		return newMappings;
	}
	
	@Override
	public Set<SolutionMapping> applyToSolutionMapping( final SolutionMapping sm ) {
		return applyMapToSolutionMapping(sm,g2lMap);
	}

	@Override
	public Set<SolutionMapping> applyInverseToSolutionMapping( final SolutionMapping sm ) {
		return applyMapToSolutionMapping(sm,l2gMap);
	}

}
