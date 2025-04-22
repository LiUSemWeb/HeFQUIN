package se.liu.ida.hefquin.base.query.impl;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.core.*;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformSubst;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.base.queryplan.utils.ExpectedVariablesUtilsCopy;

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
	 * Assumes that the given {@link ElementPathBlock} does not contain
	 * property path patterns (but only triple patterns). If it does,
	 * this methods throws an {@link IllegalArgumentException}.
	 */
	public static BGP createBGP( final ElementPathBlock pattern ) {
		return createBGP( pattern.getPattern() );
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

	/**
	 * Returns a representation of the given graph pattern as
	 * an object of the {@link Op} interface of the Jena API.
	 */
	public static Op convertToJenaOp( final SPARQLGraphPattern pattern ) {
		if ( pattern instanceof TriplePattern) {
			final Triple triple = ( (TriplePattern) pattern ).asJenaTriple();
			return new OpTriple(triple);
		}
		else if ( pattern instanceof BGP ) {
			final Set<? extends TriplePattern> tps = ( (BGP) pattern ).getTriplePatterns();
			final BasicPattern bgp = new BasicPattern();
			for ( final TriplePattern tp : tps ) {
				bgp.add( tp.asJenaTriple() );
			}
			return new OpBGP(bgp);
		}
		else if ( pattern instanceof SPARQLUnionPattern ) {
			final SPARQLUnionPattern up = (SPARQLUnionPattern) pattern;

			final Iterator<SPARQLGraphPattern> it = up.getSubPatterns().iterator();
			Op unionOp = convertToJenaOp( it.next() );
			while ( it.hasNext() ) {
				final Op nextOp = convertToJenaOp( it.next() );
				unionOp = OpUnion.create( unionOp, nextOp );
			}

			return unionOp;
		}
		else if ( pattern instanceof SPARQLGroupPattern ) {
			final SPARQLGroupPattern gp = (SPARQLGroupPattern) pattern;

			final Iterator<SPARQLGraphPattern> it = gp.getSubPatterns().iterator();
			Op joinOp = convertToJenaOp( it.next() );
			while ( it.hasNext() ) {
				final Op nextOp = convertToJenaOp( it.next() );
				joinOp = OpJoin.create( joinOp, nextOp );
			}

			return joinOp;
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl1 ) {
			@SuppressWarnings("deprecation")
			final Op jenaOp = ( (GenericSPARQLGraphPatternImpl1) pattern ).asJenaOp();
			return jenaOp;
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 ) {
			return ( (GenericSPARQLGraphPatternImpl2) pattern ).asJenaOp();
		}

		throw new IllegalArgumentException( "Unsupported type of graph pattern: " + pattern.getClass().getName() );
	}

	public static Element convertToJenaElement( final SPARQLGraphPattern p ) {
		if ( p instanceof TriplePattern ) {
			final TriplePattern tp = (TriplePattern) p;

			final ElementTriplesBlock e = new ElementTriplesBlock();
			e.addTriple( tp.asJenaTriple() );
			return e;
		}
		else if ( p instanceof BGP ) {
			final BGP bgp = (BGP) p;

			final ElementTriplesBlock e = new ElementTriplesBlock();
			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				e.addTriple( tp.asJenaTriple() );
			}
			return e;
		}
		else if (p instanceof SPARQLUnionPattern) {
			final SPARQLUnionPattern up = (SPARQLUnionPattern) p;
			
			final ElementUnion e = new ElementUnion();
			for ( final SPARQLGraphPattern gp : up.getSubPatterns() ) {
				e.addElement(convertToJenaElement(gp));
			}
			return e;
		}
		else if (p instanceof SPARQLGroupPattern) {
			final SPARQLGroupPattern gp = (SPARQLGroupPattern) p;
			
			final ElementGroup e = new ElementGroup();
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
			throw new IllegalArgumentException( "unexpected type of graph pattern: " + p.getClass().getName() );
		}
	}

	/**
	 * Returns the set of all triple patterns that occur in the given graph pattern.
	 */
	public static Set<TriplePattern> getTPsInPattern( final SPARQLGraphPattern queryPattern ) {
		if ( queryPattern instanceof GenericSPARQLGraphPatternImpl1 gp1 ) {
			return getTPsInPattern( gp1.asJenaElement() );
		}
		if ( queryPattern instanceof GenericSPARQLGraphPatternImpl2 gp2 ) {
			return getTPsInPattern( gp2.asJenaOp() );
		}

		final Set<TriplePattern> tps = new HashSet<>();
		if ( queryPattern instanceof TriplePattern tp ) {
			tps.add(tp);
		}
		else if ( queryPattern instanceof BGP bgp ) {
			tps.addAll( bgp.getTriplePatterns() );
		}
		else if ( queryPattern instanceof SPARQLGroupPattern gp ) {
			for ( int i = 0; i < gp.getNumberOfSubPatterns(); i++ ) {
				tps.addAll( getTPsInPattern(gp.getSubPatterns(i)) );
			}
		}
		else if ( queryPattern instanceof SPARQLUnionPattern up ) {
			for ( int i = 0; i < up.getNumberOfSubPatterns(); i++ ) {
				tps.addAll( getTPsInPattern( up.getSubPatterns(i) ) );
			}
		}
		else {
			throw new UnsupportedOperationException( queryPattern.getClass().getName() );
		}
		return tps;
	}

	public static Set<TriplePattern> getTPsInPattern( final Op op ) {
		if ( op instanceof OpJoin || op instanceof OpLeftJoin || op instanceof OpUnion ) {
			return getTPsInPattern( (Op2) op );
		}

		if ( op instanceof OpService || op instanceof OpFilter || op instanceof OpExtend ){
			return getTPsInPattern( ((Op1) op).getSubOp() );
		}

		if ( op instanceof OpBGP opBGP ) {
			final Set<TriplePattern> tps = new HashSet<>();
			for ( final Triple t : opBGP.getPattern().getList() ) {
				tps.add( new TriplePatternImpl(t) );
			}
			return tps;
		}

		throw new UnsupportedOperationException("Getting the triple patterns from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
	}

	public static Set<TriplePattern> getTPsInPattern( final Op2 op ) {
		final Set<TriplePattern> varLeft = getTPsInPattern( op.getLeft() );
		final Set<TriplePattern> varRight = getTPsInPattern( op.getRight() );
		varLeft.addAll(varRight);
		return varLeft;
	}

	public static Set<TriplePattern> getTPsInPattern ( final Element e ) {
		final Set<TriplePattern> tps = new HashSet<>();
		if ( e instanceof ElementTriplesBlock b ) {
			for ( final Triple tp : b.getPattern().getList() ) {
				tps.add( new TriplePatternImpl(tp) );
			}
		}
		else if ( e instanceof ElementPathBlock b ) {
			for ( final TriplePath tpp : b.getPattern().getList() ) {
				if ( ! tpp.isTriple() ) {
					throw new IllegalArgumentException( "Property paths patterns are not supported by HeFQUIN." );
				}
				tps.add( new TriplePatternImpl(tpp.asTriple()) );
			}
		}
		else if ( e instanceof ElementGroup eg ) {
			for ( Element el: eg.getElements() ){
				tps.addAll( getTPsInPattern(el) );
			}
		}
		else if ( e instanceof ElementFilter ) {
			// Do nothing
		}
		else if ( e instanceof ElementBind ) {
			// Do nothing
		}
		else
			throw new IllegalArgumentException( "Cannot get triple patterns of the operator (type: " + e.getClass().getName() + ")." );

		return tps;
	}

	/**
	 * Returns the set of all variables that occur in the given graph pattern,
	 * but ignoring variables in FILTER expressions.
	 *
	 * If the given pattern is a {@link TriplePattern}, this function returns
	 * the result of {@link #getVariablesInPattern(TriplePattern)}. Similarly,
	 * if the given pattern is a {@link BGP}, this function returns the result
	 * of {@link #getVariablesInPattern(BGP)}.
	 */
	public static Set<Var> getVariablesInPattern( final SPARQLGraphPattern queryPattern ) {
		if ( queryPattern instanceof TriplePattern tp ) {
			return getVariablesInPattern(tp);
		}
		else if ( queryPattern instanceof BGP bgp ) {
			return getVariablesInPattern(bgp);
		}
		else if ( queryPattern instanceof SPARQLGroupPattern gp ) {
			final Set<Var> vars = new HashSet<>();
			for ( final SPARQLGraphPattern subPattern : gp.getSubPatterns() ) {
				vars.addAll( getVariablesInPattern(subPattern) );
			}

			return vars;
		}
		else if ( queryPattern instanceof SPARQLUnionPattern up ) {
			final Set<Var> vars = new HashSet<>();
			for ( final SPARQLGraphPattern subPattern : up.getSubPatterns() ) {
				vars.addAll( getVariablesInPattern(subPattern) );
			}

			return vars;
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl1 gp1 ) {
			@SuppressWarnings("deprecation")
			final Op jenaOp = gp1.asJenaOp();
			return getVariablesInPattern(jenaOp);
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl2 gp2 ) {
			return getVariablesInPattern( gp2.asJenaOp() );
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

	/**
	 * Ignores variables in FILTER expressions and in BIND expressions (but not
	 * the variables assigned by a BIND).
	 */
	public static Set<Var> getVariablesInPattern( final Op op ) {
		if ( op instanceof OpBGP opBGP ) {
			return getVariablesInPattern(opBGP);
		}
		else if ( op instanceof OpJoin || op instanceof OpLeftJoin || op instanceof OpUnion ) {
			return getVariablesInPattern( (Op2) op );
		}
		else if ( op instanceof OpService opService ){
			return getVariablesInPattern( opService.getSubOp() );
		}
		else if ( op instanceof OpFilter opFilter ){
			return getVariablesInPattern( opFilter.getSubOp() );
		}
		else if ( op instanceof OpExtend opExtend ){
			final Set<Var> vars = getVariablesInPattern( opExtend.getSubOp() );
			vars.addAll( opExtend.getVarExprList().getVars() );
			return vars;
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
		if ( op instanceof OpBGP opBGP ) {
			return getNumberOfVarOccurrences(opBGP);
		}
		else if ( op instanceof OpJoin || op instanceof OpLeftJoin || op instanceof OpUnion ) {
			return getNumberOfVarOccurrences( (Op2) op );
		}
		else if ( op instanceof OpService opService ){
			return getNumberOfVarOccurrences( opService.getSubOp() );
		}
		else if ( op instanceof OpFilter opFilter ){
			return getNumberOfVarOccurrences( opFilter.getSubOp() );
		}
		else if ( op instanceof OpExtend opExtend ){
			return getNumberOfVarOccurrences( opExtend.getSubOp() ) + opExtend.getVarExprList().getVars().size();
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
		if ( op instanceof OpBGP opBGP ) {
			return getNumberOfTermOccurrences(opBGP);
		}
		else if ( op instanceof OpJoin || op instanceof OpLeftJoin || op instanceof OpUnion ) {
			return getNumberOfTermOccurrences( (Op2) op );
		}
		else if ( op instanceof OpService opService ){
			return getNumberOfTermOccurrences( opService.getSubOp() );
		}
		else if ( op instanceof OpFilter opFilter ){
			return getNumberOfTermOccurrences( opFilter.getSubOp() );
		}
		else if ( op instanceof OpExtend opExtend ){
			return getNumberOfTermOccurrences( opExtend.getSubOp() );
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
		if ( queryPattern instanceof TriplePattern tp ) {
			return getNumberOfVarOccurrences(tp);
		}
		else if ( queryPattern instanceof BGP bgp ) {
			return getNumberOfVarOccurrences(bgp);
		}
		else if ( queryPattern instanceof SPARQLGroupPattern gp ) {
			int sum = 0;
			for ( final SPARQLGraphPattern subPattern : gp.getSubPatterns() ) {
				sum += getNumberOfVarOccurrences(subPattern);
			}

			return sum;
		}
		else if ( queryPattern instanceof SPARQLUnionPattern up ) {
			int sum = 0;
			for ( final SPARQLGraphPattern subPattern : up.getSubPatterns() ) {
				sum += getNumberOfVarOccurrences(subPattern);
			}

			return sum;
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl1 gp1 ) {
			@SuppressWarnings("deprecation")
			final Op jenaOp = gp1.asJenaOp();
			return getNumberOfVarOccurrences(jenaOp);
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl2 gp2 ) {
			return getNumberOfVarOccurrences( gp2.asJenaOp() );
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
		if ( queryPattern instanceof TriplePattern tp ) {
			return getNumberOfTermOccurrences(tp);
		}
		else if ( queryPattern instanceof BGP bgp ) {
			return getNumberOfTermOccurrences(bgp);
		}
		else if ( queryPattern instanceof SPARQLGroupPattern gp ) {
			int sum = 0;
			for ( final SPARQLGraphPattern subPattern : gp.getSubPatterns() ) {
				sum += getNumberOfTermOccurrences(subPattern);
			}

			return sum;
		}
		else if ( queryPattern instanceof SPARQLUnionPattern up ) {
			int sum = 0;
			for ( final SPARQLGraphPattern subPattern : up.getSubPatterns() ) {
				sum += getNumberOfTermOccurrences(subPattern);
			}

			return sum;
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl1 gp1 ) {
			@SuppressWarnings("deprecation")
			final Op jenaOp = gp1.asJenaOp();
			return getNumberOfTermOccurrences(jenaOp);
		}
		else if ( queryPattern instanceof GenericSPARQLGraphPatternImpl2 gp2 ) {
			return getNumberOfTermOccurrences( gp2.asJenaOp() );
		}
		else {
			throw new UnsupportedOperationException( queryPattern.getClass().getName() );
		}
	}

	public static ExpectedVariables getExpectedVariablesInPattern( final SPARQLGraphPattern pattern ) {
		if ( pattern instanceof TriplePattern tp ) {
			return new ExpectedVariables() {
				@Override public Set<Var> getPossibleVariables() {
					return Collections.emptySet();
				}

				@Override public Set<Var> getCertainVariables() {
					return getVariablesInPattern(tp);
				}
			};
		}
		else if ( pattern instanceof BGP bgp ) {
			return new ExpectedVariables() {
				@Override public Set<Var> getPossibleVariables() {
					return Collections.emptySet();
				}

				@Override public Set<Var> getCertainVariables() {
					return getVariablesInPattern(bgp);
				}
			};
		}
		else if ( pattern instanceof SPARQLGroupPattern gp ) {
			final ExpectedVariables[] evs = new ExpectedVariables[gp.getNumberOfSubPatterns()];
			for ( int i = 0; i < gp.getNumberOfSubPatterns(); i++ ) {
				evs[i] = getExpectedVariablesInPattern( gp.getSubPatterns(i) );
			}

			final Set<Var> certainVars = ExpectedVariablesUtilsCopy.unionOfCertainVariables(evs);
			final Set<Var> possibleVars = ExpectedVariablesUtilsCopy.unionOfPossibleVariables(evs);
			possibleVars.removeAll(certainVars);

			return new ExpectedVariables() {
				@Override public Set<Var> getPossibleVariables() { return possibleVars; }
				@Override public Set<Var> getCertainVariables() { return certainVars; }
			};
		}
		else if ( pattern instanceof SPARQLUnionPattern up ) {
			final ExpectedVariables[] evs = new ExpectedVariables[up.getNumberOfSubPatterns()];
			for ( int i = 0; i < up.getNumberOfSubPatterns(); i++ ) {
				evs[i] = getExpectedVariablesInPattern( up.getSubPatterns(i) );
			}

			final Set<Var> certainVars = ExpectedVariablesUtilsCopy.intersectionOfCertainVariables(evs);
			final Set<Var> possibleVars = ExpectedVariablesUtilsCopy.unionOfAllVariables(evs);
			possibleVars.removeAll(certainVars);

			return new ExpectedVariables() {
				@Override public Set<Var> getPossibleVariables() { return possibleVars; }
				@Override public Set<Var> getCertainVariables() { return certainVars; }
			};
		}
		else {
			final Op jenaOp;
			if ( pattern instanceof GenericSPARQLGraphPatternImpl1 gp1 ) {
				@SuppressWarnings("deprecation")
				final Op o = gp1.asJenaOp();
				jenaOp = o;
			}
			else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 gp2 ) {
				jenaOp = gp2.asJenaOp();
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
		if ( pattern instanceof TriplePattern tp )
		{
			return applySolMapToTriplePattern(sm, tp);
		}
		else if ( pattern instanceof BGP bgp )
		{
			return applySolMapToBGP(sm, bgp);
		}
		else if ( pattern instanceof SPARQLUnionPattern up )
		{
			final SPARQLUnionPatternImpl upNew = new SPARQLUnionPatternImpl();
			boolean unchanged = true;
			for ( final SPARQLGraphPattern p : up.getSubPatterns() ) {
				final SPARQLGraphPattern pNew = applySolMapToGraphPattern(sm, p);
				upNew.addSubPattern(pNew);
				if ( ! pNew.equals(p) ) {
					unchanged = false;
				}
			}

			return ( unchanged ) ? pattern : upNew;
		}
		else if ( pattern instanceof SPARQLGroupPattern gp )
		{
			final SPARQLGroupPatternImpl gpNew = new SPARQLGroupPatternImpl();
			boolean unchanged = true;
			for ( final SPARQLGraphPattern p : gp.getSubPatterns() ) {
				final SPARQLGraphPattern pNew = applySolMapToGraphPattern(sm, p);
				gpNew.addSubPattern(pNew);
				if ( ! pNew.equals(p) ) {
					unchanged = false;
				}
			}

			return ( unchanged ) ? pattern : gpNew;
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl1 gp1 )
		{
			final Map<Var, Node> map1 = new HashMap<>();
			final Map<String, Expr> map2 = new HashMap<>();
			sm.asJenaBinding().forEach( (v,n) -> { map1.put(v,n); map2.put(v.getVarName(),NodeValue.makeNode(n)); } );
			final ElementTransform t1 = new ElementTransformSubst(map1);
			final ExprTransformSubstitute t2 = new ExprTransformSubstitute(map2);

			final Element e = gp1.asJenaElement();
			final Element eNew = ElementTransformer.transform(e, t1, t2);
			return ( e == eNew ) ? pattern : new GenericSPARQLGraphPatternImpl1(eNew);
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 gp2 )
		{
			final Map<Var, Node> map = new HashMap<>();
			sm.asJenaBinding().forEach( (v,n) -> map.put(v,n) );
			final NodeTransform t = new NodeTransformSubst(map);

			final Op op = gp2.asJenaOp();
			final Op opNew = NodeTransformLib.transform(t, op);
			return ( op == opNew ) ? pattern : new GenericSPARQLGraphPatternImpl2(opNew);
		}
		else
			throw new UnsupportedOperationException("TODO");
	}

	/**
	 * Attention, this function throws an exception in all cases in which
	 * one of the variables of the BGP would be replaced by a blank node.
	 */
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

	/**
	 * Merges the given filter expressions into the given graph pattern.
	 */
	public static SPARQLGraphPattern merge( final ExprList exprs, final SPARQLGraphPattern p ) {
		// Create a new ElementGroup object, add into it the Element represented
		// by the given graph pattern, add into it the filters, and create a new
		// graph pattern from it.
		final ElementGroup group = new ElementGroup();

		// - convert the given graph pattern into an Element and add it to the group
		final Element elmt = convertToJenaElement(p);
		if ( elmt instanceof ElementGroup eg ) {
			// If the given graph pattern was converted to an ElementGroup, instead
			// of simply adding it into the new group, copy its sub-elements over
			// to the new group, which avoids unnecessary nesting of groups.
			for ( final Element subElmt : eg.getElements() ) {
				group.addElement(subElmt);
			}
		}
		else {
			// If the given graph pattern was converted to something other
			// than an ElementGroup, simply add it into the new group.
			group.addElement(elmt);
		}

		// - now add the filters to the group
		for ( final Expr expr : exprs ) {
			final ElementFilter f = new ElementFilter(expr);
			group.addElementFilter(f);
		}

		return new GenericSPARQLGraphPatternImpl1(group);
	}

	/**
	 * Merges the given BIND clause into the given graph pattern.
	 */
	public static SPARQLGraphPattern merge( final VarExprList exprs, final SPARQLGraphPattern p ) {
		// Create a new ElementGroup object, add into it the Element represented
		// by the given graph pattern, add into it the filters, and create a new
		// graph pattern from it.
		final ElementGroup group = new ElementGroup();

		// - convert the given graph pattern into an Element and add it to the group
		final Element elmt = convertToJenaElement(p);
		if ( elmt instanceof ElementGroup eg ) {
			// If the given graph pattern was converted to an ElementGroup, instead
			// of simply adding it into the new group, copy its sub-elements over
			// to the new group, which avoids unnecessary nesting of groups.
			for ( final Element subElmt : eg.getElements() ) {
				group.addElement(subElmt);
			}
		}
		else {
			// If the given graph pattern was converted to something other
			// than an ElementGroup, simply add it into the new group.
			group.addElement(elmt);
		}

		// - now add the BIND clause to the group
		for ( final Var v : exprs.getVars() ) {
			final ElementBind f = new ElementBind(v, exprs.getExpr(v) );
			group.addElement(f);
		}

		return new GenericSPARQLGraphPatternImpl1(group);
	}

	/**
	 * Merges the two graph patterns into a single one, using join semantics.
	 * Returns a {@link BGP} if possible (for instance, if both of the given
	 * patterns are BGPs or one of them is a BGP and the other one a triple
	 * pattern).
	 */
	public static SPARQLGraphPattern merge( final SPARQLGraphPattern p1,
	                                        final SPARQLGraphPattern p2 )
	{
		if ( p1 instanceof TriplePattern tp ) return merge(tp, p2);

		if ( p2 instanceof TriplePattern tp ) return merge(tp, p1);

		if ( p1 instanceof BGP bgp ) return merge(bgp, p2);

		if ( p2 instanceof BGP bgp ) return merge(bgp, p1);

		return new SPARQLGroupPatternImpl(p1, p2);
	}

	/**
	 * Merges the given triple pattern into the given graph pattern. If the
	 * given graph pattern is also a triple pattern or a BGP, then the resulting
	 * graph pattern is a BGP to which the triple pattern was added. Otherwise,
	 * the resulting graph pattern is the given graph pattern with the triple
	 * pattern joined into it.
	 */
	public static SPARQLGraphPattern merge( final TriplePattern tp, final SPARQLGraphPattern p ) {
		// If the given graph pattern is a triple pattern, produce and return a BGP.
		if ( p instanceof TriplePattern tp2 ) {
			return new BGPImpl(tp2, tp);
		}

		// If the given graph pattern is a BGP, produce and return a BGP.
		if ( p instanceof BGP bgp ) {
			final BGPImpl resultBGP = new BGPImpl(tp);

			for ( final TriplePattern tpOfBGP : bgp.getTriplePatterns() ) {
				resultBGP.addTriplePattern(tpOfBGP);
			}

			return resultBGP;
		}

		// Convert the given graph pattern into a Jena Element object and continue with that.
		final Element elmt = convertToJenaElement(p);

		// If we can still create a BGP, then we do that.
		if ( elmt instanceof ElementTriplesBlock block ) {
			// create the BGP and add the given triple pattern into it
			final BGPImpl resultBGP = new BGPImpl(tp);

			// add the triple patterns from the given graph pattern into the BGP as well
			final Iterator<Triple> it = block.patternElts();
			while ( it.hasNext() ) {
				resultBGP.addTriplePattern( new TriplePatternImpl(it.next()) );
			}

			return resultBGP;
		}

		// At this point it is clear that we will return a GenericSPARQLGraphPatternImpl1,
		// for which we need to create an Element object first. The type of Element that
		// we create depends on the type of the Element that the given graph pattern was
		// converted to.
		final Element resultElmt;

		if ( elmt instanceof ElementPathBlock block ) {
			// If the given graph pattern was converted to an ElementPathBlock,
			// create a copy of that ElementPathBlock and add the given triple
			// pattern into that copy.
			final ElementPathBlock copy = new ElementPathBlock();

			final Iterator<TriplePath> it = block.patternElts();
			while ( it.hasNext() ) {
				copy.addTriple( it.next() );
			}

			copy.addTriple( tp.asJenaTriple() );

			resultElmt = copy;
		}
		else if ( elmt instanceof ElementGroup eg ) {
			// If the given graph pattern was converted to an ElementGroup,
			// create a copy of that ElementGroup with the same sub-elements.
			// When creating the copy, try to add the given triple pattern
			// into a copy of one of the sub-elements. If that's not possible
			// (i.e., none of the sub-elements is of a suitable type), then
			// add the triple pattern as an additional sub-element to the copy
			// in the end.
			final ElementGroup newGroup = new ElementGroup();
			boolean tpAdded = false;
			for ( final Element subElmt : eg.getElements() )
			{
				if ( ! tpAdded && subElmt instanceof ElementTriplesBlock block ) {
					final ElementTriplesBlock copy = new ElementTriplesBlock();

					final Iterator<Triple> it = block.patternElts();
					while ( it.hasNext() ) {
						copy.addTriple( it.next() );
					}

					copy.addTriple( tp.asJenaTriple() );
					tpAdded = true;

					newGroup.addElement(copy);
				}
				else if ( ! tpAdded && subElmt instanceof ElementPathBlock block ) {
					final ElementPathBlock copy = new ElementPathBlock();

					final Iterator<TriplePath> it = block.patternElts();
					while ( it.hasNext() ) {
						copy.addTriplePath( it.next() );
					}

					copy.addTriple( tp.asJenaTriple() );
					tpAdded = true;

					newGroup.addElement(copy);
				}
				else {
					newGroup.addElement(subElmt);
				}
			}

			if ( ! tpAdded ) {
				final ElementTriplesBlock bgp = new ElementTriplesBlock();
				bgp.addTriple( tp.asJenaTriple() );
				newGroup.addElement(bgp);
			}

			resultElmt = newGroup;
		}
		else {
			// In all other cases, create an ElementGroup, ...
			final ElementGroup newGroup = new ElementGroup();

			// ... add the Element obtained for the given graph
			// pattern as one sub-element of the group, and ...
			newGroup.addElement(elmt);

			// ... add the given triple pattern as another sub-element.
			final ElementTriplesBlock bgp = new ElementTriplesBlock();
			bgp.addTriple( tp.asJenaTriple() );
			newGroup.addElement(bgp);

			resultElmt = newGroup;
		}

		return new GenericSPARQLGraphPatternImpl1(resultElmt);
	}

	/**
	 * Merges the given BGP into the given graph pattern. If the given graph
	 * pattern is also a BGP, then the resulting graph pattern is a BGP that
	 * is the union of the two given BGPs. Otherwise, the resulting graph
	 * pattern is the given graph pattern with the BGP joined into it.
	 */
	public static SPARQLGraphPattern merge( final BGP bgp, final SPARQLGraphPattern p ) {
		// If the given graph pattern is a triple pattern, produce and return a BGP.
		if ( p instanceof TriplePattern tp ) {
			final BGPImpl resultBGP = new BGPImpl(tp);

			for ( final TriplePattern tp2 : bgp.getTriplePatterns() ) {
				resultBGP.addTriplePattern(tp2);
			}

			return resultBGP;
		}

		// If the given graph pattern is a BGP, produce and return a BGP.
		if ( p instanceof BGP bgp2 ) {
			final BGPImpl resultBGP = new BGPImpl();

			for ( final TriplePattern tp : bgp2.getTriplePatterns() ) {
				resultBGP.addTriplePattern(tp);
			}

			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				resultBGP.addTriplePattern(tp);
			}

			return resultBGP;
		}

		// Convert the given graph pattern into a Jena Element object and continue with that.
		final Element elmt = convertToJenaElement(p);

		// If we can still create a BGP, then we do that.
		if ( elmt instanceof ElementTriplesBlock block ) {
			// create the BGP
			final BGPImpl resultBGP = new BGPImpl();

			// add the triple patterns from the given graph pattern to the BGP
			final Iterator<Triple> it = block.patternElts();
			while ( it.hasNext() ) {
				resultBGP.addTriplePattern( new TriplePatternImpl(it.next()) );
			}

			// add the triple patterns of the given BGP as well
			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				resultBGP.addTriplePattern(tp);
			}

			return resultBGP;
		}

		// At this point it is clear that we will return a GenericSPARQLGraphPatternImpl1,
		// for which we need to create an Element object first. The type of Element that
		// we create depends on the type of the Element that the given graph pattern was
		// converted to.
		final Element resultElmt;

		if ( elmt instanceof ElementPathBlock block ) {
			// If the given graph pattern was converted to an ElementPathBlock,
			// create a copy of that ElementPathBlock, and add the triple patterns
			// of the given BGP into that copy.
			final ElementPathBlock copy = new ElementPathBlock();

			final Iterator<TriplePath> it = block.patternElts();
			while ( it.hasNext() ) {
				copy.addTriple( it.next() );
			}

			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				copy.addTriple( tp.asJenaTriple() );
			}

			resultElmt = copy;
		}
		else if ( elmt instanceof ElementGroup eg ) {
			// If the given graph pattern was converted to an ElementGroup,
			// create a copy of that ElementGroup with the same sub-elements.
			// When creating the copy, try to add the triple patterns of the
			// given BGP into a copy of one of the sub-elements. If that's not
			// possible (i.e., none of the sub-elements is of a suitable type),
			// then add the BGP as an additional sub-element to the copy in the
			// end.
			final ElementGroup newGroup = new ElementGroup();
			boolean bgpAdded = false;
			for ( final Element subElmt : eg.getElements() )
			{
				if ( ! bgpAdded && subElmt instanceof ElementTriplesBlock block ) {
					final ElementTriplesBlock copy = new ElementTriplesBlock();

					final Iterator<Triple> it = block.patternElts();
					while ( it.hasNext() ) {
						copy.addTriple( it.next() );
					}

					for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
						copy.addTriple( tp.asJenaTriple() );
					}
					bgpAdded = true;

					newGroup.addElement(copy);
				}
				else if ( ! bgpAdded && subElmt instanceof ElementPathBlock block ) {
					final ElementPathBlock copy = new ElementPathBlock();

					final Iterator<TriplePath> it = block.patternElts();
					while ( it.hasNext() ) {
						copy.addTriplePath( it.next() );
					}

					for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
						copy.addTriple( tp.asJenaTriple() );
					}
					bgpAdded = true;

					newGroup.addElement(copy);
				}
				else {
					newGroup.addElement(subElmt);
				}
			}

			if ( ! bgpAdded ) {
				final ElementTriplesBlock bgpToAdd = new ElementTriplesBlock();

				for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
					bgpToAdd.addTriple( tp.asJenaTriple() );
				}

				newGroup.addElement(bgpToAdd);
			}

			resultElmt = newGroup;
		}
		else {
			// In all other cases, create an ElementGroup, ...
			final ElementGroup newGroup = new ElementGroup();

			// ... add the Element obtained for the given graph
			// pattern as one sub-element of the group, and ...
			newGroup.addElement(elmt);

			// ... add the given BGP as another sub-element.
			final ElementTriplesBlock bgpToAdd = new ElementTriplesBlock();
			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				bgpToAdd.addTriple( tp.asJenaTriple() );
			}
			newGroup.addElement(bgpToAdd);

			resultElmt = newGroup;
		}

		return new GenericSPARQLGraphPatternImpl1(resultElmt);
	}

}
