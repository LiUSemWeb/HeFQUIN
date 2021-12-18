package se.liu.ida.hefquin.engine;

public interface HeFQUINMetrics {

    void putQueryPlanningTime(final String time);
    void putPlanCompileTime( final String time );
    void putPlanExecutingTime( final String time );
    void putCost( final double cost );
//    void putQueryExecutionTime( final String time );


    String getQueryPlanningTime();
    String getPlanCompileTime();
    String getPlanExecutionTime();
    double getCost();
//    String getQueryExecutionTime();

//    String printSelectedQueryPlan( final PhysicalPlan plan );

}
