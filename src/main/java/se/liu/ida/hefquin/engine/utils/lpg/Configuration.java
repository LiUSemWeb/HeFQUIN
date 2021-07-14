package se.liu.ida.hefquin.engine.utils.lpg;

import org.apache.jena.graph.Node;

/**
 * A class that contains the functions of an LPG2RDFstar configurations
 */
public interface Configuration {

    String mapNode( final String node );
    String mapProperty( final String property );
    String mapRelationship( final String relationship );
    String mapLabel( final String label );
    
    String unmapNode( final Node n);
    String unmapProperty ( final Node n);
    String unmapRelationship ( final Node n );
    String unmapLabel( final Node n);

    boolean mapsToNode( final Node n);
    boolean mapsToProperty( final Node n );
    boolean mapsToRelationship( final Node n);
    boolean mapsToLabel( final Node n);
    boolean isLabelIRI( final Node n);

    String getLabelIRI();
}
