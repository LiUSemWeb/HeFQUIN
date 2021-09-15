package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;
import se.liu.ida.hefquin.engine.utils.RandomizedSelection;

import java.util.*;

public class EvolutionaryAlgorithmQueryOptimizer implements QueryOptimizer {
    protected final QueryOptimizationContext ctxt;
    protected final int nmCandidates;
    protected final int nmSurvivors;
    protected final int nmSteps;

    protected final RandomizedSelection randomizedSelection = new RandomizedSelection<>();
    protected final Random rand = new Random();

    public EvolutionaryAlgorithmQueryOptimizer( final QueryOptimizationContext ctxt,
                                                final int nmCandidates, final int nmSurvivors, int nmSteps ) {
        assert ctxt != null;
        assert (nmCandidates - nmSurvivors) > 0;

        this.ctxt = ctxt;
        this.nmCandidates = nmCandidates;
        this.nmSurvivors = nmSurvivors;
        this.nmSteps = nmSteps;
    }

    @Override
    public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws QueryOptimizationException {
        final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert( initialPlan, false );

        return optimizePlan( initialPhysicalPlan );
    }

    public PhysicalPlan optimizePlan( final PhysicalPlan plan ) throws QueryOptimizationException {
        final RuleApplicationsOfPlan ruleApplicationCache = new RuleApplicationsOfPlan( new RuleInstances() );
        // initialize the first generation
        List<PhysicalPlanWithCost> currentGen = generateFirstGen( plan, ruleApplicationCache );

        // TODO: extend this implementation with different sorts of termination criteria
        // termination criteria: number of generations
        for ( int gen = 1; gen < nmSteps; gen++ ){
            currentGen = generateNextGen( currentGen, ruleApplicationCache );
        }
        return findPlanWithSmallestCost(currentGen).getPlan();
    }

    protected List<PhysicalPlanWithCost> generateFirstGen( final PhysicalPlan plan, final RuleApplicationsOfPlan cache ) throws QueryOptimizationException {
        final List<PhysicalPlan> currentGen = new ArrayList<>();
        currentGen.add(plan);

        // determine all rule applications for the initial plan
        final Set<RuleApplication> ruleApps = cache.determineRuleApplications( plan );

        // generate candidates for the first generation
        while ( (currentGen.size() < nmCandidates) && (ruleApps.size() > 0) ){
            // Pick one rule application based on priority, and apply the selected rule application to the initial plan
            final RuleApplication ruleApplication = (RuleApplication) randomizedSelection.pickOne( ruleApps );
            currentGen.add( ruleApplication.getResultingPlan() );

            // remove the applied rule application from the set of rule applications
            ruleApps.remove( ruleApplication );
        }

        return annotatePlansWithCost(currentGen);
    }

    protected List<PhysicalPlanWithCost> generateNextGen( final List<PhysicalPlanWithCost> currentGen, final RuleApplicationsOfPlan cache ) throws QueryOptimizationException {
        final Set<PhysicalPlanWithCost> parentSet = new HashSet<>( currentGen );
        final List<PhysicalPlan> newCandidates = new ArrayList<>();

        while ( newCandidates.size() < nmCandidates && parentSet.size() > 0 ) {
            // pick a parent from the current generation
            final PhysicalPlan parent = ( (PhysicalPlanWithCost)randomizedSelection.pickOne( parentSet ) ).getPlan() ;

            // determine all possible applications of rewriting rules for the parent plan
            final Set<RuleApplication> ruleApps = cache.determineRuleApplications( parent );

            if ( ruleApps.size() > 0) {
                // Pick one rewriting rule based on priority
                final RuleApplication ruleApplication = (RuleApplication) randomizedSelection.pickOne( ruleApps );
                newCandidates.add( ruleApplication.getResultingPlan() );
                ruleApps.remove(ruleApplication);
            }
            parentSet.remove( parent );
        }
        currentGen.addAll( annotatePlansWithCost(newCandidates) );

        // select the next generation from all candidates
        return selectNextGenFromCandidates( currentGen );
    }

    protected List<PhysicalPlanWithCost> annotatePlansWithCost( final List<PhysicalPlan> plans ) throws QueryOptimizationException {
        final Double[] costs;
        try {
            costs = CostEstimationUtils.getEstimates( ctxt.getCostModel(), plans );
        } catch ( CostEstimationException e) {
            throw new QueryOptimizationException("Unexpected exception when determining the cost for plans.", e);
        }

        final List<PhysicalPlanWithCost> plansWithCost = new ArrayList<>();
        for ( int i = 0; i < plans.size(); i++ ) {
            plansWithCost.add( new PhysicalPlanWithCost( plans.get(i), costs[i] ) );
        }

        return plansWithCost;
    }

    protected List<PhysicalPlanWithCost> selectNextGenFromCandidates( final List<PhysicalPlanWithCost> planWithCosts ) {
        final List<PhysicalPlanWithCost> currentGen = new ArrayList<>();

        for ( int i = 0; i < (nmCandidates-nmSurvivors); i++ ) {
            final PhysicalPlanWithCost planWithSmallestCost = findPlanWithSmallestCost( planWithCosts );
            currentGen.add( planWithSmallestCost );
            planWithCosts.remove( planWithSmallestCost );
        }

        for ( int i = 0; i < nmSurvivors; i++ ) {
            final PhysicalPlanWithCost survivor = planWithCosts.get( rand.nextInt(planWithCosts.size()) );
            currentGen.add( survivor );
            planWithCosts.remove( survivor );
        }

        return currentGen;
    }

    protected PhysicalPlanWithCost findPlanWithSmallestCost( final List<PhysicalPlanWithCost> plansWithCost ) {
        PhysicalPlanWithCost bestPlan = plansWithCost.get(0);
        double min = bestPlan.getWeight();

        for ( int i = 1; i < plansWithCost.size(); i++) {
            final PhysicalPlanWithCost p = plansWithCost.get(i);
            if ( p.getWeight() <= min ) {
                min = p.getWeight();
                bestPlan = p;
            }
        }
        return bestPlan;
    }

}
