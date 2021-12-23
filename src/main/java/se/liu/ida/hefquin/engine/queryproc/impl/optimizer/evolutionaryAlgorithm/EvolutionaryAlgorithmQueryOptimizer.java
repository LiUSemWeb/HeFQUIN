package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.PlanRewritingUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.PhysicalPlanWithCostUtils;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.utils.RandomizedSelection;

import java.util.*;

public class EvolutionaryAlgorithmQueryOptimizer implements QueryOptimizer {
    protected final QueryOptimizationContext ctxt;
    protected final int nmCandidates;
    protected final int nmSurvivors;
    protected final TerminationCriterion terminateCriterion;

    protected final RandomizedSelection<RuleApplication> ruleRandomizedSelect = new RandomizedSelection<>();
    protected final RandomizedSelection<PhysicalPlanWithCost> planRandomizedSelect = new RandomizedSelection<>();
    protected final Random rand = new Random();

    public EvolutionaryAlgorithmQueryOptimizer( final QueryOptimizationContext ctxt,
                                                final int nmCandidates, final int nmSurvivors,
                                                final TerminationCriterion terminateCriterion) {
        assert ctxt != null;
        assert (nmCandidates - nmSurvivors) > 0;

        this.ctxt = ctxt;
        this.nmCandidates = nmCandidates;
        this.nmSurvivors = nmSurvivors;
        this.terminateCriterion = terminateCriterion;
    }

    @Override
    public Pair<PhysicalPlan, QueryOptimizationStats> optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
        final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert( initialPlan, false );
        return optimize( initialPhysicalPlan );
    }

    public Pair<PhysicalPlan, QueryOptimizationStats> optimize( final PhysicalPlan plan ) throws QueryOptimizationException {
        final PlanRewritingUtils ruleApplicationCache = new PlanRewritingUtils( new RuleInstances() );
        // initialize the first generation
        Generation currentGen = generateFirstGen( plan, ruleApplicationCache );

        final List<Generation> previousGenerations = new ArrayList<>();
        while( ! terminateCriterion.readyToTerminate( currentGen, previousGenerations ) ) {
            previousGenerations.add(currentGen);
            currentGen = generateNextGen( currentGen, ruleApplicationCache );
        }
        final PhysicalPlanWithCost bestPlan = currentGen.bestPlan;

        final QueryOptimizationStatsImpl myStats = new QueryOptimizationStatsImpl();
        myStats.put( "numberOfGenerations", previousGenerations.size() + 1 );
        myStats.put( "costOfSelectedPlan", bestPlan.getWeight() );

		return new Pair<>(bestPlan.getPlan(), myStats);
    }

    protected Generation generateFirstGen( final PhysicalPlan plan, final PlanRewritingUtils cache ) throws QueryOptimizationException {
        final List<PhysicalPlan> currentGen = new ArrayList<>();
        currentGen.add(plan);

        // determine all rule applications for the initial plan
        final Set<RuleApplication> ruleApps = cache.getRuleApplications( plan );

        // generate candidates for the first generation
        while ( (currentGen.size() < nmCandidates) && (ruleApps.size() > 0) ){
            // Pick one rule application based on priority, and apply the selected rule application to the initial plan
            final RuleApplication ruleApplication = ruleRandomizedSelect.pickOne( ruleApps );
            currentGen.add( ruleApplication.getResultingPlan() );

            // remove the applied rule application from the set of rule applications
            ruleApps.remove( ruleApplication );
        }

        return new Generation( PhysicalPlanWithCostUtils.annotatePlansWithCost(ctxt.getCostModel(), currentGen) );
    }

    protected Generation generateNextGen( final Generation currentGen, final PlanRewritingUtils cache ) throws QueryOptimizationException {
        final Set<PhysicalPlanWithCost> parentSet = new HashSet<>( currentGen.plans );
        final List<PhysicalPlan> newCandidates = new ArrayList<>();

        while ( newCandidates.size() < nmCandidates && parentSet.size() > 0 ) {
            // pick a parent from the current generation
            final PhysicalPlanWithCost parent = planRandomizedSelect.pickOne( parentSet );

            // determine all possible applications of rewriting rules for the parent plan
            final Set<RuleApplication> ruleApps = cache.getRuleApplications( parent.getPlan() );
            if ( ruleApps.size() > 0) {
                // Pick one rewriting rule based on priority
                final RuleApplication ruleApplication = ruleRandomizedSelect.pickOne( ruleApps );
                newCandidates.add( ruleApplication.getResultingPlan() );
                ruleApps.remove(ruleApplication);
            }
            else {
                parentSet.remove(parent);
            }
        }

        final List<PhysicalPlanWithCost> candidatesWithCost = PhysicalPlanWithCostUtils.annotatePlansWithCost(ctxt.getCostModel(), newCandidates );
        candidatesWithCost.addAll( currentGen.plans );

        // select the next generation from all candidates
        return new Generation( selectNextGenFromCandidates( candidatesWithCost ) );
    }

    protected List<PhysicalPlanWithCost> selectNextGenFromCandidates( final List<PhysicalPlanWithCost> planWithCosts ) {
        final List<PhysicalPlanWithCost> currentGen = new ArrayList<>();

        for ( int i = 0; i < (nmCandidates-nmSurvivors); i++ ) {
            if ( planWithCosts.size() == 0 ) {
                break;
            }
            final PhysicalPlanWithCost planWithSmallestCost = PhysicalPlanWithCostUtils.findPlanWithLowestCost( planWithCosts );
            currentGen.add( planWithSmallestCost );
            planWithCosts.remove( planWithSmallestCost );
        }

        for ( int i = 0; i < nmSurvivors; i++ ) {
            if ( planWithCosts.size() == 0 ) {
                break;
            }
            final PhysicalPlanWithCost survivor = planWithCosts.get( rand.nextInt(planWithCosts.size()) );
            currentGen.add( survivor );
            planWithCosts.remove( survivor );
        }

        return currentGen;
    }

}
