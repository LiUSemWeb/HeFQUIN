package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting;

import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.BGPImpl;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules.RuleMergeTPAddOfBGPAddIntoBGPAdd;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;

import java.util.*;

import static org.junit.Assert.*;

public class RewritingRuleInstancesTest extends EngineTestBase {
    protected static boolean PRINT_TIME = false;

    @Test
    public void mergeTPAddOfBGPAddIntoBGPAdd() {
        final TriplePattern tp1 = new TriplePatternImpl( NodeFactory.createBlankNode(),
                NodeFactory.createBlankNode(),
                NodeFactory.createBlankNode() );
        final TriplePattern tp2 = new TriplePatternImpl( NodeFactory.createBlankNode(),
                NodeFactory.createBlankNode(),
                NodeFactory.createBlankNode() );
        final TriplePattern tp3 = new TriplePatternImpl( NodeFactory.createBlankNode(),
                NodeFactory.createBlankNode(),
                NodeFactory.createBlankNode() );

        final Set<TriplePattern> tps = new HashSet<>();
        tps.add(tp2);
        final FederationMember fm = new SPARQLEndpointForTest();
        final PhysicalPlan subplan = createBGPAddPlanWithBindJoinUNION( createRequestPlan(tp1), new BGPImpl(tps), fm );
        final PhysicalPlan plan = createTPAddPlan(subplan, tp3, fm);

        final long startTime = new Date().getTime();

        // rewrite the plan
        final RewritingRule rule = new RuleMergeTPAddOfBGPAddIntoBGPAdd(0.3);
        final Iterator<RuleApplication> ruleApplications = rule.determineAllPossibleApplications(plan).iterator();
        final PhysicalPlan newPlan = ruleApplications.next().getResultingPlan();

        final long endTime = new Date().getTime();
        if ( PRINT_TIME ) System.out.println( "MergeTPAddOfBGPAddIntoBGPAdd \t milliseconds passed: " + (endTime - startTime) );

        assertTrue( newPlan.getRootOperator() instanceof PhysicalOpBindJoinWithUNION );
        assertFalse( newPlan.getRootOperator() instanceof PhysicalOpBindJoinWithFILTER );

        final BGP bgp = ((LogicalOpBGPAdd) ((PhysicalOpBindJoinWithUNION) newPlan.getRootOperator()).getLogicalOperator()).getBGP();
        assertEquals( bgp.getTriplePatterns().size(), 2 );

        tps.add(tp3);
        // final PhysicalPlan expectPlan = createBGPAddPlanWithBindJoinUNION( subsubplan, new BGPImpl(tps), fm );
        assertEquals( bgp.getTriplePatterns(), tps );
    }

    protected PhysicalPlan createRequestPlan( final TriplePattern tp ) {
        final FederationMember fm = new TPFServerForTest();
        final TriplePatternRequest req = new TriplePatternRequestImpl(tp);

        final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(fm, req);

        return PhysicalPlanFactory.createPlan(reqOp);
    }

    protected PhysicalPlan createTPAddPlan( final PhysicalPlan subplan,
                                            final TriplePattern tp,
                                            final FederationMember fm ) {
        final LogicalOpTPAdd tpAdd = new LogicalOpTPAdd(fm, tp);
        return PhysicalPlanFactory.createPlan(tpAdd, subplan);
    }

    protected PhysicalPlan createBGPAddPlanWithBindJoinUNION( final PhysicalPlan subplan,
                                             final BGP bgp ,
                                             final FederationMember fm) {
        final LogicalOpBGPAdd bgpAdd = new LogicalOpBGPAdd(fm, bgp);
        return PhysicalPlanFactory.createPlanWithBindJoinUNION(bgpAdd, subplan);
    }

}
