package se.liu.ida.hefquin.engine.federation.access;

public interface Neo4jRequest extends DataRetrievalRequest{

    String getCypherQuery();

}
