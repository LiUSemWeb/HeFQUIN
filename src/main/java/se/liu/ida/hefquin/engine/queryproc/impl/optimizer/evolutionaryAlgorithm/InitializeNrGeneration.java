package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

public class InitializeNrGeneration {

    public static int countNumOfOp( final LogicalPlan plan ) {
        final LogicalOperator lop = plan.getRootOperator();

        if ( lop instanceof LogicalOpMultiwayJoin || lop instanceof LogicalOpMultiwayUnion ) {
            final int count = plan.numberOfSubPlans();

            int nrGeneration = ( lop instanceof LogicalOpMultiwayJoin ) ? calcFactorial(count) : 1;
            for ( int i = 0; i < count; i++ ) {
                nrGeneration = nrGeneration * countNumOfOp( plan.getSubPlan( i ) );
                if ( nrGeneration < 0 )
                    return Integer.MAX_VALUE;
            }
            return nrGeneration;
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
