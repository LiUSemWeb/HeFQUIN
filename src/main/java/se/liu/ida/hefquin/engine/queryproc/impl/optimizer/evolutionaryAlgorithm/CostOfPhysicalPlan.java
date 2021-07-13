package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

public interface CostOfPhysicalPlan {

    int getNumberOfRequests( );

    int getShippedRDFTermsForRequests( ) ;

    int getShippedRDFVarsForRequests( );

    int getShippedRDFTermsForResponses( );

    int getShippedRDFVarsForResponses( );

    int getIntermediateResultsSize( );

}
