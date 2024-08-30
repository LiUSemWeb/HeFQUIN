package se.liu.ida.hefquin.base.data.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;

public abstract class TestsForSolutionMappingsIterators
{
	/**
	 * Use this method instead of {@link SolutionMappingUtils#createSolutionMapping(Var, Node)}
	 * to make ensure new java objects for the variables and RDF terms.
	 */
	protected SolutionMapping createSolMap( final String varName, final String uriString ) {
		final BindingBuilder b = BindingBuilder.create();
		b.add( Var.alloc(varName), NodeFactory.createURI(uriString) );
		return new SolutionMappingImpl( b.build() );
	}

	/**
	 * Use this method instead of {@link SolutionMappingUtils#createSolutionMapping(Var, Node, Var, Node)}
	 * to make ensure new java objects for the variables and RDF terms.
	 */
	protected SolutionMapping createSolMap( final String varName1, final String uriString1, final String varName2, final String uriString2 ) {
		final BindingBuilder b = BindingBuilder.create();
		b.add( Var.alloc(varName1), NodeFactory.createURI(uriString1) );
		b.add( Var.alloc(varName2), NodeFactory.createURI(uriString2) );
		return new SolutionMappingImpl( b.build() );
	}

}
