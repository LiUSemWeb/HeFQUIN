package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.Set;

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

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;

public class ExecOpBindJoinSPARQLwithFILTER extends ExecOpGenericBindJoinWithRequestOps<SPARQLGraphPattern, SPARQLEndpoint>
{
	protected final Set<Var> varsInSubQuery;

	public ExecOpBindJoinSPARQLwithFILTER( final TriplePattern query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInSubQuery = QueryPatternUtils.getVariablesInPattern(query);
	}

	public ExecOpBindJoinSPARQLwithFILTER( final BGP query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInSubQuery = QueryPatternUtils.getVariablesInPattern(query);
	}

	public ExecOpBindJoinSPARQLwithFILTER( final SPARQLGraphPattern query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInSubQuery = QueryPatternUtils.getVariablesInPattern(query);
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> solMaps ) {
		final Op op = createFilter(solMaps);
		if ( op == null ) {
			return null;
		}

		final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		return new ExecOpRequestSPARQL(request, fm);
	}

	protected Op createFilter( final Iterable<SolutionMapping> solMaps ) {
		if ( varsInSubQuery.isEmpty() ) {
			return representQueryPatternAsJenaOp(query);
		}
		Expr disjunction = null;
		boolean solMapsContainBlankNodes = false;
		for (final SolutionMapping s : solMaps) {
			final Binding b = SolutionMappingUtils.restrict(s.asJenaBinding(), varsInSubQuery);
			if ( SolutionMappingUtils.containsBlankNodes(b) ) {
				solMapsContainBlankNodes = true;
				continue;
			}

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
			return solMapsContainBlankNodes ? null : representQueryPatternAsJenaOp(query);
		}

		return OpFilter.filter(disjunction, representQueryPatternAsJenaOp(query));
	}

}
