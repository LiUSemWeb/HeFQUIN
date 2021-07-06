package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.dynamicProgramming;

import se.liu.ida.hefquin.engine.federation.FederationAccessException;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.QueryOptimizationContext;

import java.util.ArrayList;
import java.util.List;

public class WrappingDPAlgorithm {
    protected final QueryOptimizationContext ctxt;

    public WrappingDPAlgorithm( final QueryOptimizationContext ctxt ) {
        assert ctxt != null;
        this.ctxt = ctxt;
    }

    public PhysicalPlan optimize( final LogicalPlan initialPlan ) throws FederationAccessException {
        final boolean keepMultiwayJoins = true;
        final PhysicalPlan initialPhysicalPlan = ctxt.getLogicalToPhysicalPlanConverter().convert(initialPlan, keepMultiwayJoins);

        return optimizePhysicalPlan(initialPhysicalPlan);
    }

    public PhysicalPlan optimizePhysicalPlan( final PhysicalPlan pp) throws FederationAccessException {
        final List<PhysicalPlan> children = findChildren(pp);
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOperator lop = pop.getLogicalOperator();
        if ( lop instanceof NaryLogicalOp ) {
            return rewrite( children );
        }
        else {
            return pp;
        }
    }

    public List<PhysicalPlan> findChildren( final PhysicalPlan pp ) throws FederationAccessException {
        final List<PhysicalPlan> children = new ArrayList<PhysicalPlan>();
        final int numChildren = pp.numberOfSubPlans();
        if ( numChildren > 0 ) {
            for ( int i = 0; i < numChildren; ++i ) {
                children.add( optimizePhysicalPlan(pp.getSubPlan(i)) );
            }
        }
        return children;
    }

    public PhysicalPlan rewrite( final List<PhysicalPlan> children ) throws FederationAccessException {
        if ( children.size() < 1 )
            throw new IllegalArgumentException( "unexpected number of sub-plans: " + children.size() );
        else if (children.size() == 1 ){
            return children.get(0);
        }

        DynamicProgramming dp= new DynamicProgramming(ctxt, children);
        return dp.optimizePhysicalPlanForMultiwayJoin();
    }
}
