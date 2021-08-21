package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.*;

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
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;

public class ExecOpBindJoinSPARQLwithUNION extends ExecOpGenericBindJoinWithRequestOps<TriplePattern,SPARQLEndpoint>
{
	protected final List<Var> varsInTP;

	public ExecOpBindJoinSPARQLwithUNION( final TriplePattern query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInTP = new ArrayList<>( QueryPatternUtils.getVariablesInPattern(query) );
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> solMaps ) {
		final Op op = createUnion(solMaps);
		if ( op == null ) {
			return null;
		}

		final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		return new ExecOpRequestSPARQL(request, fm);
	}

	protected Op createUnion(final Iterable<SolutionMapping> solMaps) {
		final Op tp = new OpTriple(query.asJenaTriple());
		if (varsInTP.isEmpty()) return tp;

		final Set<Expr> conjunctions = new HashSet<>();
		boolean conjunctionsMustBeNonEmpty = false;
		for ( final SolutionMapping s : solMaps) {
			final Binding b = SolutionMappingUtils.restrict(s.asJenaBinding(), varsInTP);
			// If the current solution mapping does not have any variables in common with
			// the triple pattern of this operator, then every matching triple is a join partner
			// for the current solution mapping. Hence, in this case, we may simply retrieve
			// all matching triples (i.e., no need for putting together the UNION pattern).
			if (b.size() == 0) return tp;

			if ( SolutionMappingUtils.containsBlankNodes(b) ) {
				conjunctionsMustBeNonEmpty = true;
				continue;
			}

			Expr conjunction = null;
			for (final Var v : varsInTP){
				if (! b.contains(v)) continue;
				final Node uri = b.get(v);
				final Expr expr = new E_Equals(new ExprVar(v), new NodeValueNode(uri));
				if (conjunction == null){
					conjunction = expr;
				} else {
					conjunction = new E_LogicalAnd(conjunction, expr);
				}
			}
			conjunctions.add(conjunction);
		}

		if ( conjunctions.isEmpty() ) {
			return conjunctionsMustBeNonEmpty ? null : tp;
		}

		Op union = null;
		for (final Expr conjunction : conjunctions) {
			final Op filter = OpFilter.filter(conjunction, tp);
			if (union == null) {
				union = filter;
			} else {
				union = new OpUnion(union, filter);
			}
		}
		return union;
	}

}
