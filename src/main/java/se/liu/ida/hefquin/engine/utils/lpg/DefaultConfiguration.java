package se.liu.ida.hefquin.engine.utils.lpg;

import org.apache.jena.graph.Node;

public class DefaultConfiguration implements Configuration {
    protected final String PROPERTY_MAPPING = "http://example.org/property/";
    protected final String LABEL_URI = "http://www.w3.org/2000/01/rdf-schema#label";
    protected final String CLASS_MAPPING = "http://example.org/class/";
    protected final String RELATIONSHIP_MAPPING = "http://example.org/relationship/";
    protected final String NODE_MAPPING = "http://example.org/node/";

    public String unmapProperty( final String uri ) {
        return uri.replaceAll(PROPERTY_MAPPING, "");
    }

    public String unmapRelationship( final String uri ) {
        return uri.replaceAll(RELATIONSHIP_MAPPING, "");
    }

    @Override
    public String unmapLabel( final String label ) {
        return label.replaceAll(CLASS_MAPPING, "");
    }

    @Override
    public boolean mapsToNode( final Node n) {
        return n.isURI() && n.getURI().startsWith(NODE_MAPPING);
    }

    @Override
    public boolean mapsToProperty( final Node n ) {
        return n.isURI() && n.getURI().startsWith(PROPERTY_MAPPING);
    }

    @Override
    public boolean mapsToRelationship( final Node n) {
        return n.isURI() && n.getURI().startsWith(RELATIONSHIP_MAPPING);
    }

    @Override
    public boolean mapsToLabel( final Node n) {
        return n.isURI() && n.getURI().startsWith(CLASS_MAPPING);
    }

    @Override
    public boolean isLabelIRI( final Node n) {
        return n.isURI() && n.getURI().equals(LABEL_URI);
    }

    @Override
    public String getLabelIRI() {
        return LABEL_URI;
    }

    @Override
    public String mapNode( final String node ) {
        return NODE_MAPPING + node;
    }

    @Override
    public String mapProperty( final String property ) {
        return PROPERTY_MAPPING + property;
    }

    @Override
    public String mapRelationship( final String relationship ) {
        return RELATIONSHIP_MAPPING + relationship;
    }

    @Override
    public String mapLabel( final String label ) {
        return CLASS_MAPPING + label;
    }

    public String unmapNode( final String uri ) {
        return uri.replaceAll(NODE_MAPPING, "");
    }
}
