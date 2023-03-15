package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.federation.access.utils.RequestMemberPair;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpBindJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel.CFRNumberOfRequests;

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
            // Create a list of RequestMemberPair objects for all request operators inside the given list of subplans,
            // where the list of these objects is ordered in the order in which
            // the request operators can be found by a depth-first traversal of the subplans.
            final List<RequestMemberPair> flattenRequestMemPairs = new ArrayList<>();
            for ( final PhysicalPlan plan : subplans ) {
                flattenRequestMemPairs.addAll( createRequestMemPairsFromSourceAssignment(plan) );
            }

            // Next, get a list of cardinalities by performing cardinality requests with above list of RequestMemberPair objects.
            final CardinalityResponse[] resps;
            try {
                resps = FederationAccessUtils.performCardinalityRequests(fedAccessMgr, flattenRequestMemPairs.toArray(new RequestMemberPair[0]));
            } catch (final FederationAccessException e) {
                throw new PhysicalOptimizationException("Issuing a cardinality request caused an exception.", e);
            }

            // Then, annotate each subQuery with its cardinality
            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = associateCardWithSubPlans( flattenRequestMemPairs, resps);

            // To build a left-deep query plan, first sort subPlans based on their cardinality estimation (starting with the lowest cardinality)
            Collections.sort(subPlansWithStatistics, new Comparator<PhysicalPlanWithStatistics>() {
                @Override
                public int compare( PhysicalPlanWithStatistics o1, PhysicalPlanWithStatistics o2 ) {
                    return Integer.compare( o1.getCardinality(), o2.getCardinality() );
                }
            });

            final Iterator<PhysicalPlanWithStatistics> it = subPlansWithStatistics.iterator();
            PhysicalPlanWithStatistics currentPlan = it.next();
            // Decide the physical algorithm depending on the estimated number of requests
            while ( it.hasNext() ){
                final PhysicalPlanWithStatistics nextPlan = it.next();
                currentPlan = decidePhysicalAlgorithm( currentPlan, nextPlan );
            }

            return currentPlan.plan;
        }

        /**
         * Create a list of RequestMemberPair objects for a subplan of the source assignment.
         * This subplan can be in form of single request, filter with request, or Union with requests.
         */
        protected List<RequestMemberPair> createRequestMemPairsFromSourceAssignment( final PhysicalPlan plan ) {
            final PhysicalOperator pop = plan.getRootOperator();

            if (pop instanceof PhysicalOpRequest) {
                return Arrays.asList(createRequestMemPairFromRequest((PhysicalOpRequest) pop));
            }
            else if (pop instanceof PhysicalOpFilter
                    && plan.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest) {
                return Arrays.asList( createRequestMemPairFromRequest((PhysicalOpRequest) plan.getSubPlan(0)));
            }
            else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                final List<RequestMemberPair> requestMemPairs = new ArrayList<>();
                final int numOfSubPlans = plan.numberOfSubPlans();
                for (int i = 0; i < numOfSubPlans; i++) {
                    requestMemPairs.addAll( createRequestMemPairsFromSourceAssignment(plan.getSubPlan(i)) );
                }
                return requestMemPairs;
            }
            else
                throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");
        }

        protected RequestMemberPair createRequestMemPairFromRequest( final PhysicalOpRequest pop ) {
            final FederationMember fm = pop.getLogicalOperator().getFederationMember();
            DataRetrievalRequest req = pop.getLogicalOperator().getRequest();
            if ( fm instanceof TPFServer ) {
                req = ensureTPFRequest( (TriplePatternRequest) req );
            }
            else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
                req = ensureTPFRequest( (TriplePatternRequest) req );
            }
            else if ( fm instanceof BRTPFServer && req instanceof BindingsRestrictedTriplePatternRequest ) {
                req = ensureBRTPFRequest( (BindingsRestrictedTriplePatternRequest) req );
            }

            return new RequestMemberPair(req, fm);
        }

        protected TPFRequest ensureTPFRequest( final TriplePatternRequest req ) {
            if ( req instanceof TPFRequest ) {
                return (TPFRequest) req;
            }
            else {
                return new TPFRequestImpl( req.getQueryPattern() );
            }
        }

        protected BRTPFRequest ensureBRTPFRequest( final BindingsRestrictedTriplePatternRequest req ) {
            if ( req instanceof BRTPFRequest ) {
                return (BRTPFRequest) req;
            }
            else {
                return new BRTPFRequestImpl( req.getTriplePattern(), req.getSolutionMappings() );
            }
        }

        /**
         *
         * @param flattenRequestMemPairs A list of RequestMemberPair objects, which is ordered in the order in which the request operators can be found by a depth-first traversal of the subplans.
         * @param resps A list of cardinality in the same order of 'flattenRequestMemPairs'
         * @return A list of PhysicalPlanWithStatistics, which contains a physical plan with associated cardinality, a list of relevant federations members, and estimated number of access to fm
         *
         * Depth-first traversal of the subplans, increase the index by 1 when visiting a request operator:
         * for the subPlan with Request as root operator, get the corresponding cardinality from resps with the same index;
         * for the subPlan with a Union of requests, calculate the total cardinality by summing up the cardinality of individual requests (see page 1052 in the paper[1])
         */
        protected List<PhysicalPlanWithStatistics> associateCardWithSubPlans( final List<RequestMemberPair> flattenRequestMemPairs, final CardinalityResponse[] resps ){
            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = new ArrayList<>();

            int index = 0;
            for ( final PhysicalPlan plan : subplans ) {
                final PhysicalOperator pop = plan.getRootOperator();

                final PhysicalPlanWithStatistics planWithStatistics;
                if ( pop instanceof PhysicalOpRequest ||
                        (pop instanceof PhysicalOpFilter && plan.getSubPlan(0).getRootOperator() instanceof LogicalOpRequest)) {
                    final int cardinality = resps[index].getCardinality();
                    final FederationMember fm = flattenRequestMemPairs.get(index).getMember();
                    final int numOfAccess = accessNumForReq( resps[index].getCardinality(), fm );

                    planWithStatistics = new PhysicalPlanWithStatistics( plan, Arrays.asList(fm), cardinality, numOfAccess );
                    index++;
                }
                else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                    int cardinality = 0, numOfAccess = 0;
                    final List<FederationMember> fms = new ArrayList<>();
                    for ( int count = 0; count < plan.numberOfSubPlans(); count++ ) {
                        cardinality += resps[index].getCardinality();

                        final FederationMember fm = flattenRequestMemPairs.get(index).getMember();
                        numOfAccess += accessNumForReq( resps[index].getCardinality(), fm );
                        fms.add( fm );

                        index++;
                    }

                    planWithStatistics = new PhysicalPlanWithStatistics(plan, fms, cardinality, numOfAccess );
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
                return BaseForExecOpBindJoin.defaultPreferredInputBlockSize;
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

}
