package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

public interface Metrics {

    Integer getNumberOfRequests( );

    Integer getShippedRDFTermsForRequests( ) ;

    Integer getShippedRDFVarsForRequests( );

    Integer getShippedRDFTermsForResponses( );

    Integer getShippedRDFVarsForResponses( );

    Integer getIntermediateResultsSize( );

    Boolean isEmpty();

}
