package se.liu.ida.hefquin.engine;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;

public class HeFQUINMetricsImp implements HeFQUINMetrics {

    String queryPlanningTime;
    String planCompileTime;
    String planExecutionTime;

    double cost;
//    String queryExecutionTime;

    public void putQueryPlanningTime( final String time ) {
        this.queryPlanningTime = time;
    }

    public void putPlanCompileTime( final String time ) {
        this.planCompileTime = time;
    }

    public void putPlanExecutingTime( final String time ) {
        this.planExecutionTime = time;
    }

    public void putCost( final double cost ) {
        this.cost = cost;
    }

//    public void putQueryExecutionTime( final String time ) {
//        this.queryExecutionTime = time;
//    }

    @Override
    public String getQueryPlanningTime() {
        return this.queryPlanningTime;
    }

    @Override
    public String getPlanCompileTime() {
        return this.planCompileTime;
    }

    @Override
    public String getPlanExecutionTime() {
        return this.planExecutionTime;
    }

    @Override
    public double getCost() {
        return this.cost;
    }

//    @Override
//    public String getQueryExecutionTime() {
//        return this.queryExecutionTime;
//    }

//    @Override
//    public String printSelectedQueryPlan(final PhysicalPlan plan ) {
//        return PhysicalPlanPrinter.print(plan);
//    }

}
