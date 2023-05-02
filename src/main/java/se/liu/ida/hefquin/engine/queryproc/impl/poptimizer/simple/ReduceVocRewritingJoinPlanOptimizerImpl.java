package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.CardinalityBasedJoinPlanOptimizerUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithStatistics;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithStatisticsUtils;

import java.util.*;

/**
 * This class implements a query optimizer that builds left-deep query plans,
 * for which it uses a greedy approach to determine the join order based on vocabulary mapping and cardinality estimation.
 * Specifically, choose the first subPlan with the smallest cardinality, then the subPlan with the same vocabulary mapping is selected.
 * If there are multiple subPlans with the same vocabulary mapping, the order depends on estimated cardinality;
 * If such a subPlan does not exist, simply select the subPlan with the lowest cardinality as the next subPlan.
 *
 * After determining join ordering, the physical plan can be constructed using symmetric hash join or bind join.
 * {constructPlanWithSHJ} or {constructPlanWithBJ}
 */
public class ReduceVocRewritingJoinPlanOptimizerImpl extends JoinPlanOptimizerBase
{
    protected final FederationAccessManager fedAccessMgr;

    public ReduceVocRewritingJoinPlanOptimizerImpl(final FederationAccessManager fedAccessMgr ) {
        this.fedAccessMgr = fedAccessMgr;
    }

    @Override
    public EnumerationAlgorithm initializeEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {
        return new GreedyConstructionAlgorithm(subplans);
    }

    protected class GreedyConstructionAlgorithm implements EnumerationAlgorithm {
        protected final List<PhysicalPlan> subplans;

        public GreedyConstructionAlgorithm( final List<PhysicalPlan> subplans ) {
            this.subplans = subplans;
        }

