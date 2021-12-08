package se.liu.ida.hefquin.engine.query.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.Vars;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementPathBlock;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class QueryPatternUtils
{
	public static BGP createBGP( final BasicPattern pattern ) {
		final Set<TriplePattern> tps = new HashSet<>();
		final Iterator<Triple> it = pattern.iterator();
		while ( it.hasNext() ) {
			tps.add( new TriplePatternImpl(it.next()) );
		}
		return new BGPImpl(tps);
	}

	/**
	 * Assumes that the given {@link PathBlock} does not contain property path
	 * patterns (but only triple patterns). If it does, this methods throws an
	 * {@link IllegalArgumentException}.
	 */
	public static BGP createBGP( final PathBlock pattern ) {
		final Set<TriplePattern> tps = new HashSet<>();
		final Iterator<TriplePath> it = pattern.iterator();
		while ( it.hasNext() ) {
			final TriplePath tp = it.next();
			if ( ! tp.isTriple() ) {
				throw new IllegalArgumentException( "the given PathBlock contains a property path pattern (" + tp.toString() + ")" );
			}
			tps.add( new TriplePatternImpl(tp.asTriple()) );
		}
		return new BGPImpl(tps);
	}

	public static Element convertToJenaElement( final SPARQLGraphPattern p ) {
		if ( p instanceof TriplePattern ) {
			final TriplePattern tp = (TriplePattern) p;

			final ElementPathBlock e = new ElementPathBlock();
			e.addTriple( tp.asJenaTriple() );
			return e;
		}
		else if ( p instanceof BGP ) {
			final BGP bgp = (BGP) p;

			final ElementPathBlock e = new ElementPathBlock();
			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				e.addTriple( tp.asJenaTriple() );
			}
			return e;
		}
		else {
			return OpAsQuery.asQuery( p.asJenaOp() ).getQueryPattern();
		}
	}

	/**
	 * Returns the set of all variables that occur in the given graph pattern.
	 *
	 * If the given pattern is a {@link TriplePattern}, this function returns
	 * the result of {@link #getVariablesInPattern(TriplePattern)}. Similarly,
	 * if the given pattern is a {@link BGP}, this function returns the result
	 * of {@link #getVariablesInPattern(BGP)}.
	 */
	public static Set<Var> getVariablesInPattern( final SPARQLGraphPattern queryPattern ) {
		if ( queryPattern.asJenaOp() instanceof Triple) {
			return getVariablesInPattern( new TriplePatternImpl( (Triple) queryPattern.asJenaOp()) );
		}
		if ( queryPattern.asJenaOp() instanceof OpBGP) {
			return getVariablesInPattern( createBGP(((OpBGP) queryPattern.asJenaOp()).getPattern()) );
		}
		else {
			throw new UnsupportedOperationException("Getting the variables from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + queryPattern.asJenaOp().getClass().getName() + ").");
		}
	}

	public static Set<Var> getVariablesInPattern( final TriplePattern tp ) {
		final Set<Var> result = new HashSet<>();
		Vars.addVarsFromTriple( result, tp.asJenaTriple() );
		return result;
	}

	public static Set<Var> getVariablesInPattern( final BGP bgp ) {
		final Set<Var> result = new HashSet<>();
		for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
			result.addAll( getVariablesInPattern(tp) );
		}
		return result;
	}

	/**
	 * Returns the number of elements of the given triple pattern that are variables.
	 */
	public static int getNumberOfVarOccurrences( final TriplePattern tp ) {
		final Triple jenaTP = tp.asJenaTriple();
		int n = 0;
		if ( jenaTP.getSubject().isVariable() )   { n += 1; }
		if ( jenaTP.getPredicate().isVariable() ) { n += 1; }
		if ( jenaTP.getObject().isVariable() )    { n += 1; }
		return n;
	}

	/**
	 * Returns the number of elements of the given triple pattern that are RDF terms.
	 */
	public static int getNumberOfTermOccurrences( final TriplePattern tp ) {
		final Triple jenaTP = tp.asJenaTriple();
		int n = 0;
		if ( ! jenaTP.getSubject().isVariable() )   { n += 1; }
		if ( ! jenaTP.getPredicate().isVariable() ) { n += 1; }
		if ( ! jenaTP.getObject().isVariable() )    { n += 1; }
		return n;
	}

	/**
	 * Returns the number of elements of the given triple pattern that are blank nodes.
	 */
	public static int getNumberOfBNodeOccurrences( final TriplePattern tp ) {
		final Triple jenaTP = tp.asJenaTriple();
		int n = 0;
		if ( ! jenaTP.getSubject().isBlank() )   { n += 1; }
		if ( ! jenaTP.getPredicate().isBlank() ) { n += 1; }
		if ( ! jenaTP.getObject().isBlank() )    { n += 1; }
		return n;
	}

	public static int getNumberOfVarOccurrences( final BGP bgp ) {
		int n = 0;
		for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
			n += getNumberOfVarOccurrences(tp);
		}
		return n;
	}

	public static int getNumberOfTermOccurrences( final BGP bgp ) {
		return 3 * bgp.getTriplePatterns().size() - getNumberOfVarOccurrences(bgp);
	}

	public static int getNumberOfBNodeOccurrences( final BGP bgp ) {
		int n = 0;
		for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
			n += getNumberOfBNodeOccurrences(tp);
		}
		return n;
	}

	/**
	 * Returns the number of occurrences of variables in the given graph
	 * pattern. If the same variable occurs multiple times, each occurrence
	 * is counted.
	 *
	 * If the given pattern is a {@link TriplePattern}, this function returns
	 * the result of {@link #getNumberOfVarOccurrences(TriplePattern)}.
	 * Similarly, if the given pattern is a {@link BGP}, this function
	 * returns the result of {@link #getNumberOfVarOccurrences(BGP)}.
	 */
	public static int getNumberOfVarOccurrences( final SPARQLGraphPattern queryPattern ) {
		if ( queryPattern.asJenaOp() instanceof Triple) {
			return getNumberOfVarOccurrences( new TriplePatternImpl( (Triple) queryPattern.asJenaOp()) );
		}
		else if ( queryPattern.asJenaOp() instanceof OpBGP) {
			return getNumberOfVarOccurrences( createBGP(((OpBGP) queryPattern.asJenaOp()).getPattern()) );
		}
		else {
			throw new UnsupportedOperationException("Getting the number of elements (variables) from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + queryPattern.asJenaOp().getClass().getName() + ").");
		}
	}

	/**
	 * Returns the number of occurrences of RDF terms in the given graph
	 * pattern. If the same term occurs multiple times, each occurrence
	 * is counted.
	 *
	 * If the given pattern is a {@link TriplePattern}, this function returns
	 * the result of {@link #getNumberOfTermOccurrences(TriplePattern)}.
	 * Similarly, if the given pattern is a {@link BGP}, this function
	 * returns the result of {@link #getNumberOfTermOccurrences(BGP)}.
	 */
	public static int getNumberOfTermOccurrences( final SPARQLGraphPattern queryPattern ) {
		if ( queryPattern.asJenaOp() instanceof Triple) {
			return getNumberOfTermOccurrences( new TriplePatternImpl( (Triple) queryPattern.asJenaOp())  );
		}
		else if ( queryPattern.asJenaOp() instanceof OpBGP) {
			return getNumberOfTermOccurrences( createBGP(((OpBGP) queryPattern.asJenaOp()).getPattern()) );
		}
		else {
			throw new UnsupportedOperationException("Getting the number of elements (RDF terms) from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + queryPattern.asJenaOp().getClass().getName() + ").");
		}
	}

	public static ExpectedVariables getExpectedVariablesInPattern( final SPARQLGraphPattern pattern ) {
		if ( pattern instanceof TriplePattern ) {
			return new ExpectedVariables() {
				@Override public Set<Var> getPossibleVariables() {
					return Collections.emptySet();
				}

				@Override public Set<Var> getCertainVariables() {
					return getVariablesInPattern( (TriplePattern) pattern );
				}
			};
		}
		else if ( pattern instanceof BGP ) {
			return new ExpectedVariables() {
				@Override public Set<Var> getPossibleVariables() {
					return Collections.emptySet();
				}

				@Override public Set<Var> getCertainVariables() {
					return getVariablesInPattern( (BGP) pattern );
				}
			};
		}
		else {
			final Set<Var> certainVars = OpVars.fixedVars( pattern.asJenaOp() );
			Set<Var> possibleVars = OpVars.visibleVars( pattern.asJenaOp() );
			possibleVars.removeAll(certainVars);

			return new ExpectedVariables() {
				@Override public Set<Var> getPossibleVariables() { return possibleVars; }
				@Override public Set<Var> getCertainVariables() { return certainVars; }
			};
		}
	}

	public static ExpectedVariables getExpectedVariablesInQuery( final SPARQLQuery query ) {
		final Set<Var> vars = new HashSet<>( query.asJenaQuery().getProjectVars() );

		return new ExpectedVariables() {
			@Override public Set<Var> getPossibleVariables() { return vars; }
			@Override public Set<Var> getCertainVariables() { return Collections.emptySet(); }
		};
	}

	public static SPARQLGraphPattern applySolMapToGraphPattern(
			final SolutionMapping sm,
			final SPARQLGraphPattern pattern )
					throws VariableByBlankNodeSubstitutionException
	{
		// TODO
		if ( pattern instanceof TriplePattern )
			return applySolMapToTriplePattern( sm, (TriplePattern) pattern );
		else if ( pattern instanceof BGP )
			return applySolMapToBGP( sm, (BGP) pattern );
		else
			throw new UnsupportedOperationException("TODO");
	}

	public static BGP applySolMapToBGP( final SolutionMapping sm, final BGP bgp )
			throws VariableByBlankNodeSubstitutionException
	{
		final Set<TriplePattern> tps = new HashSet<>();
		boolean unchanged = true;
		for ( final TriplePattern tp : ((BGPImpl)bgp).getTriplePatterns() ) {
			final TriplePattern tp2 = applySolMapToTriplePattern(sm, tp);
			tps.add(tp2);
			if ( tp2 != tp ) {
				unchanged = false;
			}
		}

		if ( unchanged ) {
			return bgp;
		} else {
			return new BGPImpl(tps);
		}
	}

	/**
	 * Attention, this function throws an exception in all cases in which one
	 * of the variables of the triple pattern would be replaced by a blank node.
	 */
	public static TriplePattern applySolMapToTriplePattern( final SolutionMapping sm,
	                                                        final TriplePattern tp )
			throws VariableByBlankNodeSubstitutionException
	{
		final Binding b = sm.asJenaBinding();
		boolean unchanged = true;

		Node s = tp.asJenaTriple().getSubject();
		if ( Var.isVar(s) ) {
			final Var var = Var.alloc(s);
			if ( b.contains(var) ) {
				s = b.get(var);
				unchanged = false;
				if ( s.isBlank() ) {
					throw new VariableByBlankNodeSubstitutionException();
				}
			}
		}

		Node p = tp.asJenaTriple().getPredicate();
		if ( Var.isVar(p) ) {
			final Var var = Var.alloc(p);
			if ( b.contains(var) ) {
				p = b.get(var);
				unchanged = false;
				if ( p.isBlank() ) {
					throw new VariableByBlankNodeSubstitutionException();
				}
			}
		}

		Node o = tp.asJenaTriple().getObject();
		if ( Var.isVar(o) ) {
			final Var var = Var.alloc(o);
			if ( b.contains(var) ) {
				o = b.get(var);
				unchanged = false;
				if ( o.isBlank() ) {
					throw new VariableByBlankNodeSubstitutionException();
				}
			}
		}

		if ( unchanged ) {
			return tp;
		} else {
			return new TriplePatternImpl(s,p,o);
		}
	}


	public static class VariableByBlankNodeSubstitutionException extends Exception
	{
		private static final long serialVersionUID = 3285677866147999456L;	
	}

}
