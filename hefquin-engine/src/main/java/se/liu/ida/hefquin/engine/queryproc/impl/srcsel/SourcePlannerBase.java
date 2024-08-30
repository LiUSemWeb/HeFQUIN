package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import org.apache.jena.sparql.algebra.Op;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningException;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;

public abstract class SourcePlannerBase implements SourcePlanner
{
	protected final QueryProcContext ctxt;

	protected SourcePlannerBase( final QueryProcContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	public final Pair<LogicalPlan, SourcePlanningStats> createSourceAssignment( final Query query )
			throws SourcePlanningException
	{
		final Op jenaOp;
		if ( query instanceof GenericSPARQLGraphPatternImpl1 ) {
			@SuppressWarnings("deprecation")
			final Op o = ( (GenericSPARQLGraphPatternImpl1) query ).asJenaOp();
			jenaOp = o;
		}
		else if ( query instanceof GenericSPARQLGraphPatternImpl2 ) {
			jenaOp = ( (GenericSPARQLGraphPatternImpl2) query ).asJenaOp();
		}
		else {
			throw new UnsupportedOperationException( query.getClass().getName() );
		}

		return createSourceAssignment(jenaOp);
	}

	protected abstract Pair<LogicalPlan, SourcePlanningStats> createSourceAssignment(Op jenaOp)
			throws SourcePlanningException;
}