        @Override
        public PhysicalPlan getResultingPlan() throws PhysicalOptimizationException
        {
            // Extract all request operators from the given list of subplans,
            // where the list of these operators is ordered in the order in which
            // the operators can be found by a depth-first traversal of the subplans.
            final List<LogicalOpRequest<?,?>> reqOpsOfAllSubPlans = new ArrayList<>();
            for ( final PhysicalPlan subplan : subplans ) {
            	reqOpsOfAllSubPlans.addAll(CardinalityBasedJoinPlanOptimizerUtils.extractAllRequestOpsFromSourceAssignment(subplan) );
            }

            // Next, get a list of cardinality estimates by performing cardinality
            // requests for the above list of request operators.
            final CardinalityResponse[] resps;
            try {
                resps = FederationAccessUtils.performCardinalityRequests(fedAccessMgr, reqOpsOfAllSubPlans);
            } catch (final FederationAccessException e) {
                throw new PhysicalOptimizationException("Issuing a cardinality request caused an exception.", e);
            }

            // Then, annotate each subplan (in the 'subplans') with all statistics relevant for the join planning
            // ( including cardinality, number of access, and a list of relevant federation members )
            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = associateCardWithSubPlans(resps);

            // To build a left-deep query plan, first select the subplan with the lowest estimated cardinality
            PhysicalPlanWithStatistics nextSelectedSubPlan = PhysicalPlanWithStatisticsUtils.chooseFirstSubPlan(subPlansWithStatistics);
            subPlansWithStatistics.remove(nextSelectedSubPlan);

            PhysicalPlan currentJoinPlan = nextSelectedSubPlan.plan;
            // The nextSubPlan is selected from subplans that have join variables with selected subplans.
            // To achieve this, we implement a custom comparator and create a PriorityQueue to store candidate subPlans in cardinality order.
            final Comparator<PhysicalPlanWithStatistics> orderBasedOnCard = (o1, o2) -> Integer.compare( o1.getCardinality(), o2.getCardinality() );
            final PriorityQueue<PhysicalPlanWithStatistics> orderedCandidateSubPlans = new PriorityQueue<>(orderBasedOnCard);

            while ( !subPlansWithStatistics.isEmpty() ){
                // Identify subplans that have join variables with the selected subplan as new candidateSubPlans.
                List<PhysicalPlanWithStatistics> tmpCandidateSubPlans = PhysicalPlanWithStatisticsUtils.getSubPlansContainVars( currentJoinPlan.getExpectedVariables(), subPlansWithStatistics );
                if ( tmpCandidateSubPlans.isEmpty() && orderedCandidateSubPlans.isEmpty() ) {
                    // Independent subplans exist, and there are no more candidate subplans to be consumed in orderedCandidateSubPlans
                    // In this case, choose the subplan with the lowest candidate from subPlansWithStatistics as candidate plan
                    tmpCandidateSubPlans = Arrays.asList( PhysicalPlanWithStatisticsUtils.chooseFirstSubPlan(subPlansWithStatistics) );
                }

                // Keep candidate SubPlans that use the same vocabulary mapping with the previous selected SubPlan.
                // If 'candidateSubPlans' is empty, use all subplans in 'tmpCandidateSubPlans' as candidate subplans.
                List<PhysicalPlanWithStatistics>  candidateSubPlans = PhysicalPlanWithStatisticsUtils.getSubPlansWithSameVoc( nextSelectedSubPlan.getVocabularyMappings(), tmpCandidateSubPlans);
                if( candidateSubPlans.isEmpty() ){
                    candidateSubPlans = tmpCandidateSubPlans;
                }

                // The candidateSubPlans are added to the ordered list 'orderedCandidateSubPlans'
                orderedCandidateSubPlans.addAll(candidateSubPlans);

                // select the first element (with the lowest cardinality) from orderedCandidateSubPlans as the nextSelectedSubPlan
                // Remove the selected subPlan from subPlansWithStatistics and clear orderedCandidateSubPlans for the next loop
                nextSelectedSubPlan = orderedCandidateSubPlans.poll();
                subPlansWithStatistics.remove(nextSelectedSubPlan);
                orderedCandidateSubPlans.clear();

                // Construct a physical plan: two physical algorithms can be selected by calling following functions.
//                currentJoinPlan = constructPlanWithSHJ( currentJoinPlan, nextSelectedSubPlan.plan );
                currentJoinPlan = constructPlanWithBJ( currentJoinPlan, nextSelectedSubPlan.plan );
            }

            return currentJoinPlan;
        }

