package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
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

public class ExecOpBindJoinSPARQLwithUNION extends ExecOpGenericBindJoin<TriplePattern,SPARQLEndpoint> {

	private Set<Var> varsInTP;

	public ExecOpBindJoinSPARQLwithUNION( final TriplePattern query, final SPARQLEndpoint fm) {
		super(query, fm);
		varsInTP = QueryPatternUtils.getVariablesInPattern(query);
	}

	@Override
	protected Iterable<? extends SolutionMapping> fetchSolutionMappings( final Iterable<SolutionMapping> solMaps,
			final ExecutionContext execCxt) {
		final Op op = createUnion(solMaps);
		final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		final SolMapsResponse response = execCxt.getFederationAccessMgr().performRequest(request, fm);
		return response.getSolutionMappings();
	}

	protected Op createUnion(final Iterable<SolutionMapping> solMaps) {
		Op tp = new OpTriple(query.asJenaTriple());
		if (varsInTP.isEmpty()) return tp;
		Op union = null;
		for ( final SolutionMapping s : solMaps) {
			final Binding b = SolutionMappingUtils.restrict(s.asJenaBinding(), varsInTP);
			if (b.size() == 0) continue;
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
			final Op filter = OpFilter.filter(conjunction, tp);
			if (union == null) {
				union = filter;
			} else {
				union = new OpUnion(union, filter);
			}
		}
		if (union == null) return tp;
		return union;
	}

}
