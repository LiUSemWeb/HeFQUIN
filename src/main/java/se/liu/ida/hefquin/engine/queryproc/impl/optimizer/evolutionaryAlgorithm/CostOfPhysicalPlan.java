package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

public interface CostOfPhysicalPlan {

    int getNumberOfRequests( );

    int getShippedRDFTermsForRequests( ) ;

    int getShippedVarsForRequests( );

    int getShippedRDFTermsForResponses( );

    int getShippedVarsForResponses( );

    int getIntermediateResultsSize( );

}
