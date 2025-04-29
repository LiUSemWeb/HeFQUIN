package se.liu.ida.hefquin.jenaext.graph;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.Vars;

/**
 * This class provides useful functionality
 * related to Jena's {@link Triple} class.
 */
public class TripleUtils
{
	/**
	 * Assuming the given {@link Triple} is a triple pattern, this function
	 * returns the number of times any variable is mentioned in this triple
	 * pattern. If the same variable is mentioned multiple times, then each
	 * of these mentions is counted.
	 */
	public static int getNumberOfVarMentions( final Triple tp ) {
		int n = 0;
		if ( tp.getSubject().isVariable() )   { n += 1; }
		if ( tp.getPredicate().isVariable() ) { n += 1; }
		if ( tp.getObject().isVariable() )    { n += 1; }
		return n;
	}

	/**
	 * Assuming the given {@link Triple} is a triple pattern, this function
	 * returns the number of times any RDF term is mentioned in this triple
	 * pattern. If the same term is mentioned multiple times, then each of
	 * these mentions is counted.
	 */
	public static int getNumberOfTermMentions( final Triple tp ) {
		int n = 0;
		if ( ! tp.getSubject().isVariable() )   { n += 1; }
		if ( ! tp.getPredicate().isVariable() ) { n += 1; }
		if ( ! tp.getObject().isVariable() )    { n += 1; }
		return n;
	}

	/**
	 * Assuming the given {@link Triple} is a triple pattern, this function
	 * returns the set of variables contained in this triple pattern.
	 */
	public static Set<Var> getVariablesInPattern( final Triple tp ) {
		final Set<Var> result = new HashSet<>();
		Vars.addVarsFromTriple( result, tp );
		return result;
	}

}
