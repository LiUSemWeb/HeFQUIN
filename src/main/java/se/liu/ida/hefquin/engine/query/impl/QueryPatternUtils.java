package se.liu.ida.hefquin.engine.query.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.Vars;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementUnion;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
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
		else if (p instanceof SPARQLUnionPattern) {
			final SPARQLUnionPattern up = (SPARQLUnionPattern) p;
			
			final ElementUnion e = new ElementUnion(); // ?
			for ( final SPARQLGraphPattern gp : up.getSubPatterns() ) {
				e.addElement(convertToJenaElement(gp));
			}
			return e;
		}
		else if (p instanceof SPARQLGroupPattern) {
			final SPARQLGroupPattern gp = (SPARQLGroupPattern) p;
			
			final ElementGroup e = new ElementGroup(); // ?
			for ( final SPARQLGraphPattern g : gp.getSubPatterns() ) {
				e.addElement(convertToJenaElement(g));
			}
			return e;
		}
		else if ( p instanceof GenericSPARQLGraphPatternImpl1 ) {
			return ( (GenericSPARQLGraphPatternImpl1) p ).asJenaElement();
		}
		else if ( p instanceof GenericSPARQLGraphPatternImpl2 ) {
			@SuppressWarnings("deprecation")
			final Element jenaElement = ( (GenericSPARQLGraphPatternImpl2) p ).asJenaElement();
			return jenaElement;
		}
		else {
			throw new UnsupportedOperationException( p.getClass().getName() );
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
		if ( queryPattern instanceof TriplePattern ) {
			return getVariablesInPattern( (TriplePattern) queryPattern );
		}
		else if ( queryPattern instanceof BGP ) {
			return getVariablesInPattern( (BGP) queryPattern );
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl1 ) {
			@SuppressWarnings("deprecation")
			final Op jenaOp = ( (GenericSPARQLGraphPatternImpl1) queryPattern ).asJenaOp();
			return getVariablesInPattern(jenaOp);
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl2 ) {
			final Op jenaOp = ( (GenericSPARQLGraphPatternImpl2) queryPattern ).asJenaOp();
			return getVariablesInPattern(jenaOp);
		}
		else {
			throw new UnsupportedOperationException( queryPattern.getClass().getName() );
		}
	}

	public static Set<Var> getVariablesInPattern( final TriplePattern tp ) {
		return getVariablesInPattern( tp.asJenaTriple() );
	}

	public static Set<Var> getVariablesInPattern( final Triple tp ) {
		final Set<Var> result = new HashSet<>();
		Vars.addVarsFromTriple( result, tp );
		return result;
	}

	public static Set<Var> getVariablesInPattern( final BGP bgp ) {
		final Set<Var> result = new HashSet<>();
		for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
			result.addAll( getVariablesInPattern(tp) );
		}
		return result;
	}

	public static Set<Var> getVariablesInPattern( final OpBGP bgp ) {
		final Set<Var> result = new HashSet<>();
		for ( final Triple tp : bgp.getPattern().getList() ) {
			result.addAll( getVariablesInPattern(tp) );
		}
		return result;
	}

	public static Set<Var> getVariablesInPattern( final Op2 op ) {
		final Set<Var> varLeft = getVariablesInPattern( op.getLeft() );
		final Set<Var> varRight = getVariablesInPattern( op.getRight() );
		varLeft.addAll(varRight);
		return varLeft;
	}

	public static Set<Var> getVariablesInPattern( final Op op ) {
		if ( op instanceof OpBGP ) {
			return getVariablesInPattern( (OpBGP) op);
		}
		else if ( op instanceof OpJoin || op instanceof OpUnion ) {
			return getVariablesInPattern( (Op2) op );
		}
		else if ( op instanceof OpService ){
			return getVariablesInPattern( ((Op1) op).getSubOp());
		}
		else {
			throw new UnsupportedOperationException("Getting the variables from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
		}
	}


	/**
	 * Returns the number of elements of the given triple pattern that are variables.
	 */
	public static int getNumberOfVarOccurrences( final TriplePattern tp ) {
		final Triple jenaTP = tp.asJenaTriple();
		return getNumberOfVarOccurrences(jenaTP);
	}

	public static int getNumberOfVarOccurrences( final Triple jenaTP ) {
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

	public static int getNumberOfVarOccurrences( final OpBGP bgp ) {
		int n = 0;
		for ( final Triple tp : bgp.getPattern().getList() ) {
			n += getNumberOfVarOccurrences(tp);
		}
		return n;
	}

	public static int getNumberOfVarOccurrences( final Op2 op ) {
		final int numLeft = getNumberOfVarOccurrences( op.getLeft() );
		final int numRight = getNumberOfVarOccurrences( op.getRight() );

		return numLeft+numRight;
	}

	public static int getNumberOfVarOccurrences( final Op op ) {
		if ( op instanceof OpBGP ) {
			return getNumberOfVarOccurrences( (OpBGP) op);
		}
		else if ( op instanceof OpJoin || op instanceof OpUnion ) {
			return getNumberOfVarOccurrences( (Op2) op );
		}
		else if ( op instanceof OpService ){
			return getNumberOfVarOccurrences( ((Op1) op).getSubOp());
		}
		else {
			throw new UnsupportedOperationException("Getting the number of elements (variables) from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
		}
	}

	public static int getNumberOfTermOccurrences( final BGP bgp ) {
		return 3 * bgp.getTriplePatterns().size() - getNumberOfVarOccurrences(bgp);
	}

	public static int getNumberOfTermOccurrences( final Op2 op ) {
		final int numLeft = getNumberOfTermOccurrences( op.getLeft() );
		final int numRight = getNumberOfTermOccurrences( op.getRight() );

		return numLeft+numRight;
	}

	public static int getNumberOfTermOccurrences( final Op op ) {
		if ( op instanceof OpBGP ) {
			return getNumberOfTermOccurrences( (OpBGP) op);
		}
		else if ( op instanceof OpJoin || op instanceof OpUnion ) {
			return getNumberOfTermOccurrences( (Op2) op );
		}
		else if ( op instanceof OpService ){
			return getNumberOfTermOccurrences( ((Op1) op).getSubOp());
		}
		else {
			throw new UnsupportedOperationException("Getting the number of elements (RDF terms) from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
		}
	}

	public static int getNumberOfTermOccurrences( final OpBGP bgp ) {
		return 3 * bgp.getPattern().getList().size() - getNumberOfVarOccurrences(bgp);
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
		if ( queryPattern instanceof TriplePattern ) {
			return getNumberOfVarOccurrences( (TriplePattern) queryPattern );
		}
		else if ( queryPattern instanceof BGP ) {
			return getNumberOfVarOccurrences( (BGP) queryPattern );
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl1 ) {
			@SuppressWarnings("deprecation")
			final Op jenaOp = ( (GenericSPARQLGraphPatternImpl1) queryPattern ).asJenaOp();
			return getNumberOfVarOccurrences(jenaOp);
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl2 ) {
			final Op jenaOp = ( (GenericSPARQLGraphPatternImpl2) queryPattern ).asJenaOp();
			return getNumberOfVarOccurrences(jenaOp);
		}
		else {
			throw new UnsupportedOperationException( queryPattern.getClass().getName() );
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
		if ( queryPattern instanceof TriplePattern ) {
			return getNumberOfTermOccurrences( (TriplePattern) queryPattern  );
		}
		else if ( queryPattern instanceof BGP ) {
			return getNumberOfTermOccurrences( (BGP) queryPattern  );
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl1 ) {
			@SuppressWarnings("deprecation")
			final Op jenaOp = ( (GenericSPARQLGraphPatternImpl1) queryPattern ).asJenaOp();
			return getNumberOfTermOccurrences(jenaOp);
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl2 ) {
			final Op jenaOp = ( (GenericSPARQLGraphPatternImpl2) queryPattern ).asJenaOp();
			return getNumberOfTermOccurrences(jenaOp);
		}
		else {
			throw new UnsupportedOperationException( queryPattern.getClass().getName() );
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
			final Op jenaOp;
			if ( pattern instanceof GenericSPARQLGraphPatternImpl1 ) {
				@SuppressWarnings("deprecation")
				final Op o = ( (GenericSPARQLGraphPatternImpl1) pattern ).asJenaOp();
				jenaOp = o;
			}
			else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 ) {
				jenaOp = ( (GenericSPARQLGraphPatternImpl2) pattern ).asJenaOp();
			}
			else {
				throw new UnsupportedOperationException( pattern.getClass().getName() );
			}

			final Set<Var> certainVars = OpVars.fixedVars(jenaOp);
			Set<Var> possibleVars = OpVars.visibleVars(jenaOp);
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
