package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.QueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostEstimationException;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleInstances;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils.CostEstimationUtils;
import se.liu.ida.hefquin.engine.utils.RandomizedSelection;

import java.util.*;

public class EvolutionaryAlgorithmQueryOptimizer implements QueryOptimizer {
    protected final QueryOptimizationContext ctxt;
    protected final CostModel costModel;
    private final int nmCandidates;
    private final int nmSurvivors;
    private final int nmSteps;
    protected final RuleApplicationsOfPlan ruleApplicationCache = new RuleApplicationsOfPlan( new RuleInstances() );

    public EvolutionaryAlgorithmQueryOptimizer( final QueryOptimizationContext ctxt, CostModel costModel,
                                                final int nmCandidates, final int nmSurvivors, int nmSteps ) {
        assert ctxt != null;

        this.ctxt = ctxt;
        this.costModel = costModel;
        this.nmCandidates = nmCandidates;
        this.nmSurvivors = nmSurvivors;
        this.nmSteps = nmSteps;
    }

    @Override
    public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws CostEstimationException {
        final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert( initialPlan, false );

        return optimizePlan( initialPhysicalPlan );
    }

    public PhysicalPlan optimizePlan( final PhysicalPlan plan ) throws CostEstimationException {
        // initialize the first generation
        List<PhysicalPlanWithCost> currentGen = generateTheFirstGen( plan );

        // termination condition of optimizing the plan: number of generations
        for ( int gen = 1; gen < nmSteps; gen++ ){
            currentGen = generateTheNextGen( currentGen );
        }
        return findPlanWithSmallestCost(currentGen).getPlan();
    }

    protected List<PhysicalPlanWithCost> generateTheFirstGen( final PhysicalPlan plan ) throws CostEstimationException {
        final List<PhysicalPlan> currentGen = new ArrayList<>();
        currentGen.add(plan);

        // determine all rule applications for the initial plan
        final Set<RuleApplication> ruleApps = ruleApplicationCache.determineRuleApplications( plan );

        // generate candidates for the first generation
        final RandomizedSelection<RuleApplication> ruleRandomizedSelect = new RandomizedSelection<>();
        while ( currentGen.size() < nmCandidates && ruleApps.size() > 0 ){
            // Pick one rule application based on priority, and apply the selected rule application to the initial plan
            final RuleApplication ruleApplication = ruleRandomizedSelect.pickOne( ruleApps );
            currentGen.add( ruleApplication.getResultingPlan() );

            // remove the applied rule application from the set of rule applications
            ruleApps.remove( ruleApplication );
        }
        ruleApplicationCache.replaceRuleApplications( plan, ruleApps);

        return annotatePlansWithCost(currentGen);
    }

    protected List<PhysicalPlanWithCost> generateTheNextGen( final List<PhysicalPlanWithCost> currentGen ) throws CostEstimationException {
        List<PhysicalPlan> newCandidates = new ArrayList<>();

        while ( newCandidates.size() < nmCandidates ) {
            // pick a parent from the current generation
            final RandomizedSelection<PhysicalPlanWithCost> planRandomizedSelect = new RandomizedSelection<>();
            final PhysicalPlan parent = ( planRandomizedSelect.pickOne( new HashSet<>(currentGen) )).getPlan();

            // determine all possible applications of rewriting rules for the parent plan
            Set<RuleApplication> ruleApps = ruleApplicationCache.determineRuleApplications( parent );

            final RandomizedSelection<RuleApplication> ruleRandomizedSelect = new RandomizedSelection<>();
            if ( ruleApps.size() > 0) {
                // Pick one rewriting rule based on priority
                final RuleApplication ruleApplication = ruleRandomizedSelect.pickOne( ruleApps );
                newCandidates.add( ruleApplication.getResultingPlan() );
                ruleApplicationCache.removeRuleApplications( parent, ruleApplication );
            }
        }
        currentGen.addAll( annotatePlansWithCost(newCandidates) );

        // select the next generation from all candidates
        return selectNextGenFromCandidates( currentGen );
    }

    protected List<PhysicalPlanWithCost> annotatePlansWithCost( final List<PhysicalPlan> plans ) throws CostEstimationException {
        final Double[] costs = CostEstimationUtils.getEstimates( costModel, plans );

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

        Random rand = new Random();
        for ( int i = 0; i < nmSurvivors; i++ ) {
            PhysicalPlanWithCost survivorWithCost = planWithCosts.get( rand.nextInt(planWithCosts.size()) );
            currentGen.add( survivorWithCost );
            planWithCosts.remove( survivorWithCost );
        }

        return currentGen;
    }

    protected PhysicalPlanWithCost findPlanWithSmallestCost( final List<PhysicalPlanWithCost> plansWithCost ) {
        final PhysicalPlanWithCost bestPlan = plansWithCost.get(0);
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
