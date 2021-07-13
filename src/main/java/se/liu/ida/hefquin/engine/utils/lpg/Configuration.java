package se.liu.ida.hefquin.engine.utils.lpg;

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

    boolean mapsToNode( final String iri );
    boolean mapsToProperty( final String iri );
    boolean mapsToRelationship( final String iri );
    boolean mapsToLabel( final String label );
    boolean isLabelIRI( final String iri );

    String getLabelIRI();
}
