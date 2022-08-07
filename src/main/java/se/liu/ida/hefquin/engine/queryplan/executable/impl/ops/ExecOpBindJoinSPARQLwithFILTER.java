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

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl2;

public class ExecOpBindJoinSPARQLwithFILTER extends BaseForExecOpBindJoinSPARQL
{
	public ExecOpBindJoinSPARQLwithFILTER( final TriplePattern query,
	                                       final SPARQLEndpoint fm,
	                                       final boolean useOuterJoinSemantics,
	                                       final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
	}

	public ExecOpBindJoinSPARQLwithFILTER( final BGP query,
	                                       final SPARQLEndpoint fm,
	                                       final boolean useOuterJoinSemantics,
	                                       final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
	}

	public ExecOpBindJoinSPARQLwithFILTER( final SPARQLGraphPattern query,
	                                       final SPARQLEndpoint fm,
	                                       final boolean useOuterJoinSemantics,
	                                       final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> solMaps ) {
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
