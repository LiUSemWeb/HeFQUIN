package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PhysicalPlanWithStatisticsUtils {
    /**
     * Compares all available subplans in terms of
     * their respective cardinality returns the one with the lowest cardinality.
     */
    public static PhysicalPlanWithStatistics chooseFirstSubPlan( final List<PhysicalPlanWithStatistics> subPlansWithStatistics ) {
        PhysicalPlanWithStatistics bestPlanWithCandidate = subPlansWithStatistics.get(0);
        int lowestCost = bestPlanWithCandidate.getCardinality();

        for ( int i = 1; i < subPlansWithStatistics.size(); i++ ) {
            PhysicalPlanWithStatistics current = subPlansWithStatistics.get(i);
            if ( current.getCardinality() < lowestCost ) {
                lowestCost = current.getCardinality();
                bestPlanWithCandidate = current;
            }
        }

        return bestPlanWithCandidate;
    }

    /**
     * Iterate through the remaining subplans and selected those that contain any of the variables in the given set of variables.
     */
    public static List<PhysicalPlanWithStatistics> getSubPlansContainVars( final ExpectedVariables vars, final List<PhysicalPlanWithStatistics> subPlansWithStatistics ) {
        final List<PhysicalPlanWithStatistics> subPlansContainsVars = new ArrayList<>();
        for ( final PhysicalPlanWithStatistics subplan: subPlansWithStatistics ) {
            final Set<Var> joinVars = ExpectedVariablesUtils.intersectionOfAllVariables( vars, subplan.plan.getExpectedVariables() );
            if ( joinVars != null && !joinVars.isEmpty() ){
                subPlansContainsVars.add(subplan);
            }
        }

        return subPlansContainsVars;
    }

    /**
     * Iterate through the remaining subplans and selected those that have the same vocabulary mapping in the given list of vocabulary mappings.
     */
    public static List<PhysicalPlanWithStatistics> getSubPlansWithSameVoc( final List<VocabularyMapping> vms, final List<PhysicalPlanWithStatistics> subPlansWithStatistics ) {
        final List<PhysicalPlanWithStatistics> subPlansWithSameVoc = new ArrayList<>();
        for ( final PhysicalPlanWithStatistics subplan: subPlansWithStatistics ) {
            final List<VocabularyMapping> vmsNext = subplan.getVocabularyMappings();
            vms.retainAll(vmsNext);
            if ( !vms.isEmpty() ){
                subPlansWithSameVoc.add(subplan);
            }
        }

        return subPlansWithSameVoc;
    }

}