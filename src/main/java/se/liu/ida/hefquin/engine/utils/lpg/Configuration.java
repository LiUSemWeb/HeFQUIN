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
    String unmapNode( final String iri );
    String unmapProperty ( final String iri );
    String unmapRelationship ( final String iri );
    String unmapLabel( final String label );

    boolean mapsToNode( final Node n);
    boolean mapsToProperty( final Node n );
    boolean mapsToRelationship( final Node n);
    boolean mapsToLabel( final Node n);
    boolean isLabelIRI( final Node n);

    String getLabelIRI();
}
