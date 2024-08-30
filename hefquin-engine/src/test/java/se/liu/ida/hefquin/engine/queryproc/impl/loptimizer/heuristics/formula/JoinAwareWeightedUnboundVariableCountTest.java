package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class JoinAwareWeightedUnboundVariableCountTest {
    // set up
    final Node v1 = Var.alloc("x");
    final Node v2 = Var.alloc("y");
    final Node v3 = Var.alloc("z");
    final Node v4 = Var.alloc("m");

    final List<Node> subs = Arrays.asList( v1, v2, v3 );
    final List<Node> preds = Arrays.asList( v1, v1, v1 );
    final List<Node> objs = Arrays.asList( v1, v1, v2, v2 );

    @Test
    public void countUnboundVarsTest_emptyBoundVars() {
        final Set<Node> boundVariables = new HashSet<>();

        // check
        final int countUnboundVars_s = JoinAwareWeightedUnboundVariableCount.countUnboundVars(subs, boundVariables);
        assertEquals( 3, countUnboundVars_s );

        final int countUnboundVars_p = JoinAwareWeightedUnboundVariableCount.countUnboundVars(preds, boundVariables);
        assertEquals( 1, countUnboundVars_p );

        final int countUnboundVars_o = JoinAwareWeightedUnboundVariableCount.countUnboundVars(objs, boundVariables);
        assertEquals( 2, countUnboundVars_o );
    }

    @Test
    public void countUnboundVarsTest_oneBoundVars() {
        final Set<Node> boundVariables = new HashSet<>();
        boundVariables.add(v2);

        // check
        final int countUnboundVars_s = JoinAwareWeightedUnboundVariableCount.countUnboundVars(subs, boundVariables);
        assertEquals( 2, countUnboundVars_s );

        final int countUnboundVars_p = JoinAwareWeightedUnboundVariableCount.countUnboundVars(preds, boundVariables);
        assertEquals( 1, countUnboundVars_p );

        final int countUnboundVars_o = JoinAwareWeightedUnboundVariableCount.countUnboundVars(objs, boundVariables);
        assertEquals( 1, countUnboundVars_o );
    }

    @Test
    public void countUnboundVarsTest_twoBoundVars() {
        final Set<Node> boundVariables = new HashSet<>();
        boundVariables.add(v1);
        boundVariables.add(v4);

        // check
        final int countUnboundVars_s = JoinAwareWeightedUnboundVariableCount.countUnboundVars(subs, boundVariables);
        assertEquals( 2, countUnboundVars_s );

        final int countUnboundVars_p = JoinAwareWeightedUnboundVariableCount.countUnboundVars(preds, boundVariables);
        assertEquals( 0, countUnboundVars_p );

        final int countUnboundVars_o = JoinAwareWeightedUnboundVariableCount.countUnboundVars(objs, boundVariables);
        assertEquals( 1, countUnboundVars_o );
    }

}
