package se.liu.ida.hefquin.engine.federation.access;

public interface Neo4jInterface extends StringRetrievalInterface {
    /** Returns the URL of the HTTP endpoint of this interface. */
    String getURL();
}
