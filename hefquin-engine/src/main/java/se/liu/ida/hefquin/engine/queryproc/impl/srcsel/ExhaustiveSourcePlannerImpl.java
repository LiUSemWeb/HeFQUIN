package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.BasicPattern;

import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;

/**
 * This implementation of {@link SourcePlanner} assigns every triple
 * pattern of the given query to every federation member known in the
 * federation catalog. In other words, this source planner creates what
 * we call an 'exhaustive source assignment' in the FedQPL paper (see
 * Definition 9 in Section 5.2 of the paper).
 *
 * This source planner assumes that the given queries do not contain
 * any SERVICE clauses.
 */
public class ExhaustiveSourcePlannerImpl extends ServiceClauseBasedSourcePlannerImpl
{
	public ExhaustiveSourcePlannerImpl( final QueryProcContext ctxt ) {
		super(ctxt);
	}

	@Override
	protected LogicalPlan createPlan( final Op jenaOp ) {
		if ( jenaOp instanceof OpBGP ) {
			return createPlanForBGP( (OpBGP) jenaOp );
		}
		else if ( jenaOp instanceof OpService ) {
			throw new IllegalArgumentException( "queries with SERVICE patterns are not supported by this source planner (" + getClass().getName() + ")" ); 
		}

		return super.createPlan(jenaOp);
	}

	protected LogicalPlan createPlanForBGP( final OpBGP bgpOp ) {
		final BasicPattern bgp = bgpOp.getPattern();
		assert ! bgp.isEmpty();

		if ( bgp.size() == 1 ) {
			return createSubPlanForTP( bgp.get(0) );
		}

		final List<LogicalPlan> subPlans = new ArrayList<>();
		for ( final Triple tp : bgp.getList() ) {
			subPlans.add( createSubPlanForTP(tp) );
		}

		return LogicalPlanUtils.createPlanWithMultiwayJoin(subPlans);
	}

	protected LogicalPlan createSubPlanForTP( final Triple tp ) {
		final Set<FederationMember> allFMs = ctxt.getFederationCatalog().getAllFederationMembers();
		assert ! allFMs.isEmpty();

		if ( allFMs.size() == 1 ) {
			return createRequestSubPlan( tp, allFMs.iterator().next() );
		}

		final List<LogicalPlan> reqSubPlans = new ArrayList<>();
		for ( final FederationMember fm : allFMs ) {
			reqSubPlans.add( createRequestSubPlan(tp, fm) );
		}

		return LogicalPlanUtils.createPlanWithMultiwayUnion(reqSubPlans);
	}

	protected LogicalPlan createRequestSubPlan( final Triple tp, final FederationMember fm ) {
		final TriplePatternRequest req = new TriplePatternRequestImpl( new TriplePatternImpl(tp) );
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(fm, req);
		return new LogicalPlanWithNullaryRootImpl(reqOp);
	}
}
