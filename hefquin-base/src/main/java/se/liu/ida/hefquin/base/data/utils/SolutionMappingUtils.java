package se.liu.ida.hefquin.base.data.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.*;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.FmtUtils;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;

public class SolutionMappingUtils
{
	/**
	 * Creates an empty solution mapping.
	 */
	public static SolutionMapping createSolutionMapping()
	{
		final Binding b = BindingFactory.binding();
		return new SolutionMappingImpl(b);
	}

	/**
	 * Creates a solution mapping based on the given {@link QuerySolution}.
	 */
	@SuppressWarnings("unused")
	public static SolutionMapping createSolutionMapping( final QuerySolution s )
	{
		final Binding b = BindingLib.asBinding(s);
		if ( true ) {
			final BindingBuilder bb = BindingBuilder.create();
			b.forEach( (var,node) -> bb.add(var, node) );
			return new SolutionMappingImpl( bb.build() );
		}
		else
			return new SolutionMappingImpl(b);
	}

	/**
	 * Creates a solution mapping in which the given variable
	 * is mapped to the given node.
	 */
	public static SolutionMapping createSolutionMapping(
			final Var var, final Node node )
	{
		final Binding b = BindingFactory.binding(var, node);
		return new SolutionMappingImpl(b);
	}

	/**
	 * Creates a solution mapping in which the given variables
	 * are mapped to the given nodes, respectively.
	 */
	public static SolutionMapping createSolutionMapping(
			final Var var1, final Node node1,
			final Var var2, final Node node2 )
	{
		final BindingBuilder b = BindingBuilder.create();
		b.add(var1, node1);
		b.add(var2, node2);
		return new SolutionMappingImpl(b.build());
	}

	/**
	 * Creates a solution mapping in which the given variables
	 * are mapped to the given nodes, respectively.
	 */
	public static SolutionMapping createSolutionMapping(
			final Var var1, final Node node1,
			final Var var2, final Node node2,
			final Var var3, final Node node3 )
	{
		final BindingBuilder b = BindingBuilder.create();
		b.add(var1, node1);
		b.add(var2, node2);
		b.add(var3, node3);
		return new SolutionMappingImpl(b.build());
	}

	/**
	 * Returns true if the given solution mappings are equivalent; that
	 * is, if they are defined for the exact same set of variables and
	 * they are compatible.
	 */
	public static boolean equals( final SolutionMapping m1, final SolutionMapping m2 ) {
		return BindingLib.equals( m1.asJenaBinding(), m2.asJenaBinding() );
	}

	/**
	 * Returns true if the given set of solution mappings are equivalent; that
	 * is, if they contain the same number of solution mappings and for each such
	 * solution mapping there is an equivalent solution mapping in the respective
	 * other set.
	 */
	public static boolean equals( final Set<SolutionMapping> s1, final Set<SolutionMapping> s2 ) {
		if ( s1 == s2 )
			return true;
		else if ( s1.size() != s2.size() )
			return false;
		else
			return s1.containsAll(s2);
	}

	/**
	 * Returns true if the given set of solution mappings are equivalent; that
	 * is, if they contain the same number of solution mappings and for each such
	 * solution mapping there is an equivalent solution mapping in the respective
	 * other set.
	 */
	public static boolean equalSets( final Set<Binding> s1, final Set<Binding> s2 ) {
		if ( s1 == s2 )
			return true;
		else if ( s1.size() != s2.size() )
			return false;
		else
			return s1.containsAll(s2);
	}

	/**
	 * Returns true if the given solution mappings are compatible.
	 */
	public static boolean compatible( final SolutionMapping m1, final SolutionMapping m2 ) {
		final Binding b1 = m1.asJenaBinding();
		final Binding b2 = m2.asJenaBinding();

		final Iterator<Var> it = b1.vars();
		while ( it.hasNext() ) {
			final Var v = it.next();
			if ( b2.contains(v) && ! b2.get(v).sameValueAs(b1.get(v)) )
				return false;
		}

		return true;
	}

	/**
	 * Returns <code>true</code> if the first solution mapping, b1, is
	 * included in the second solution mapping, b2, where we say that
	 * 'b1 is included in b2' if the variables in b1 are a proper subset
	 * of the variables in b2 and the two solution mappings are compatible.
	 * In other words, b1 and b2 are the same for the subset of variables
	 * for which they both have bindings and, additionally, b2 has bindings
	 * for additional variables.
	 */
	public static boolean includedIn( final Binding b1, final Binding b2 ) {
		// First check: b1 can be included in b2 only if b1 has fewer
		// variables than b2. If that is not the case, we can immediately
		// conclude that b1 is not included in b2.
		if ( b1.size() >= b2.size() ) return false;

		// Now the main check: We iterate over the variables bound in b1 and,
		// for each of these variables, we check that
		// (a) the variable is also bound in b2 and
		// (b) both solution mappings have the same term for the variable.
		final Iterator<Var> it = b1.vars();
		while ( it.hasNext() ) {
			final Var var = it.next();
			// check (a)
			if ( ! b2.contains(var) ) return false;
			// check (b)
			if ( ! b1.get(var).equals(b2.get(var)) ) return false;
		}

		return true;
	}

