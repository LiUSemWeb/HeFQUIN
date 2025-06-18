package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.serializer.FormatterElement;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

/**
 * Implementation of (a batching version of) the bind join algorithm that
 * uses FILTERs to capture the potential join partners that are sent to the
 * federation member.
 *
 * For more details about the actual implementation of the algorithm, and its
 * extra capabilities, refer to {@link BaseForExecOpBindJoinWithRequestOps}.
 */
public class ExecOpBindJoinSPARQLwithFILTER extends BaseForExecOpBindJoinSPARQL
{
	public final static int DEFAULT_BATCH_SIZE = BaseForExecOpBindJoinWithRequestOps.DEFAULT_BATCH_SIZE;

	protected final Element pattern;

	/**
	 * @param query - the graph pattern to be evaluated (in a bind-join
	 *          manner) at the federation member given as 'fm'
	 *
	 * @param fm - the federation member targeted by this operator
	 *
	 * @param inputVars - the variables to be expected in the solution
	 *          mappings that will be pushed as input to this operator
	 *
	 * @param useOuterJoinSemantics - <code>true</code> if the 'query' is to
	 *          be evaluated under outer-join semantics; <code>false</code>
	 *          for inner-join semantics
	 *
	 * @param batchSize - the number of solution mappings to be included in
	 *          each bind-join request; this value must not be smaller than
	 *          {@link #minimumRequestBlockSize}; as a default value for this
	 *          parameter, use {@link #DEFAULT_BATCH_SIZE}
	 *
	 * @param collectExceptions - <code>true</code> if this operator has to
	 *          collect exceptions (which is handled entirely by one of the
	 *          super classes); <code>false</code> if the operator should
	 *          immediately throw every {@link ExecOpExecutionException}
	 */
	public ExecOpBindJoinSPARQLwithFILTER( final SPARQLGraphPattern query,
	                                       final SPARQLEndpoint fm,
	                                       final ExpectedVariables inputVars,
	                                       final boolean useOuterJoinSemantics,
	                                       final int batchSize,
	                                       final boolean collectExceptions ) {
		super(query, fm, inputVars, useOuterJoinSemantics, batchSize, collectExceptions);

		pattern = QueryPatternUtils.convertToJenaElement(query);
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOp( final Set<Binding> solMaps ) {
		return createExecutableReqOp(solMaps, pattern, fm);
	}


	// ---- helper functions ---------

	public static NullaryExecutableOp createExecutableReqOp( final Set<Binding> solMaps,
	                                                         final Element pattern,
	                                                         final SPARQLEndpoint fm ) {
		final Expr expr = createFilterExpression(solMaps);

		final ElementGroup group = new ElementGroup();
		group.addElement( pattern );
		group.addElement( new ElementFilter(expr) );

		final SPARQLGraphPattern patternForReq = new GenericSPARQLGraphPatternImpl1(group);
		final SPARQLRequest request = new SPARQLRequestImpl(patternForReq);
		return new ExecOpRequestSPARQL(request, fm, false);
	}

	public static Expr createFilterExpression( final Iterable<Binding> solMaps ) {
		Expr disjunction = null;
		for ( final Binding sm : solMaps ) {
			Expr conjunction = null;
			final Iterator<Var> vars = sm.vars();
			while ( vars.hasNext() ) {
				final Var v = vars.next();
				final Node n = sm.get(v);
				final Expr expr = new E_Equals( new ExprVar(v),
				                                new NodeValueNode(n) );

				if ( conjunction == null )
					conjunction = expr;
				else
					conjunction = new E_LogicalAnd(conjunction, expr);
			}

			assert conjunction != null;

			if ( disjunction == null )
				disjunction = conjunction;
			else
				disjunction = new E_LogicalOr(disjunction, conjunction);
		}

		assert disjunction != null;

		return disjunction;
	}

}