        /**
         * @param resps A list of cardinality, which is ordered in the order in which the request operators can be found by a depth-first traversal of the subplans.
         * @return A list of PhysicalPlanWithStatistics, with each object corresponding to a subplan in the 'subplans' list.
         * Each PhysicalPlanWithStatistics object contains the physical plan for the subplan, along with its cardinality,
         * a list of relevant federations members, and estimated number of access to federation members.
         *
         * During a depth-first traversal of the subplans, increase the index by 1 when visiting a request operator:
         * If the root operator of the subplan is a Request operator, get the corresponding cardinality from resps with the same index;
         * If the subPlan with a Union of requests, calculate the total cardinality by summing up the cardinality of individual requests.
         * (See page 1052 in the paper[1])
         */
        protected List<PhysicalPlanWithStatistics> associateCardWithSubPlans(
        		final CardinalityResponse[] resps ){
            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = new ArrayList<>();

            int index = 0;
            for ( final PhysicalPlan subplan : subplans ) {
                final PhysicalOperator pop = subplan.getRootOperator();

                PhysicalPlanWithStatistics planWithStatistics = null;
                if ( pop instanceof PhysicalOpRequest
                        || (pop instanceof PhysicalOpFilter && subplan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest)) {
                    final int cardinality = resps[index].getCardinality();

                    planWithStatistics = new PhysicalPlanWithStatistics( subplan, null, new ArrayList<>(), cardinality, 0 );
                    index++;
                }
                else if ( pop instanceof PhysicalOpLocalToGlobal ){
                    if (  subplan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest
                     || (subplan.getSubPlan(0).getRootOperator() instanceof PhysicalOpFilter && subplan.getSubPlan(0).getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest)) {
                        final int cardinality = resps[index].getCardinality();
                        final VocabularyMapping vm = ((LogicalOpLocalToGlobal) ((PhysicalOpLocalToGlobal) pop).getLogicalOperator()).getVocabularyMapping();

                        planWithStatistics = new PhysicalPlanWithStatistics(subplan, null, Arrays.asList(vm), cardinality, 0 );
                        index++;
                    }
                }
                else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                    int aggregatedCardinality = 0;
                    final List<VocabularyMapping> vms = new ArrayList<>();
                    for ( int count = 0; count < subplan.numberOfSubPlans(); count++ ) {
                        final int cardinality = resps[index].getCardinality();
                        aggregatedCardinality += (cardinality < 0 ) ? Integer.MAX_VALUE : cardinality;
                        if ( aggregatedCardinality < 0 ) aggregatedCardinality = Integer.MAX_VALUE;

                        if ( subplan.getSubPlan(count).getRootOperator() instanceof PhysicalOpLocalToGlobal){
                            final VocabularyMapping vm = ((LogicalOpLocalToGlobal) ((PhysicalOpLocalToGlobal) subplan.getSubPlan(count).getRootOperator()).getLogicalOperator()).getVocabularyMapping();
                            vms.add(vm);
                        }

                        index++;
                    }

                    planWithStatistics = new PhysicalPlanWithStatistics(subplan, null, vms, aggregatedCardinality, 0 );
                }
                else
                    throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");

                subPlansWithStatistics.add( planWithStatistics );
            }

            return subPlansWithStatistics;
        }

        /**
         * Construct a physical plan using symmetric hash join:
         * If both currentPlan and nextPlan have PhysicalOpLocalToGlobal as root operator, and use the same vocabulary mapping,
         * the physical plan can be simplified as:
         * join( l2g^{vm}(p1), l2g^{vm}(p2) ) --> l2g^{vm}( join (p1, p2) )
         */
        protected PhysicalPlan constructPlanWithSHJ( final PhysicalPlan currentPlan, final PhysicalPlan nextPlan ) {
            final PhysicalOperator currentPop = currentPlan.getRootOperator();
            final PhysicalOperator nextPop = nextPlan.getRootOperator();
            if ( currentPop instanceof PhysicalOpLocalToGlobal
                    && nextPop instanceof PhysicalOpLocalToGlobal ) {
                final VocabularyMapping vm1 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) currentPop).getLogicalOperator()).getVocabularyMapping();
                final VocabularyMapping vm2 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) nextPop).getLogicalOperator()).getVocabularyMapping();
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

        /**
         * Construct a physical plan using bind join when it is possible:
         * If both currentPlan and nextPlan have PhysicalOpLocalToGlobal as root operator, and use the same vocabulary mapping,
         * the physical plan can be simplified as when unary operator can be applied:
         * tpAdd^{tp}_{fm}(g2l^{vm}(l2g^{vm}(p))) --> tpAdd^{tp}_{fm}(p)
         */
        protected PhysicalPlan constructPlanWithBJ( final PhysicalPlan currentPlan, final PhysicalPlan nextPlan ) {
            final PhysicalOperator currentPop = currentPlan.getRootOperator();
            final PhysicalOperator nextPop = nextPlan.getRootOperator();
            if ( currentPop instanceof PhysicalOpLocalToGlobal
                    && nextPop instanceof PhysicalOpLocalToGlobal ) {
                final VocabularyMapping vm1 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) currentPop).getLogicalOperator()).getVocabularyMapping();
                final VocabularyMapping vm2 = ((LogicalOpLocalToGlobal)((PhysicalOpLocalToGlobal) nextPop).getLogicalOperator()).getVocabularyMapping();
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
    }

}
