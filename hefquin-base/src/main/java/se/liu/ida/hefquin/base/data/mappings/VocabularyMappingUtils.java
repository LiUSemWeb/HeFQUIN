package se.liu.ida.hefquin.base.data.mappings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.expr.ExprList;

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
		// TODO: translate the filter expressions
		throw new UnsupportedOperationException("Applying vocabulary mappings into FILTER expressions is not implemented yet.");
	}

}
