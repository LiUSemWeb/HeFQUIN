package se.liu.ida.hefquin.query.jenaimpl;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.query.TriplePattern;

public class JenaBasedQueryPatternUtils
{
	public static JenaBasedTriplePattern createJenaBasedTriplePattern( final Node s, final Node p, final Node o ) {
		return new JenaBasedTriplePattern( new org.apache.jena.graph.Triple(s,p,o) );
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
