package se.liu.ida.hefquin.federation.access;

public interface Neo4jRequest extends DataRetrievalRequest{

    String getCypherQuery();

}
