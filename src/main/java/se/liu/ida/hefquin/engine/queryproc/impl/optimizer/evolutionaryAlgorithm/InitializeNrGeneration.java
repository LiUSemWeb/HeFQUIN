package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

public class InitializeNrGeneration {

    public static int countNumOfOp( final LogicalPlan plan ) {
        final LogicalOperator lop = plan.getRootOperator();

        int nrGeneration = 1;
        if ( lop instanceof LogicalOpMultiwayJoin) {
            int count = plan.numberOfSubPlans();
            nrGeneration = calcFactorial(count);
            for ( int i = 0; i < count; i++ ) {
                nrGeneration = nrGeneration * countNumOfOp( plan.getSubPlan( i ) );
            }
        }
        else if ( lop instanceof LogicalOpMultiwayUnion) {
            int count = plan.numberOfSubPlans();
            for ( int i = 0; i < count; i++ ) {
                nrGeneration = nrGeneration * countNumOfOp( plan.getSubPlan( i ) );
            }
        }
        else if ( lop instanceof LogicalOpRequest) {
            nrGeneration = 1;
        }
        return nrGeneration;
    }

    protected static int calcFactorial( final int n ) {
        if ( n <= 1 )
            return 1;
        else
            return n * calcFactorial(n - 1);
    }

}
