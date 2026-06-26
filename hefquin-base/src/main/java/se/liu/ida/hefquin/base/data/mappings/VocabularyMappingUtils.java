package se.liu.ida.hefquin.base.data.mappings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.BGPImpl;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.query.impl.SPARQLGroupPatternImpl;
import se.liu.ida.hefquin.base.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;

public class VocabularyMappingUtils
{
	/**
	 * Rewrites the given graph pattern by recursively applying the given
	 * vocabulary mapping to each of the triple patterns inside the graph
	 * pattern.
	 */
	public static SPARQLGraphPattern translateGraphPattern( final SPARQLGraphPattern p,
	                                                        final VocabularyMapping vm ) {
		if ( p instanceof TriplePattern ) {
			return vm.translateTriplePattern( (TriplePattern) p );
		}
		else if ( p instanceof BGP ) {
			return translateGraphPattern( (BGP) p, vm );
		}
		else if ( p instanceof SPARQLGroupPattern ) {
			return translateGraphPattern( (SPARQLGroupPattern) p, vm );
		}
		else if ( p instanceof SPARQLUnionPattern ) {
			return translateGraphPattern( (SPARQLUnionPattern) p, vm );
		}
		else if ( p instanceof GenericSPARQLGraphPatternImpl1 ) {
			@SuppressWarnings("deprecation")
			final Op op = ((GenericSPARQLGraphPatternImpl1) p).asJenaOp();
			return translateGraphPattern(op, vm);
		}
		else if ( p instanceof GenericSPARQLGraphPatternImpl2 ) {
			final Op op = ((GenericSPARQLGraphPatternImpl2) p).asJenaOp();
			return translateGraphPattern(op, vm);
		}
		else {
			throw new IllegalArgumentException( "Unsupported type of pattern: " + p.getClass().getName() );
		}
	}

	public static SPARQLGraphPattern translateGraphPattern( final BGP bgp,
	                                                        final VocabularyMapping vm ) {
		final List<SPARQLGraphPattern> allSubPatterns = new ArrayList<>();
		final Set<TriplePattern> tpSubPatterns = new HashSet<>();
		boolean allSubPatternsAreTriplePatterns = true; // assume yes

		for( final TriplePattern tp : bgp.getTriplePatterns() ) {
			final SPARQLGraphPattern p = vm.translateTriplePattern(tp);
			allSubPatterns.add(p);

			if ( allSubPatternsAreTriplePatterns && p instanceof TriplePattern ) {
				tpSubPatterns.add( (TriplePattern) p );
			}
			else {
				allSubPatternsAreTriplePatterns = false;
			}
		}

		if ( allSubPatternsAreTriplePatterns ) {
			return new BGPImpl(tpSubPatterns);
		}
		else {
			return new SPARQLGroupPatternImpl(allSubPatterns);
		}
	}

	public static SPARQLGraphPattern translateGraphPattern( final SPARQLUnionPattern p,
	                                                        final VocabularyMapping vm ) {
		final SPARQLUnionPatternImpl unionTranslation = new SPARQLUnionPatternImpl();
		for ( final SPARQLGraphPattern sub : p.getSubPatterns() ) {
			unionTranslation.addSubPattern( translateGraphPattern(sub, vm) );
		}
		return unionTranslation;
	}

	public static SPARQLGraphPattern translateGraphPattern( final SPARQLGroupPattern p,
	                                                        final VocabularyMapping vm ) {
		final List<SPARQLGraphPattern> newSubPatterns = new ArrayList<>();
		for ( final SPARQLGraphPattern sub : p.getSubPatterns() ) {
			newSubPatterns.add( translateGraphPattern(sub, vm) );
		}

		return new SPARQLGroupPatternImpl(newSubPatterns);
	}

	public static SPARQLGraphPattern translateGraphPattern( final Op op,
	                                                        final VocabularyMapping vm ) {
		if ( op instanceof OpJoin join ) {
			return translateGraphPattern( join, vm );
		}
		else if ( op instanceof OpUnion union ) {
			return translateGraphPattern( union, vm );
		}
		else if ( op instanceof OpFilter filter ) {
			return translateGraphPattern( filter, vm );
		}
		else if ( op instanceof OpBGP bgp ) {
			return translateGraphPattern( bgp, vm );
		}
		else if ( op instanceof OpSequence seq ) {
			return translateGraphPattern( seq, vm );
		}
		else {
			throw new IllegalArgumentException( "Unsupported type of pattern: " + op.getClass().getName() );
		}
	}

	public static SPARQLGraphPattern translateGraphPattern( final OpJoin op,
	                                                        final VocabularyMapping vm ) {
		final List<SPARQLGraphPattern> subPatterns = new ArrayList<>();

		final Op left = op.getLeft();
		subPatterns.add( translateGraphPattern(left, vm) );

		final Op right = op.getRight();
		subPatterns.add( translateGraphPattern(right, vm) );

		return new SPARQLGroupPatternImpl(subPatterns);
	}

	public static SPARQLGraphPattern translateGraphPattern( final OpUnion op,
	                                                        final VocabularyMapping vm) {
		final SPARQLUnionPatternImpl unionTranslation = new SPARQLUnionPatternImpl();

		final Op left = op.getLeft();
		unionTranslation.addSubPattern( translateGraphPattern(left, vm) );

		final Op right = op.getRight();
		unionTranslation.addSubPattern( translateGraphPattern(right, vm) );

		return unionTranslation;
	}