	/**
	 * Merges the given solution mappings into one, assuming
	 * that the given solution mappings are compatible.
	 */
	public static SolutionMapping merge( final SolutionMapping m1, final SolutionMapping m2 ) {
		final Binding b1 = m1.asJenaBinding();
		final Binding b2 = m2.asJenaBinding();
		return new SolutionMappingImpl( BindingLib.merge(b1,b2) );
	}

	/**
	 * Performs a nested-loop join between two Solution Mapping iterables.
	 *
	 * If you do not need the join result materialized (as done by this function),
	 * it is better to use {@link JoiningIterableForSolMaps} instead (or the
	 * iterator version: {@link JoiningIteratorForSolMaps}).
	 */
	public static Set<SolutionMapping> nestedLoopJoin( final Iterable<SolutionMapping> i1,
													   final Iterable<SolutionMapping> i2) {
		final Set<SolutionMapping> result = new HashSet<>();
		for ( final SolutionMapping m1 : i1 ) {
			for ( final SolutionMapping m2 : i2 ) {
				if ( compatible(m1, m2) )
					result.add(merge(m1, m2));
			}
		}
		return result;
	}

	/**
	 * Applies the given vocabulary mapping to each of the solution mappings
	 * of the given iterable (translating them from the global vocabulary to
	 * the local vocabulary), collects the resulting solution mappings in a
	 * list, and returns this list in the end.
	 *
	 * Attention: While this function materializes the complete list of all the
	 * resulting solution mappings, for use cases in which such a materialization
	 * is not necessary, use {@link RewritingIterableForSolMapsG2L} or
	 * {@link RewritingIteratorForSolMapsG2L} instead.
	 */
	public static List<SolutionMapping> applyVocabularyMappingG2L( final Iterable<SolutionMapping> it,
	                                                               final VocabularyMapping vm ) {
		return applyVocabularyMappingG2L( it.iterator(), vm );
	}

	/**
	 * Applies the given vocabulary mapping to each of the solution mappings
	 * of the given iterator (translating them from the global vocabulary to
	 * the local vocabulary), collects the resulting solution mappings in a
	 * list, and returns this list in the end.
	 *
	 * Attention: While this function materializes the complete list of all the
	 * resulting solution mappings, for use cases in which such a materialization
	 * is not necessary, use {@link RewritingIterableForSolMapsG2L} or
	 * {@link RewritingIteratorForSolMapsG2L} instead.
	 */
	public static List<SolutionMapping> applyVocabularyMappingG2L( final Iterator<SolutionMapping> it,
	                                                               final VocabularyMapping vm ) {
		final List<SolutionMapping> result = new ArrayList<>();
		while ( it.hasNext() ) {
			final SolutionMapping sm = it.next();
			result.addAll( vm.translateSolutionMappingFromGlobal(sm) );
		}
		return result;
	}

	/**
	 * Applies the given vocabulary mapping to each of the solution mappings
	 * of the given iterable (translating them from the local vocabulary to
	 * the global vocabulary), collects the resulting solution mappings in a
	 * list, and returns this list in the end.
	 *
	 * Attention: While this function materializes the complete list of all the
	 * resulting solution mappings, for use cases in which such a materialization
	 * is not necessary, use {@link RewritingIterableForSolMapsL2G} or
	 * {@link RewritingIteratorForSolMapsL2G} instead.
	 */
	public static List<SolutionMapping> applyVocabularyMappingL2G( final Iterable<SolutionMapping> it,
	                                                               final VocabularyMapping vm ) {
		return applyVocabularyMappingL2G( it.iterator(), vm );
	}

	/**
	 * Applies the given vocabulary mapping to each of the solution mappings
	 * of the given iterator (translating them from the local vocabulary to
	 * the global vocabulary), collects the resulting solution mappings in a
	 * list, and returns this list in the end.
	 *
	 * Attention: While this function materializes the complete list of all the
	 * resulting solution mappings, for use cases in which such a materialization
	 * is not necessary, use {@link RewritingIterableForSolMapsL2G} or
	 * {@link RewritingIteratorForSolMapsL2G} instead.
	 */
	public static List<SolutionMapping> applyVocabularyMappingL2G( final Iterator<SolutionMapping> it,
	                                                               final VocabularyMapping vm ) {
		final List<SolutionMapping> result = new ArrayList<>();
		while ( it.hasNext() ) {
			final SolutionMapping sm = it.next();
			result.addAll( vm.translateSolutionMapping(sm) );
		}
		return result;
	}

