package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithStatistics;

import java.util.*;

/**
 * This class implements a query optimizer that builds left-deep query plans,
 * for which it uses a greedy approach to determine the join order based on vocabulary mapping and cardinality estimation.
 * Specifically, choose the first subPlan with the smallest cardinality, then the subPlan with the same vocabulary mapping is selected.
 * If there are multiple subPlans with the same vocabulary mapping, the order depends on estimated cardinality;
 * If such a subPlan does not exist, simply select the subPlan with the lowest cardinality as the next subPlan.
 *
 * After determining join ordering, the physical plan can be constructed using bind join.
 */
public class ReduceVocRewritingJoinPlanOptimizerImpl extends CardinalityBasedJoinPlanOptimizerBase
{
    public ReduceVocRewritingJoinPlanOptimizerImpl(final FederationAccessManager fedAccessMgr ) {
        super(fedAccessMgr);
    }

    @Override
    public EnumerationAlgorithm initializeEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {

        return new CardinalityBasedJoinPlanOptimizerBase.GreedyConstructionAlgorithm(subplans) {
            @Override
            protected PhysicalPlan determineJoinOrderAndConstructPlan( final PhysicalPlanWithStatistics firstSubPlan, final List<PhysicalPlanWithStatistics> subPlansWithStatistics, final Comparator<PhysicalPlanWithStatistics> orderBasedOnCard) {
                final PriorityQueue<PhysicalPlanWithStatistics> orderedCandidateSubPlans = new PriorityQueue<>(orderBasedOnCard);
                PhysicalPlanWithStatistics previousSelectedSubPlan = firstSubPlan;
                PhysicalPlan currentJoinPlan = previousSelectedSubPlan.plan;

                while ( !subPlansWithStatistics.isEmpty() ){
                    // Identify subplans that have join variables with the selected subplan as new candidateSubPlans.
                    List<PhysicalPlanWithStatistics> tmpCandidateSubPlans = getSubPlansContainVars( currentJoinPlan.getExpectedVariables(), subPlansWithStatistics );
                    if( tmpCandidateSubPlans.isEmpty() ){
                        tmpCandidateSubPlans = subPlansWithStatistics;
                    }

                    // Keep candidate SubPlans that use the same vocabulary mapping with the previous selected SubPlan.
                    // If 'candidateSubPlans' is empty, use all subplans in 'tmpCandidateSubPlans' as candidate subplans.
                    List<PhysicalPlanWithStatistics> candidateSubPlans = getSubPlansWithSameVoc( previousSelectedSubPlan.getVocabularyMappings(), tmpCandidateSubPlans);
                    if( candidateSubPlans.isEmpty() ){
                        candidateSubPlans = tmpCandidateSubPlans;
                    }

                    // The candidateSubPlans are added to the ordered list 'orderedCandidateSubPlans'
                    orderedCandidateSubPlans.addAll(candidateSubPlans);

                    // select the first element (with the lowest cardinality) from orderedCandidateSubPlans as the nextSelectedSubPlan
                    // Remove the selected subPlan from subPlansWithStatistics and clear orderedCandidateSubPlans for the next loop
                    final PhysicalPlanWithStatistics nextSelectedSubPlan = orderedCandidateSubPlans.poll();
                    subPlansWithStatistics.remove(nextSelectedSubPlan);
                    orderedCandidateSubPlans.clear();

                    // Construct a physical plan using bind join
                    currentJoinPlan = constructPlanWithBJ( currentJoinPlan, nextSelectedSubPlan.plan );
                    previousSelectedSubPlan = nextSelectedSubPlan;
                }

                return currentJoinPlan;
            }
        };
    }

    /**
     * Iterate through the remaining subplans and selected those that have the same vocabulary mapping in the given list of vocabulary mappings.
     */
    protected List<PhysicalPlanWithStatistics> getSubPlansWithSameVoc( final List<VocabularyMapping> vms, final List<PhysicalPlanWithStatistics> subPlansWithStatistics ) {
        final List<PhysicalPlanWithStatistics> subPlansWithSameVoc = new ArrayList<>();
        for ( final PhysicalPlanWithStatistics subplan: subPlansWithStatistics ) {
            final List<VocabularyMapping> vmsNext = subplan.getVocabularyMappings();
            vmsNext.retainAll(vms);
            if ( !vmsNext.isEmpty() ){
                subPlansWithSameVoc.add(subplan);
            }
        }

        return subPlansWithSameVoc;
    }

