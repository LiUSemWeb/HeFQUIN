package se.liu.ida.hefquin.base.query.utils;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.*;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;

public class QueryPatternUtils
{
	/**
	 * Returns a representation of the given graph pattern as
	 * an object of the {@link Op} interface of the Jena API.
	 */
	public static Op convertToJenaOp( final SPARQLGraphPattern pattern ) {
		if ( pattern instanceof TriplePattern tp) {
			return new OpTriple( tp.asJenaTriple() );
		}
		else if ( pattern instanceof BGP bgp ) {
			final Set<TriplePattern> tps = bgp.getTriplePatterns();
			final BasicPattern bp = new BasicPattern();
			for ( final TriplePattern tp : tps ) {
				bp.add( tp.asJenaTriple() );
			}
			return new OpBGP(bp);
		}
		else if ( pattern instanceof SPARQLUnionPattern up ) {
			final Iterator<SPARQLGraphPattern> it = up.getSubPatterns().iterator();
			Op unionOp = convertToJenaOp( it.next() );
			while ( it.hasNext() ) {
				final Op nextOp = convertToJenaOp( it.next() );
				unionOp = OpUnion.create( unionOp, nextOp );
			}

			return unionOp;
		}
		else if ( pattern instanceof SPARQLGroupPattern gp ) {
			final Iterator<SPARQLGraphPattern> it = gp.getSubPatterns().iterator();
			Op joinOp = convertToJenaOp( it.next() );
			while ( it.hasNext() ) {
				final Op nextOp = convertToJenaOp( it.next() );
				joinOp = OpJoin.create( joinOp, nextOp );
			}

			return joinOp;
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl1 gp1 ) {
			@SuppressWarnings("deprecation")
			final Op jenaOp = gp1.asJenaOp();
			return jenaOp;
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 gp2 ) {
			return gp2.asJenaOp();
		}

		throw new IllegalArgumentException( "Unsupported type of graph pattern: " + pattern.getClass().getName() );
	}

	public static Element convertToJenaElement( final SPARQLGraphPattern p ) {
		if ( p instanceof TriplePattern tp ) {
			final ElementTriplesBlock e = new ElementTriplesBlock();
			e.addTriple( tp.asJenaTriple() );
			return e;
		}
		else if ( p instanceof BGP bgp ) {
			final ElementTriplesBlock e = new ElementTriplesBlock();
			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				e.addTriple( tp.asJenaTriple() );
			}
			return e;
		}
		else if (p instanceof SPARQLUnionPattern up ) {
			final ElementUnion e = new ElementUnion();
			for ( final SPARQLGraphPattern gp : up.getSubPatterns() ) {
				e.addElement(convertToJenaElement(gp));
			}
			return e;
		}
		else if (p instanceof SPARQLGroupPattern gp ) {
			final ElementGroup e = new ElementGroup();
			for ( final SPARQLGraphPattern g : gp.getSubPatterns() ) {
				e.addElement(convertToJenaElement(g));
			}
			return e;
		}
		else if ( p instanceof GenericSPARQLGraphPatternImpl1 gp1 ) {
			return gp1.asJenaElement();
		}
		else if ( p instanceof GenericSPARQLGraphPatternImpl2 gp2 ) {
			@SuppressWarnings("deprecation")
			final Element jenaElement = gp2.asJenaElement();
			return jenaElement;
		}
		else {
			throw new IllegalArgumentException( "unexpected type of graph pattern: " + p.getClass().getName() );
		}
	}

}
