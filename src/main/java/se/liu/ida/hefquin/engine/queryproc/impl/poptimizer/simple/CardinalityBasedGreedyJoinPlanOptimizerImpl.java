package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpBindJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel.CFRNumberOfRequests;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.CardinalityBasedJoinPlanOptimizerUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithStatistics;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithStatisticsUtils;

import java.util.*;

/**
 * This class implements a query optimizer[1] that builds left-deep query plans,
 * for which it uses a greedy approach to determine the join order based on cardinality estimation,
 * and then choose physical algorithm according to the estimated number of request to execute the join.
 *
 * [1] Heling, Lars, and Maribel Acosta. "Federated SPARQL query processing over heterogeneous linked data fragments." Proceedings of the ACM Web Conference 2022.
 */
public class CardinalityBasedGreedyJoinPlanOptimizerImpl extends JoinPlanOptimizerBase
{
    protected final FederationAccessManager fedAccessMgr;

    public CardinalityBasedGreedyJoinPlanOptimizerImpl(final FederationAccessManager fedAccessMgr ) {
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
            	reqOpsOfAllSubPlans.addAll( CardinalityBasedJoinPlanOptimizerUtils.extractAllRequestOpsFromSourceAssignment(subplan) );
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
            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = associateCardWithSubPlans(reqOpsOfAllSubPlans, resps);

            // To build a left-deep query plan, first select the subplan with the lowest estimated cardinality
            PhysicalPlanWithStatistics nextSelectedSubPlan = PhysicalPlanWithStatisticsUtils.chooseFirstSubPlan(subPlansWithStatistics);
            subPlansWithStatistics.remove(nextSelectedSubPlan);

            PhysicalPlanWithStatistics currentJoinPlan = nextSelectedSubPlan;

            // The nextSubPlan is selected from subplans that have join variables with selected subplans.
            // To achieve this, we implement a custom comparator and create a PriorityQueue to store candidate subPlans in cardinality order.
            final Comparator<PhysicalPlanWithStatistics> orderBasedOnCard = new Comparator<PhysicalPlanWithStatistics>() {
                @Override
                public int compare( PhysicalPlanWithStatistics o1, PhysicalPlanWithStatistics o2 ) {
                    return Integer.compare( o1.getCardinality(), o2.getCardinality() );
                }
            };
            final PriorityQueue<PhysicalPlanWithStatistics> orderedCandidateSubPlans = new PriorityQueue<>(orderBasedOnCard);

            while ( !orderedCandidateSubPlans.isEmpty() || !subPlansWithStatistics.isEmpty() ){

                if ( !subPlansWithStatistics.isEmpty() ) {
                    // Identify subplans that have join variables with the selected subplan as new candidateSubPlans.
                    List<PhysicalPlanWithStatistics> candidateSubPlans = PhysicalPlanWithStatisticsUtils.getSubPlansContainVars( nextSelectedSubPlan.plan.getExpectedVariables(), subPlansWithStatistics );
                    if ( candidateSubPlans.isEmpty() && orderedCandidateSubPlans.isEmpty() ) {
                        // Independent subplans exist, and there are no more candidate subplans to be consumed in orderedCandidateSubPlans
                        // In this case, choose the subplan with the lowest candidate from subPlansWithStatistics as candidate plan
                        candidateSubPlans = Arrays.asList( PhysicalPlanWithStatisticsUtils.chooseFirstSubPlan(subPlansWithStatistics) );
                    }

                    // The candidateSubPlans are added to the ordered list 'orderedCandidateSubPlans' and removed from the remaining subPlansWithStatistics
                    orderedCandidateSubPlans.addAll(candidateSubPlans);
                    subPlansWithStatistics.removeAll(candidateSubPlans);
                }

                // select the first element (with the lowest cardinality) from orderedCandidateSubPlans as the nextSelectedSubPlan
                nextSelectedSubPlan = orderedCandidateSubPlans.poll();
                // Decide the physical algorithm depending on the estimated number of requests
                currentJoinPlan = decidePhysicalAlgorithm( currentJoinPlan, nextSelectedSubPlan );
            }

            return currentJoinPlan.plan;
        }

