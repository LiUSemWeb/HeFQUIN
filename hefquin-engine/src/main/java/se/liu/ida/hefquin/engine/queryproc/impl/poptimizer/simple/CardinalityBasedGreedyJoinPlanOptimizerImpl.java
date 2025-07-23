package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpBindJoinWithRequestOps;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel.CFRNumberOfRequests;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.*;

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

    public CardinalityBasedGreedyJoinPlanOptimizerImpl( final QueryProcContext ctx ) {
        fedAccessMgr = ctx.getFederationAccessMgr();
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
            	reqOpsOfAllSubPlans.addAll( extractAllRequestOpsFromSourceAssignment(subplan) );
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
            PhysicalPlanWithStatistics nextSelectedSubPlan = chooseFirstSubPlan(subPlansWithStatistics);
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
                    List<PhysicalPlanWithStatistics> candidateSubPlans = getSubPlansContainVars(nextSelectedSubPlan.plan.getExpectedVariables(), subPlansWithStatistics);
                    if ( candidateSubPlans.isEmpty() && orderedCandidateSubPlans.isEmpty() ) {
                        // Independent subplans exist, and there are no more candidate subplans to be consumed in orderedCandidateSubPlans
                        // In this case, choose the subplan with the lowest candidate from subPlansWithStatistics as candidate plan
                        candidateSubPlans = Arrays.asList( chooseFirstSubPlan(subPlansWithStatistics) );
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
         * Extracts all request operators from the given plan, assuming
         * that this plan is a sub-plan of a source assignment (hence,
         * assuming that this plan can only be either a single request,
         * a filter over a request, or a union with requests).
         * 
         * The extracted request operators will be order in the order in
         * which they are discovered by a depth-first traversal of the
         * given plan.   
         */
        protected List<LogicalOpRequest<?,?>> extractAllRequestOpsFromSourceAssignment( final PhysicalPlan plan ) {
            final PhysicalOperator pop = plan.getRootOperator();

            if (pop instanceof PhysicalOpRequest) {
                return Arrays.asList( ((PhysicalOpRequest<?,?>) pop).getLogicalOperator() );
            }
            else if (pop instanceof PhysicalOpFilter
                    && plan.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest) {
                return Arrays.asList( ((PhysicalOpRequest<?,?>) plan.getSubPlan(0)).getLogicalOperator() );
            }
            else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                final List<LogicalOpRequest<?,?>> reqOps = new ArrayList<>();
                final int numOfSubPlans = plan.numberOfSubPlans();
                for (int i = 0; i < numOfSubPlans; i++) {
                    reqOps.addAll( extractAllRequestOpsFromSourceAssignment(plan.getSubPlan(i)) );
                }
                return reqOps;
            }
            else
                throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");
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
                if ( pop instanceof PhysicalOpRequest ||
                        (pop instanceof PhysicalOpFilter && subplan.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest)) {
                    final int cardinality = computeEffectiveCardinality( resps[index] );
                    final FederationMember fm = reqOpsOfAllSubPlans.get(index).getFederationMember();
                    final int numOfAccess = accessNumForReq(cardinality, fm);

                    planWithStatistics = new PhysicalPlanWithStatistics( subplan, Arrays.asList(fm), cardinality, numOfAccess );
                    index++;
                }
                else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                    int aggregatedCardinality = 0;
                    int numOfAccess = 0;
                    final List<FederationMember> fms = new ArrayList<>();
                    for ( int count = 0; count < subplan.numberOfSubPlans(); count++ ) {
                        final int cardinality = computeEffectiveCardinality( resps[index] );
                        aggregatedCardinality += (cardinality == Integer.MAX_VALUE) ? Integer.MAX_VALUE : cardinality;
                        if ( aggregatedCardinality < 0 ) aggregatedCardinality = Integer.MAX_VALUE;

                        final FederationMember fm = reqOpsOfAllSubPlans.get(index).getFederationMember();
                        numOfAccess += accessNumForReq(cardinality, fm);
                        fms.add( fm );

                        index++;
                    }

                    planWithStatistics = new PhysicalPlanWithStatistics(subplan, fms, aggregatedCardinality, numOfAccess );
                }
                else
                    throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");

                subPlansWithStatistics.add( planWithStatistics );
            }

            return subPlansWithStatistics;
        }

        /**
         * Compares all available subplans (see {@link #subplans}) in terms of
         * their respective cardinality returns the one with the lowest cardinality.
         */
        protected PhysicalPlanWithStatistics chooseFirstSubPlan( final List<PhysicalPlanWithStatistics> subPlansWithStatistics ) {
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
        protected List<PhysicalPlanWithStatistics> getSubPlansContainVars( final ExpectedVariables vars, final List<PhysicalPlanWithStatistics> subPlansWithStatistics ) {
            final List<PhysicalPlanWithStatistics> subPlansContainsVars = new ArrayList<>();
            for ( final PhysicalPlanWithStatistics subplan: subPlansWithStatistics ) {
                if ( ! ExpectedVariablesUtils.intersectionOfAllVariables( vars, subplan.plan.getExpectedVariables() ).isEmpty() ){
                    subPlansContainsVars.add(subplan);
                }
            }

            return subPlansContainsVars;
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
            final QueryPlanningInfo qpInfoForNewPlan = null;
            if ( accNumSHJ <= accNumBJ ) {
                newPlan = PhysicalPlanFactory.createPlanWithJoin(currentPlan.plan, nextPlan.plan, qpInfoForNewPlan);
            }
            else {
                newPlan = PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan.plan, nextPlan.plan, qpInfoForNewPlan);
            }

            // "We estimate the join cardinality of two subexpressions SEi and SEj as the minimum of their cardinalities."(see page 1052 in paper[1])
            // [1] Heling, Lars, and Maribel Acosta. "Federated SPARQL query processing over heterogeneous linked data fragments." Proceedings of the ACM Web Conference 2022.
            final int joinCardinality = Math.min( currentPlan.getCardinality(), nextPlan.getCardinality() );

            // The list of fed.members and numOfAccess will not be accessed anymore for this new PhysicalPlanWithStatistics object
            return new PhysicalPlanWithStatistics( newPlan, null, joinCardinality, -1 );
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
                return BaseForExecOpBindJoinWithRequestOps.DEFAULT_BATCH_SIZE;
            }
            else if ( fm instanceof TPFServer ){
                return 1;
            }
            else
                throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
        }
    }

    class PhysicalPlanWithStatistics {
        public final PhysicalPlan plan;
        protected final int cardinality;
        protected final int numOfAccess;
        protected final List<FederationMember> fms;

        public PhysicalPlanWithStatistics( final PhysicalPlan plan, final List<FederationMember> fms, final int cardinality, final int numOfAccess ){
            this.plan = plan;
            this.fms = fms;
            this.cardinality = cardinality;
            this.numOfAccess = numOfAccess;
        }

        public int getCardinality( ){
            return cardinality;
        }

        public int getNumOfAccess( ){
            return numOfAccess;
        }

        public List<FederationMember> getFederationMembers( ){
            return fms;
        }
    }

	/**
	 * TODO: Fallback behavior? Returning Integer.MAX_VALUE for now
	 *
	 * Computes the cardinality from the given {@link CardinalityResponse}.
	 *
	 * If retrieving the cardinality fails due to an {@link UnsupportedOperationDueToRetrievalError}, this method
	 * returns {@link Integer#MAX_VALUE} as a fallback.
	 *
	 * @param resp the cardinality response to extract the cardinality from
	 * @return the cardinality, or {@code Integer.MAX_VALUE} if retrieval is unsupported
	 */
	private int computeEffectiveCardinality( final CardinalityResponse resp ) {
		try {
			return resp.getCardinality();
		} catch ( UnsupportedOperationDueToRetrievalError e ) {
			return Integer.MAX_VALUE;
		}
	}
}
