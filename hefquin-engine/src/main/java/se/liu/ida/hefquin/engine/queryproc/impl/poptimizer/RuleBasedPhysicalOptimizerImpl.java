package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;

public class RuleBasedPhysicalOptimizerImpl extends PhysicalOptimizerBase
{
	private static final Logger log = LoggerFactory.getLogger( RuleBasedPhysicalOptimizerImpl.class );

	protected final List<RewritingRuleForPhysicalOptimization> rewritingRules;

	public RuleBasedPhysicalOptimizerImpl( final List<RewritingRuleForPhysicalOptimization> rewritingRules ) {
		assert rewritingRules != null;
		this.rewritingRules = rewritingRules;
	}

	@Override
	protected Pair<PhysicalPlan, PhysicalOptimizationStats> optimize( final PhysicalPlan initialPhysicalPlan,
	                                                                  final QueryProcContextExt ctx ) throws PhysicalOptimizationException {
		log.debug( "Starting physical optimization with {} rewritingRules", rewritingRules.size() );
		PhysicalPlan resultPlan = initialPhysicalPlan;
		for ( final RewritingRuleForPhysicalOptimization r : rewritingRules ) {
			log.debug( "Applying rewriting rule {} to physical plan", r.getClass().getSimpleName() );
			resultPlan = r.apply(resultPlan, ctx);
			log.debug( "Finished applying rewriting rule {} to plan", r.getClass().getSimpleName() );
		}

		log.debug( "Physical optimization finished" );

		// Return the optimized plan and the collected statistics.
		return new Pair<>( resultPlan, new PhysicalOptimizationStatsImpl() );
	}

	@Override
	public boolean assumesLogicalMultiwayJoins() {
		return false;
	}

	@Override
	protected boolean keepMultiwayJoinsInInitialPhysicalPlan() {
		return false;
	}

}