        /**
         *
         * @param reqOpsOfAllSubPlans A list of request operators, which is ordered in the order in which the request operators can be found by a depth-first traversal of the subplans.
         * @param resps A list of cardinality in the same order of 'reqOpsOfAllSubPlans'
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
        		final List<LogicalOpRequest<?,?>> reqOpsOfAllSubPlans,
        		final CardinalityResponse[] resps ){
            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = new ArrayList<>();

            int index = 0;
            for ( final PhysicalPlan subplan : subplans ) {
                final PhysicalOperator pop = subplan.getRootOperator();

                final PhysicalPlanWithStatistics planWithStatistics;
                if ( pop instanceof PhysicalOpRequest
                        || (pop instanceof PhysicalOpFilter && subplan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest)
                        || (pop instanceof PhysicalOpLocalToGlobal && subplan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest)
                        || (pop instanceof PhysicalOpLocalToGlobal && subplan.getSubPlan(0).getRootOperator() instanceof PhysicalOpFilter && subplan.getSubPlan(0).getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest)) {
                    final int cardinality = resps[index].getCardinality();
                    final FederationMember fm = reqOpsOfAllSubPlans.get(index).getFederationMember();
                    final int numOfAccess = accessNumForReq(cardinality, fm);

                    planWithStatistics = new PhysicalPlanWithStatistics( subplan, Arrays.asList(fm), null, cardinality, numOfAccess );
                    index++;
                }
                else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                    int aggregatedCardinality = 0;
                    int numOfAccess = 0;
                    final List<FederationMember> fms = new ArrayList<>();
                    for ( int count = 0; count < subplan.numberOfSubPlans(); count++ ) {
                        final int cardinality = resps[index].getCardinality();
                        aggregatedCardinality += (cardinality < 0 ) ? Integer.MAX_VALUE : cardinality;
                        if ( aggregatedCardinality < 0 ) aggregatedCardinality = Integer.MAX_VALUE;

                        final FederationMember fm = reqOpsOfAllSubPlans.get(index).getFederationMember();
                        numOfAccess += accessNumForReq(cardinality, fm);
                        fms.add( fm );

                        index++;
                    }

                    planWithStatistics = new PhysicalPlanWithStatistics(subplan, fms, null, aggregatedCardinality, numOfAccess );
                }
                else
                    throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");

                subPlansWithStatistics.add( planWithStatistics );
            }

            return subPlansWithStatistics;
        }

        /**
         * The physical algorithm (symmetric hash join and bind join) is determined based on the estimated number of requests to execute the join (see formulas on page 1052 of the paper[1])
         * - SHJ: Card(T1)/PageSize + Card(T2)/PageSize
         * - BJ: Card(T1)/PageSize + Card(T1)/blockSize
         * [1] Heling, Lars, and Maribel Acosta. "Federated SPARQL query processing over heterogeneous linked data fragments." Proceedings of the ACM Web Conference 2022.
         *
         * For the comparison, only keeping the second element is enough.
         */
        protected PhysicalPlanWithStatistics decidePhysicalAlgorithm( final PhysicalPlanWithStatistics currentPlan, final PhysicalPlanWithStatistics nextPlan ) {
            final double accNumSHJ = nextPlan.getNumOfAccess();

            double accNumBJ = 0;
            for ( final FederationMember fm: nextPlan.getFederationMembers() ) {
                final double blockSize = determineBlockSize(fm);
                accNumBJ += currentPlan.getCardinality() / blockSize;
            }

            final PhysicalPlan newPlan;
            if ( accNumSHJ <= accNumBJ ) {
                newPlan = PhysicalPlanFactory.createPlanWithJoin(currentPlan.plan, nextPlan.plan);
            }
            else {
                newPlan = PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan.plan, nextPlan.plan);
            }

            // "We estimate the join cardinality of two subexpressions SEi and SEj as the minimum of their cardinalities."(see page 1052 in paper[1])
            // [1] Heling, Lars, and Maribel Acosta. "Federated SPARQL query processing over heterogeneous linked data fragments." Proceedings of the ACM Web Conference 2022.
            final int joinCardinality = Math.min( currentPlan.getCardinality(), nextPlan.getCardinality() );

            // The list of fed.members and numOfAccess will not be accessed anymore for this new PhysicalPlanWithStatistics object
            return new PhysicalPlanWithStatistics( newPlan, null, null, joinCardinality, -1 );
        }

        /**
         * The number of requests depends on the page size of response.
         */
        protected int accessNumForReq( final int cardinality, final FederationMember fm ) {
            if ( fm instanceof SPARQLEndpoint){
                return 1;
            }
            else if ( (fm instanceof TPFServer) || (fm instanceof BRTPFServer) ){
                final double pageSize = CFRNumberOfRequests.defaultPageSize;
                return (int) Math.ceil( cardinality/pageSize );
            }
            else
                throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
        }

        /**
         * The block size (number of bindings can be attached) depends on the type of interface
         */
        protected double determineBlockSize( final FederationMember fm ) {
            if ( (fm instanceof SPARQLEndpoint) || (fm instanceof BRTPFServer) ){
                return BaseForExecOpBindJoin.defaultPreferredInputBlockSize;
            }
            else if ( fm instanceof TPFServer ){
                return 1;
            }
            else
                throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
        }
    }

}