    /**
     * Construct a physical plan using bind join when it is possible:
     * If both currentPlan and nextPlan have PhysicalOpLocalToGlobal as root operator, and use the same vocabulary mapping,
     * the physical plan can be simplified as when unary operator can be applied:
     * tpAdd^{tp}_{fm}(g2l^{vm}(l2g^{vm}(p))) --> tpAdd^{tp}_{fm}(p) (see Equation 2 of Proposition 2)
     */
    protected PhysicalPlan constructPlanWithBJ( final PhysicalPlan currentPlan, final PhysicalPlan nextPlan ) {
        final PhysicalOperator currentPop = currentPlan.getRootOperator();
        final PhysicalOperator nextPop = nextPlan.getRootOperator();
        if ( currentPop instanceof PhysicalOpLocalToGlobal
                && nextPop instanceof PhysicalOpLocalToGlobal ) {
            final VocabularyMapping vm1 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) currentPop).getLogicalOperator()).getVocabularyMapping();
            final VocabularyMapping vm2 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) nextPop).getLogicalOperator()).getVocabularyMapping();
            // TODO: check if every rule is either an equivalence on concepts or an equivalence on roles.
            if( vm1.equals(vm2) ){
                final PhysicalPlan temPlan = PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan.getSubPlan(0), nextPlan.getSubPlan(0));
                final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(vm1);
                return PhysicalPlanFactory.createPlan( new PhysicalOpLocalToGlobal(l2g), temPlan );
            }
            else {
                return PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan, nextPlan);
            }
        }

        if ( currentPop instanceof PhysicalOpLocalToGlobal
                && nextPop instanceof PhysicalOpMultiwayUnion ) {
            final int numberOfSubPlansUnderUnion = nextPlan.numberOfSubPlans();
            final PhysicalPlan[] newUnionSubPlans = new PhysicalPlan[numberOfSubPlansUnderUnion];

            for ( int i = 0; i < numberOfSubPlansUnderUnion; i++ ) {
                final PhysicalPlan oldSubPlan = nextPlan.getSubPlan(i);
                final PhysicalPlan newSubPlan;
                if ( oldSubPlan.getRootOperator() instanceof PhysicalOpLocalToGlobal ){
                    final VocabularyMapping vm1 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) currentPop).getLogicalOperator()).getVocabularyMapping();
                    final VocabularyMapping vm2 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) oldSubPlan.getRootOperator()).getLogicalOperator()).getVocabularyMapping();
                    // TODO: check if every rule is either an equivalence on concepts or an equivalence on roles.
                    if( vm1.equals(vm2) ){
                        final PhysicalPlan temPlan = PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan.getSubPlan(0), oldSubPlan.getSubPlan(0));
                        final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(vm1);
                        newSubPlan = PhysicalPlanFactory.createPlan( new PhysicalOpLocalToGlobal(l2g), temPlan );
                    }
                    else {
                        newSubPlan = PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan, oldSubPlan);
                    }
                }
                else {
                    newSubPlan = PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan, oldSubPlan);
                }
                newUnionSubPlans[i] = newSubPlan;
            }

            return PhysicalPlanFactory.createPlan( LogicalOpMultiwayUnion.getInstance(), newUnionSubPlans );
        }
        else {
            return PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan, nextPlan);
        }
    }

    /**
     * Construct a physical plan using symmetric hash join:
     * If both currentPlan and nextPlan have PhysicalOpLocalToGlobal as root operator,
     * and every rule is either an equivalence on concepts or an equivalence on roles,
     * the physical plan can be simplified as:
     * join( l2g^{vm}(p1), l2g^{vm}(p2) ) --> l2g^{vm}( join (p1, p2) ), (see Equation 1 of Proposition 2)
     */
    protected PhysicalPlan constructPlanWithSHJ( final PhysicalPlan currentPlan, final PhysicalPlan nextPlan ) {
        final PhysicalOperator currentPop = currentPlan.getRootOperator();
        final PhysicalOperator nextPop = nextPlan.getRootOperator();
        if ( currentPop instanceof PhysicalOpLocalToGlobal
                && nextPop instanceof PhysicalOpLocalToGlobal ) {
            final VocabularyMapping vm1 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) currentPop).getLogicalOperator()).getVocabularyMapping();
            final VocabularyMapping vm2 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) nextPop).getLogicalOperator()).getVocabularyMapping();
            //TODO: check if every rule is either an equivalence on concepts or an equivalence on roles.
            if( vm1.equals(vm2) ){
                final PhysicalPlan temPlan = PhysicalPlanFactory.createPlanWithJoin(currentPlan.getSubPlan(0), nextPlan.getSubPlan(0));
                final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(vm1);
                return PhysicalPlanFactory.createPlan( new PhysicalOpLocalToGlobal(l2g), temPlan );
            }
            else {
                return PhysicalPlanFactory.createPlanWithJoin(currentPlan, nextPlan);
            }
        }
        else {
            return PhysicalPlanFactory.createPlanWithJoin(currentPlan, nextPlan);
        }
    }

}
