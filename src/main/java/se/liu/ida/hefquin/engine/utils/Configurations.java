package se.liu.ida.hefquin.engine.utils;

public class Configurations {
    public static final String PROPERTY_MAPPING = "http://example.org/property/";
    public static final String LABEL_URI = "http://www.w3.org/2000/01/rdf-schema#label";
    public static final String CLASS_MAPPING = "http://example.org/class/";
    public static final String RELATIONSHIP_MAPPING = "http://example.org/relationship/";
    public static final String NODE_MAPPING = "http://example.org/node/";

    public static String unmapProperty( final String uri ) {
        return uri.replaceAll(PROPERTY_MAPPING, "");
    }

    public static String unmapClass( final String uri ) {
        return uri.replaceAll(CLASS_MAPPING, "");
    }

    public static String unmapRelationship( final String uri ) {
        return uri.replaceAll(RELATIONSHIP_MAPPING, "");
    }

    public static int unmapNode( final String uri ) {
        return Integer.parseInt(uri.replaceAll(NODE_MAPPING, ""));
    }
}
