package se.liu.ida.hefquin.query.jenaimpl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.query.BGP;
import se.liu.ida.hefquin.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.query.TriplePattern;

public class JenaBasedQueryPatternUtils
{
	public static JenaBasedTriplePattern createJenaBasedTriplePattern( final Node s, final Node p, final Node o ) {
		return new JenaBasedTriplePattern( new org.apache.jena.graph.Triple(s,p,o) );
	}

	public static JenaBasedBGP createJenaBasedBGP( final BasicPattern pattern ) {
		final Set<JenaBasedTriplePattern> tps = new HashSet<>();
		final Iterator<Triple> it = pattern.iterator();
		while ( it.hasNext() ) {
			tps.add( new JenaBasedTriplePattern(it.next()) );
		}
		return new JenaBasedBGP(tps);
	}

	public static SPARQLGraphPattern applySolMapToGraphPattern( final SolutionMapping sm, final SPARQLGraphPattern pattern ) {
		// TODO
		if ( pattern instanceof TriplePattern )
			return applySolMapToTriplePattern( sm, (TriplePattern) pattern );
		else if ( pattern instanceof BGP )
			return applySolMapToBGP( sm, (BGP) pattern );
		else
			throw new UnsupportedOperationException("TODO");
	}

	public static BGP applySolMapToBGP( final SolutionMapping sm, final BGP bgp ) {
		final JenaBasedSolutionMapping jbsm = (JenaBasedSolutionMapping) sm;
		final Set<JenaBasedTriplePattern> tps = new HashSet<>();
		boolean unchanged = true;
		for ( final JenaBasedTriplePattern tp : ((JenaBasedBGP)bgp).getTriplePatterns() ) {
			final JenaBasedTriplePattern tp2 = applySolMapToTriplePattern(jbsm, tp);
			tps.add(tp2);
			if ( tp2 != tp ) {
				unchanged = false;
			}
		}

		if ( unchanged ) {
			return bgp;
		} else {
			return new JenaBasedBGP(tps);
		}
	}

	public static JenaBasedTriplePattern applySolMapToTriplePattern( final SolutionMapping sm, final TriplePattern tp ) {
		return applySolMapToTriplePattern( (JenaBasedSolutionMapping) sm, (JenaBasedTriplePattern) tp );
	}

	public static JenaBasedTriplePattern applySolMapToTriplePattern( final JenaBasedSolutionMapping sm, final JenaBasedTriplePattern tp ) {
		final Binding b = sm.asJenaBinding();
		boolean unchanged = true;

		Node s = tp.asTriple().getSubject();
		if ( Var.isVar(s) ) {
			final Var var = Var.alloc(s);
			if ( b.contains(var) ) {
				s = b.get(var);
				unchanged = false;
			}
		}

		Node p = tp.asTriple().getPredicate();
		if ( Var.isVar(p) ) {
			final Var var = Var.alloc(p);
			if ( b.contains(var) ) {
				p = b.get(var);
				unchanged = false;
			}
		}

		Node o = tp.asTriple().getObject();
		if ( Var.isVar(o) ) {
			final Var var = Var.alloc(o);
			if ( b.contains(var) ) {
				p = b.get(var);
				unchanged = false;
			}
		}

		if ( unchanged ) {
			return tp;
		} else {
			return new JenaBasedTriplePattern( new org.apache.jena.graph.Triple(s,p,o) );
		}
	}

}