	public static SPARQLGraphPattern translateGraphPattern( final OpBGP op,
	                                                        final VocabularyMapping vm ) {
		final List<SPARQLGraphPattern> allSubPatterns = new ArrayList<>();
		final Set<TriplePattern> tpSubPatterns = new HashSet<>();
		boolean allSubPatternsAreTriplePatterns = true; // assume yes

		for ( final Triple tp : op.getPattern().getList() ) {
			final SPARQLGraphPattern p = vm.translateTriplePattern( new TriplePatternImpl(tp) );
			allSubPatterns.add(p);

			if ( allSubPatternsAreTriplePatterns && p instanceof TriplePattern tp2 ) {
				tpSubPatterns.add(tp2);
			}
			else {
				allSubPatternsAreTriplePatterns = false;
			}
		}

		if ( allSubPatternsAreTriplePatterns ) {
			return new BGPImpl(tpSubPatterns);
		}
		else {
			return new SPARQLGroupPatternImpl(allSubPatterns);
		}
	}

	public static SPARQLGraphPattern translateGraphPattern( final OpSequence op,
	                                                        final VocabularyMapping vm ) {
		final List<SPARQLGraphPattern> subPatterns = new ArrayList<>();
		for ( final Op i : op.getElements() ) {
			subPatterns.add( translateGraphPattern(i, vm) );
		}

		return new SPARQLGroupPatternImpl(subPatterns);
	}

	public static SPARQLGraphPattern translateGraphPattern( final OpFilter op,
	                                                        final VocabularyMapping vm ) {
		final Op subOp = op.getSubOp();
		final SPARQLGraphPattern translatedSubPlan = translateGraphPattern(subOp, vm);
		final ExprList translatedExprs = translateExpressions( op.getExprs(), vm );
		return translatedSubPlan.mergeWith(translatedExprs);
	}

	public static ExprList translateExpressions( final ExprList exprs,
	                                             final VocabularyMapping vm ) {
		final ExprList rewrittenExpressions = new ExprList();

		for ( final Expr e : exprs ) {
			final Expr rewritten = rewrite(e, vm);

			if ( rewritten == null )
				throw new UnsupportedOperationException( "Filter expression " + e + " cannot be rewritten" );
			else
				rewrittenExpressions.add(rewritten);
		}
		return rewrittenExpressions;
	}

	private static Expr rewrite( final Expr expr, final VocabularyMapping vm ) {
		if ( expr instanceof E_LogicalAnd and ) {
			final Expr l = rewrite( and.getArg1(), vm );
			final Expr r = rewrite( and.getArg2(), vm );

			if ( l == null || r == null ) return null;

			return new E_LogicalAnd( l, r );
		}

		if ( expr instanceof E_LogicalOr or ) {
			final Expr l = rewrite( or.getArg1(), vm) ;
			final Expr r = rewrite( or.getArg2(), vm) ;

			if ( l == null || r == null ) return null;

			return new E_LogicalOr( l, r );
		}

		if ( expr instanceof E_Equals || expr instanceof E_NotEquals ) {
			return rewriteComparison( expr, vm );
		}

		// other rexpression types aren't considered rewritable at the moment
		return null;
	}

	private static Expr rewriteComparison( final Expr expr, final VocabularyMapping vm ) {
		final Expr left;
		final Expr right;
		final boolean equals;

		if ( expr instanceof E_Equals eq ) {
			left = eq.getArg1();
			right = eq.getArg2();
			equals = true;
		}
		else if ( expr instanceof E_NotEquals neq ) {
			left = neq.getArg1();
			right = neq.getArg2();
			equals = false;
		}
		else {
			return null;
		}

		if ( ! right.isConstant() )
			return null;

		final Node node = ( (NodeValue) right ).asNode();
		if ( ! node.isURI() )
			return null;

		final Set<Node> nodes = vm.translateNode(node);
		if ( nodes.isEmpty() )
			return null;

		final List<Expr> rewritten = new ArrayList<>();

		for ( final Node n : nodes ) {
			if ( equals )
				rewritten.add( new E_Equals(left, NodeValue.makeNode(n)) );
			else
				rewritten.add( new E_NotEquals(left, NodeValue.makeNode(n)) );
		}

		return equals ? buildOr(rewritten) : buildAnd(rewritten);
	}

	private static Expr buildOr( final List<Expr> exprList ) {
		if ( exprList == null || exprList.isEmpty() ) {
			throw new IllegalArgumentException( "Empty OR list" );
		}

		Expr result = exprList.get(0);

		for ( int i = 1; i < exprList.size(); i++ ) {
			result = new E_LogicalOr( result, exprList.get(i) );
		}

		return result;
	}

	private static Expr buildAnd( final List<Expr> exprList ) {
		if ( exprList == null || exprList.isEmpty() ) {
			throw new IllegalArgumentException("Empty AND list");
		}

		Expr result = exprList.get(0);

		for ( int i = 1; i < exprList.size(); i++ ) {
			result = new E_LogicalAnd( result, exprList.get(i) );
		}

		return result;
	}

}
