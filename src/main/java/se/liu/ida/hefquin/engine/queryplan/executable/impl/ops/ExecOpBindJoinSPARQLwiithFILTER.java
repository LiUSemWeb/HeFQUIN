package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBindJoinSPARQLwiithFILTER extends ExecOpGenericBindJoin<TriplePattern,SPARQLEndpoint> {
	
	protected final Set<Var> varsInTP;

	public ExecOpBindJoinSPARQLwiithFILTER(TriplePattern query, SPARQLEndpoint fm) {
		super(query, fm);
		varsInTP = QueryPatternUtils.getVariablesInPattern(query);
	}

	@Override
	protected Iterable<? extends SolutionMapping> fetchSolutionMappings(Iterable<SolutionMapping> solMaps,
			ExecutionContext execCxt) {
		final Op op = createFilter(solMaps);
		final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		final SolMapsResponse response = execCxt.getFederationAccessMgr().performRequest(request, fm);
		return response.getSolutionMappings();
	}

	private Op createFilter(Iterable<SolutionMapping> solMaps) {
		Expr disjunction = null;
		boolean firstDisj = true;
		for (final SolutionMapping s : solMaps) {
			Binding b = SolutionMappingUtils.restrict(s, varsInTP).asJenaBinding();
			final ExprList exprs = new ExprList();
			Expr conjunction = null;
			boolean firstConj = true;
			while (b.vars().hasNext()) {
				final Var v = b.vars().next();
				final Node uri = b.get(v);
				final Expr expr = new E_Equals(new ExprVar(v), new NodeValueNode(uri));
				if (firstConj) {
					conjunction = expr;
					firstConj = false;
				} else {
					conjunction = new E_LogicalAnd(conjunction, expr);
				}
			}
			if (firstDisj) {
				disjunction = conjunction;
				firstDisj = false;
			} else {
				disjunction = new E_LogicalOr(disjunction, conjunction);
			}
		}
		return OpFilter.filter(disjunction, new OpTriple(query.asJenaTriple()));
	}

}
