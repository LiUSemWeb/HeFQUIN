package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

/**
 * Implementation of (a batching version of) the bind join algorithm that
 * uses FILTERs to capture the potential join partners that are sent to the
 * federation member.
 *
 * For every batch of solution mappings from the input, the algorithm of this
 * operator sends a SPARQL request to the federation member; this request
 * consists of the given graph pattern, extended with a FILTER that captures
 * the possible join values which are taken from the solutions of the current
 * input batch (in fact, the algorithm may also decide to split the input
 * batch into smaller batches for multiple requests).
 * The response to such a request is the subset of the solutions for the graph
 * pattern that are join partners for at least one of the solutions that were
 * used for creating the request.
 * After receiving such a response, the algorithm locally joins the solutions
 * from the response with the solutions in the batch used for creating the
 * request, and outputs the resulting joined solutions (if any).
 * Thereafter, the algorithm moves on to the next batch of solutions from
 * the input.
 */
public class ExecOpBindJoinSPARQLwithFILTER extends BaseForExecOpBindJoinSPARQL
{
	public ExecOpBindJoinSPARQLwithFILTER( final SPARQLGraphPattern query,
	                                       final SPARQLEndpoint fm,
	                                       final boolean useOuterJoinSemantics,
	                                       final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOp( final Iterable<SolutionMapping> solMaps ) {
		final Op op = createFilter(solMaps);
		if ( op == null ) {
			return null;
		}

		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl2(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		return new ExecOpRequestSPARQL(request, fm, false);
	}


	// ---- helper functions ---------

	public Op createFilter( final Iterable<SolutionMapping> solMaps ) {
		if ( varsInSubQuery.isEmpty() ) {
			return QueryPatternUtils.convertToJenaOp(query);
		}

		Expr disjunction = null;
		for ( final SolutionMapping s : solMaps ) {
			final Binding b = SolutionMappingUtils.restrict( s.asJenaBinding(), varsInSubQuery );

			assert ! SolutionMappingUtils.containsBlankNodes(b);

			Expr conjunction = null;
			final Iterator<Var> vars = b.vars(); 
			while (vars.hasNext()) {
				final Var v = vars.next();
				final Node uri = b.get(v);
				final Expr expr = new E_Equals(new ExprVar(v), new NodeValueNode(uri));
				if (conjunction == null) {
					conjunction = expr;
				} else {
					conjunction = new E_LogicalAnd(conjunction, expr);
				}
			}
			if (conjunction == null)
				continue;
			if (disjunction == null) {
				disjunction = conjunction;
			} else {
				disjunction = new E_LogicalOr(disjunction, conjunction);
			}
		}

		if ( disjunction == null ) {
			return QueryPatternUtils.convertToJenaOp(query);
		}

		return OpFilter.filter( disjunction, QueryPatternUtils.convertToJenaOp(query) );
	}

}
