package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
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
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBindJoinSPARQLwithFILTER extends ExecOpGenericBindJoin<TriplePattern,SPARQLEndpoint> {
	
	protected final Set<Var> varsInTP;

	public ExecOpBindJoinSPARQLwithFILTER( final TriplePattern query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInTP = QueryPatternUtils.getVariablesInPattern(query);
	}

	@Override
	protected Iterable<SolutionMapping> fetchSolutionMappings(
			final Iterable<SolutionMapping> solMaps,
			final ExecutionContext execCxt )
					throws ExecOpExecutionException
	{
		final Op op = createFilter(solMaps);
		final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		final SolMapsResponse response;
		try {
			response = execCxt.getFederationAccessMgr().performRequest(request, fm);
		}
		catch ( final FederationAccessException ex ) {
			throw new ExecOpExecutionException("An exception occurred when executing a request issued by this bind join.", ex, this);
		}
		return response.getSolutionMappings();
	}

	protected Op createFilter( final Iterable<SolutionMapping> solMaps ) {
		if (varsInTP.isEmpty()) 
			return new OpTriple(query.asJenaTriple());
		Expr disjunction = null;
		for (final SolutionMapping s : solMaps) {
			final Binding b = SolutionMappingUtils.restrict(s.asJenaBinding(), varsInTP);
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
		if (disjunction == null) return new OpTriple(query.asJenaTriple());
		return OpFilter.filter(disjunction, new OpTriple(query.asJenaTriple()));
	}

}
