package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class JoinAnalyzerTest extends EngineTestBase {

    @Test
    public void countNumOfJoins_oneJoinVar() {
        // set up
        final Node v1 = Var.alloc("x");
        final Node v2 = Var.alloc("y");
        final Node v3 = Var.alloc("z");

        final QueryAnalyzer analyzer = new QueryAnalyzer(null);
        analyzer.setSubs( Arrays.asList( v1, v2, v3 ) );
        analyzer.setPreds( Arrays.asList( v1 ) );
        analyzer.setObjs( Arrays.asList( v1, v1, v1) );

        // check
        final int numJoinsWithSameSub = JoinAnalyzer.countNumOfJoinsWithSameSub(analyzer);
        assertEquals( 0, numJoinsWithSameSub );

        final int numJoinsWithSameObj = JoinAnalyzer.countNumOfJoinsWithSameObj(analyzer);
        assertEquals( 2, numJoinsWithSameObj );

        final int numOfChainJoins = JoinAnalyzer.countNumOfChainJoins(analyzer);
        assertEquals( 1, numOfChainJoins );

        final int numOfUnusualJoins = JoinAnalyzer.countNumOfUnusualJoins(analyzer);
        assertEquals( 2, numOfUnusualJoins );

    }

    @Test
    public void countNumOfJoins_twoJoinVars() {
        // set up
        final Node v1 = Var.alloc("x");
        final Node v2 = Var.alloc("y");
        final Node v3 = Var.alloc("z");

        final QueryAnalyzer analyzer = new QueryAnalyzer(null);
        analyzer.setSubs( Arrays.asList( v1, v2, v3 ) );
        analyzer.setPreds( Arrays.asList( v1, v2 ) );
        analyzer.setObjs( Arrays.asList( v1, v1, v1, v2, v2) );

        // check
        final int numJoinsWithSameSub = JoinAnalyzer.countNumOfJoinsWithSameSub(analyzer);
        assertEquals( 0, numJoinsWithSameSub );

        final int numJoinsWithSameObj = JoinAnalyzer.countNumOfJoinsWithSameObj(analyzer);
        assertEquals( 3, numJoinsWithSameObj );

        final int numOfChainJoins = JoinAnalyzer.countNumOfChainJoins(analyzer);
        assertEquals( 2, numOfChainJoins );

        final int numOfUnusualJoins = JoinAnalyzer.countNumOfUnusualJoins(analyzer);
        assertEquals( 4, numOfUnusualJoins );

    }

    @Test
    public void countDuplicatesTest() {
        final Node v1 = Var.alloc("x");
        final Node v2 = Var.alloc("y");
        final Node v3 = Var.alloc("z");

        final List<Node> vars_1 = Arrays.asList( v1, v1, v1, v2, v3 );
        final List<Node> vars_2 = Arrays.asList( v1, v1, v1, v2, v2 );

        int numJoins;
        // check
        numJoins = JoinAnalyzer.countDuplicates(vars_1);
        assertEquals( 2, numJoins );

        numJoins = JoinAnalyzer.countDuplicates(vars_2);
        assertEquals( 3, numJoins );
    }

    @Test
    public void countNumOfJoinPairsTest() {
        final Node v1 = Var.alloc("x");
        final Node v2 = Var.alloc("y");
        final Node v3 = Var.alloc("z");

        final List<Node> vars_l = Arrays.asList( v1, v1, v1, v2, v3 );

        final List<Node> vars_r1 = Arrays.asList( v1 );
        final List<Node> vars_r2 = Arrays.asList( v1, v1 );
        final List<Node> vars_r3 = Arrays.asList( v1, v1, v1, v1 );
        final List<Node> vars_r4 = Arrays.asList( v1, v2, v2 );

        int numJoins;

        // check
        numJoins = JoinAnalyzer.countNumOfJoinPairs(vars_l, vars_r1);
        assertEquals( 1, numJoins );

        numJoins = JoinAnalyzer.countNumOfJoinPairs(vars_l, vars_r2);
        assertEquals( 2, numJoins );

        numJoins = JoinAnalyzer.countNumOfJoinPairs(vars_l, vars_r3);
        assertEquals( 3, numJoins );

        numJoins = JoinAnalyzer.countNumOfJoinPairs(vars_l, vars_r4);
        assertEquals( 2, numJoins );
    }

}
