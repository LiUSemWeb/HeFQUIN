package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

public class InitializeNrGeneration {

    public static int countNumOfOp( final LogicalPlan plan ) {
        final LogicalOperator lop = plan.getRootOperator();

        if ( lop instanceof LogicalOpMultiwayJoin) {
            final int count = plan.numberOfSubPlans();

            int nrGeneration = calcFactorial(count);
            for ( int i = 0; i < count; i++ ) {
                nrGeneration = nrGeneration * countNumOfOp( plan.getSubPlan( i ) );
            }
            return nrGeneration > 0 ? nrGeneration: Integer.MAX_VALUE;
        }
        else if ( lop instanceof LogicalOpMultiwayUnion) {
            final int count = plan.numberOfSubPlans();

            int nrGeneration = 1;
            for ( int i = 0; i < count; i++ ) {
                nrGeneration = nrGeneration * countNumOfOp( plan.getSubPlan( i ) );
            }
            return nrGeneration > 0 ? nrGeneration: Integer.MAX_VALUE;
        }
        else if ( lop instanceof LogicalOpRequest) {
            return 1;
        }
        else {
            throw new IllegalArgumentException( lop.getClass().getName() );
        }
    }

    protected static int calcFactorial( final int n ) {
        if ( n <= 1 )
            return 1;
        else if ( n > 12 )
            return Integer.MAX_VALUE;
        else
            return n * calcFactorial(n - 1);
    }

}