	/**
	 * Restricts the given solution mapping to the given set of variables.
	 * Hence, the returned solution mapping will be compatible with the
	 * solution mapping given as input, but it will be defined only for
	 * the variables that are in the intersection of the given set of
	 * variables and the set of variables for which the given solution
	 * mapping is defined.
	 */
	public static Binding restrict( final Binding input, final Collection<Var> vars ) {
		final Iterator<Var> it = input.vars();
		final BindingBuilder output = BindingBuilder.create();

		while ( it.hasNext() ) {
			final Var var = it.next();
			if ( vars.contains(var) ) {
				output.add( var, input.get(var) );
			}
		}
		return output.build();
	}
	
	/**
	 * Restricts the given solution mapping to the given set of variables.
	 * Hence, the returned solution mapping will be compatible with the
	 * solution mapping given as input, but it will be defined only for
	 * the variables that are in the intersection of the given set of
	 * variables and the set of variables for which the given solution
	 * mapping is defined.
	 */
	public static SolutionMapping restrict( final SolutionMapping sm, final Collection<Var> vars ) {
		return new SolutionMappingImpl(restrict(sm.asJenaBinding(), vars));
	}

	/**
	 * Returns true if the given solution mapping
	 * binds any of its variables to a blank node.
	 */
	public static boolean containsBlankNodes( final SolutionMapping sm ) {
		return containsBlankNodes( sm.asJenaBinding() );
	}

	/**
	 * Returns true if the given solution mapping
	 * binds any of its variables to a blank node.
	 */
	public static boolean containsBlankNodes( final Binding b ) {
		final Iterator<Var> it = b.vars();
		while ( it.hasNext() ) {
			if ( b.get(it.next()).isBlank() ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a set containing all the variables that have a
	 * binding in at least one of the given solution mappings.
	 */
	public static Set<Var> getAllMentionedVariables( final Iterable<Binding> solmaps ) {
		final Set<Var> vars = new HashSet<>();
		for ( final Binding sm : solmaps ) {
			final Iterator<Var> it = sm.vars();
			while ( it.hasNext() ) {
				vars.add( it.next() );
			}
		}
		return vars;
	}

	/**
	 * Serializes the given collection of solution mappings as
	 * a string that can be used in a SPARQL VALUES clause.
	 */
	public static String createValuesClause( final Iterable<Binding> solmaps,
	                                         final SerializationContext scxt ) {
		final Set<Var> vars = SolutionMappingUtils.getAllMentionedVariables(solmaps);
		if ( vars.size() == 1 )
			return createValuesClauseShortForm( new ArrayList<>(vars), solmaps, scxt );
		else
			return createValuesClauseLongForm( new ArrayList<>(vars), solmaps, scxt );
	}

	protected static String createValuesClauseShortForm( final List<Var> vars,
	                                                     final Iterable<Binding> solmaps,
	                                                     final SerializationContext scxt ) {
		final StringBuilder b = new StringBuilder();
		b.append("?");
		b.append( vars.iterator().next().getVarName() );
		b.append(" {");
		for ( final Binding sm : solmaps ) {
			appendValuesClauseEntry(b, vars, sm, scxt);
		}
		b.append(" }");
		return b.toString();
	}

	protected static String createValuesClauseLongForm( final List<Var> vars,
	                                                    final Iterable<Binding> solmaps,
	                                                    final SerializationContext scxt ) {
		final StringBuilder b = new StringBuilder();
		b.append("(");
		for ( final Var v : vars ) {
			b.append(" ?");
			b.append( v.getVarName() );
		}
		b.append(" )");

		b.append(" {");
		for ( final Binding sm : solmaps ) {
			b.append(" (");
			appendValuesClauseEntry(b, vars, sm, scxt);
			b.append(" )");
		}
		b.append(" }");
		return b.toString();
	}

	protected static void appendValuesClauseEntry( final StringBuilder b,
	                                               final List<Var> vars,
	                                               final Binding sm,
	                                               final SerializationContext scxt ) {
		for ( final Var v : vars ) {
			b.append(" ");
			final Node n = sm.get(v);
			if ( n == null ) {
				b.append("UNDEF");
			}
			else {
				FmtUtils.stringForNode(b, n, scxt);
			}
		}
	}

}
