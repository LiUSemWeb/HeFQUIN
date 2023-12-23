package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.CompositeEdgeLabelMappingImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.CompositeNodeLabelMappingImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.CompositePropertyNameMappingImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.EdgeLabelMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.EdgeLabelMappingImpl_AllToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.LPG2RDFConfigurationImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMappingImpl_AllToLiterals;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMappingImpl_AllToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeMappingImpl_AllToBNodes;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeMappingImpl_AllToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMappingImpl_AllToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.EdgeLabelMappingImpl_RegexMatchToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMappingImpl_RegexMatchToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMappingImpl_RegexMatchToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.EdgeLabelMappingImpl_SingleMatchToURI;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMappingImpl_SingleMatchToLiteral;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMappingImpl_SingleMatchToURI;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMappingImpl_SingleMatchToURI;
import se.liu.ida.hefquin.vocabulary.LPGtoRDF;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LPG2RDFConfigurationReader
{
    /**
     * Creates a {@link LPG2RDFConfiguration} from the RDF-based description
     * in the given file. Assumes that the file describes only one such
     * configuration.
     */
    public LPG2RDFConfiguration readFromFile( final String filename ) {
        final Model m = RDFDataMgr.loadModel(filename);
        return read(m);
    }

    /**
     * Creates the {@link LPG2RDFConfiguration} that is identified by the
     * given URI in the RDF-based description  in the given file.
     */
    public LPG2RDFConfiguration readFromFile( final String filename, final String uriOfConfRsrc ) {
        final Model m = RDFDataMgr.loadModel(filename);
        final Resource confRsrc = m.createResource(uriOfConfRsrc);

        if ( ! m.contains(confRsrc, null) )
            throw new IllegalArgumentException("There is no description of the given URI (" + uriOfConfRsrc + ") in " + filename);

        return read(confRsrc);
    }

    public LPG2RDFConfiguration read( final Model m ) {
        final ResIterator itConfigs = m.listResourcesWithProperty(RDF.type, LPGtoRDF.LPGtoRDFConfiguration);

        if ( ! itConfigs.hasNext() ) {
            throw new IllegalArgumentException("LPGtoRDFConfiguration is required!");
        }

        final Resource confRsrc = itConfigs.next();

        if ( itConfigs.hasNext() ) {
            throw new IllegalArgumentException("More than one instance of LPGtoRDFConfiguration!");
        }

        return read(confRsrc);
    }

    public LPG2RDFConfiguration read( final Resource confRsrc ) {
        final NodeMapping nm = readNodeMappingFromConfig(confRsrc);
        final NodeLabelMapping nlm = readNodeLabelMappingFromConfig(confRsrc);
        final EdgeLabelMapping elm = readEdgeLabelMappingFromConfig(confRsrc);
        final PropertyNameMapping pm = readPropertyNameMappingFromConfig(confRsrc);
        final Node labelPredicate = readLabelPredicateFromConfig(confRsrc);

        return new LPG2RDFConfigurationImpl(nm, nlm, elm, pm, labelPredicate);
    }

    public Node readLabelPredicateFromConfig( final Resource lpg2rdfConfig ) {
        final URI iri = getSingleMandatoryProperty_XSDURI( lpg2rdfConfig, LPGtoRDF.labelPredicate );
        return NodeFactory.createURI( iri.toString() );
    }


    // ------------ node mappings ------------

    public NodeMapping readNodeMappingFromConfig( final Resource lpg2rdfConfig ) {
        final Resource nm = getMappingResource( lpg2rdfConfig, LPGtoRDF.nodeMapping );

        final RDFNode nmType = lpg2rdfConfig.getModel().getRequiredProperty(nm, RDF.type).getObject();

        if ( nmType.equals(LPGtoRDF.IRIPrefixBasedNodeMapping) )
            return readIRIPrefixBasedNodeMapping(nm);

        if ( nm.hasProperty(LPGtoRDF.prefixOfIRIs) )
           	return readIRIPrefixBasedNodeMapping(nm);

        if ( nmType.equals(LPGtoRDF.BNodeBasedNodeMapping) )
            return new NodeMappingImpl_AllToBNodes();

        throw new IllegalArgumentException("NodeMapping type (" + nmType + ") is unexpected!");
    }

    public NodeMapping readIRIPrefixBasedNodeMapping( final Resource nm ) {
        final URI u = getSingleMandatoryProperty_XSDURI( nm, LPGtoRDF.prefixOfIRIs );
        return new NodeMappingImpl_AllToURIs( u.toString() );
    }


    // ------------ node label mappings ------------

    public NodeLabelMapping readNodeLabelMappingFromConfig( final Resource lpg2rdfConfig ) {
        final Resource nlm = getMappingResource( lpg2rdfConfig, LPGtoRDF.nodeLabelMapping );
        return readNodeLabelMapping(nlm);
    }

    public NodeLabelMapping readNodeLabelMapping( final Resource nlm ) {
        final RDFNode nlmType = nlm.getRequiredProperty(RDF.type).getObject();

        // try to detect the type based on an rdf:type statement

        if ( nlmType.equals(LPGtoRDF.RegexBasedNodeLabelMapping) )
            return readRegexBasedNodeLabelMapping(nlm);

        if ( nlmType.equals(LPGtoRDF.IRIPrefixBasedNodeLabelMapping) )
            return readIRIPrefixBasedNodeLabelMapping(nlm);

        if ( nlmType.equals(LPGtoRDF.SingletonIRINodeLabelMapping) )
            return readSingletonIRINodeLabelMapping(nlm);

        if ( nlmType.equals(LPGtoRDF.SingletonLiteralNodeLabelMapping) )
            return readSingletonLiteralNodeLabelMapping(nlm);

        if ( nlmType.equals(LPGtoRDF.CompositeNodeLabelMapping) )
            return readCompositeNodeLabelMapping(nlm);

        if ( nlmType.equals(LPGtoRDF.LiteralBasedNodeLabelMapping) )
            return new NodeLabelMappingImpl_AllToLiterals();

        if ( ! nlmType.equals(LPGtoRDF.NodeLabelMapping) )
            throw new IllegalArgumentException("NodeLabelMapping type (" + nlmType + ") is unexpected!");

        // try to detect the type based on the properties

        if ( nlm.hasProperty(LPGtoRDF.prefixOfIRIs) ) {
            if ( nlm.hasProperty(LPGtoRDF.regex) )
                return readRegexBasedNodeLabelMapping(nlm);
            else
                return readIRIPrefixBasedNodeLabelMapping(nlm);
        }

        if ( nlm.hasProperty(LPGtoRDF.label) ) {
            if ( nlm.hasProperty(LPGtoRDF.iri) )
                return readSingletonIRINodeLabelMapping(nlm);
            if ( nlm.hasProperty(LPGtoRDF.literal) )
                return readSingletonLiteralNodeLabelMapping(nlm);
        }

        if ( nlm.hasProperty(LPGtoRDF.componentMappings) )
            return readCompositeNodeLabelMapping(nlm);

        throw new IllegalArgumentException("Incomplete NodeLabelMapping (" + nlm + ")");
    }

    public NodeLabelMapping readRegexBasedNodeLabelMapping( final Resource nlm ) {
        final String regex = getSingleMandatoryProperty_XSDString( nlm, LPGtoRDF.regex );
        final URI prefixOfIRIs = getSingleMandatoryProperty_XSDURI( nlm, LPGtoRDF.prefixOfIRIs );

        return new NodeLabelMappingImpl_RegexMatchToURIs( regex, prefixOfIRIs.toString() );
    }

    public NodeLabelMapping readIRIPrefixBasedNodeLabelMapping( final Resource nlm ) {
        final URI prefixOfIRIs = getSingleMandatoryProperty_XSDURI( nlm, LPGtoRDF.prefixOfIRIs );

        return new NodeLabelMappingImpl_AllToURIs( prefixOfIRIs.toString() );
    }

    public NodeLabelMapping readSingletonIRINodeLabelMapping( final Resource nlm ) {
        final String label = getSingleMandatoryProperty_XSDString( nlm, LPGtoRDF.label );
        final URI iri = getSingleMandatoryProperty_XSDURI( nlm, LPGtoRDF.iri );

        return new NodeLabelMappingImpl_SingleMatchToURI( label, iri.toString() );
    }

    public NodeLabelMapping readSingletonLiteralNodeLabelMapping( final Resource nlm ) {
        final String label = getSingleMandatoryProperty_XSDString( nlm, LPGtoRDF.label );
        final String literal = getSingleMandatoryProperty_XSDString( nlm, LPGtoRDF.literal );

        return new NodeLabelMappingImpl_SingleMatchToLiteral( label, literal );
    }

    public NodeLabelMapping readCompositeNodeLabelMapping( final Resource nlm ) {
        final RDFNode list = getSingleMandatoryProperty( nlm, LPGtoRDF.componentMappings );

        if ( ! list.canAs(RDFList.class) )
            throw new IllegalArgumentException( LPGtoRDF.componentMappings.getLocalName() + " property of " + nlm.toString() + " should be a list" );

        final Iterator<RDFNode> it = list.as( RDFList.class ).iterator();

        if ( ! it.hasNext() )
            throw new IllegalArgumentException( LPGtoRDF.componentMappings.getLocalName() + " property of " + nlm.toString() + " should be a nonempty (!) list" );

        final List<NodeLabelMapping> components = new ArrayList<>();
        while ( it.hasNext() ) {
            final NodeLabelMapping c = readNodeLabelMapping( (Resource) it.next() );

            if ( c instanceof CompositeNodeLabelMappingImpl )
                throw new IllegalArgumentException("CompositeNodeLabelMapping should not contain another CompositeNodeLabelMapping");

            components.add(c);
        }

        return new CompositeNodeLabelMappingImpl(components);
    }


    // ------------ edge label mappings ------------

    public EdgeLabelMapping readEdgeLabelMappingFromConfig( final Resource lpg2rdfConfig ) {
        final Resource elm = getMappingResource( lpg2rdfConfig, LPGtoRDF.edgeLabelMapping );
        return readEdgeLabelMapping(elm);
    }

    public EdgeLabelMapping readEdgeLabelMapping( final Resource elm ) {
        final RDFNode elmType = elm.getRequiredProperty(RDF.type).getObject();

        // try to detect the type based on an rdf:type statement

        if ( elmType.equals(LPGtoRDF.RegexBasedEdgeLabelMapping) )
            return readRegexBasedEdgeLabelMapping(elm);

        if ( elmType.equals(LPGtoRDF.IRIPrefixBasedEdgeLabelMapping) )
            return readIRIPrefixBasedEdgeLabelMapping(elm);

        if ( elmType.equals(LPGtoRDF.SingletonIRIEdgeLabelMapping) )
            return readSingletonIRIEdgeLabelMapping(elm);

        if ( elmType.equals(LPGtoRDF.CompositeEdgeLabelMapping) )
            return readCompositeEdgeLabelMapping(elm);

        if ( ! elmType.equals(LPGtoRDF.EdgeLabelMapping) )
            throw new IllegalArgumentException("EdgeLabelMapping type (" + elmType + ") is unexpected!");

        // try to detect the type based on the properties

        if ( elm.hasProperty(LPGtoRDF.prefixOfIRIs) ) {
            if ( elm.hasProperty(LPGtoRDF.regex) )
                return readRegexBasedEdgeLabelMapping(elm);
            else
                return readIRIPrefixBasedEdgeLabelMapping(elm);
        }

        if ( elm.hasProperty(LPGtoRDF.label) || elm.hasProperty(LPGtoRDF.iri) )
            return readSingletonIRIEdgeLabelMapping(elm);

        if ( elm.hasProperty(LPGtoRDF.componentMappings) )
            return readCompositeEdgeLabelMapping(elm);

        throw new IllegalArgumentException("Incomplete EdgeLabelMapping (" + elm + ")");
    }

    public EdgeLabelMapping readRegexBasedEdgeLabelMapping( final Resource elm ) {
        final String regex = getSingleMandatoryProperty_XSDString( elm, LPGtoRDF.regex );
        final URI prefixOfIRIs = getSingleMandatoryProperty_XSDURI( elm, LPGtoRDF.prefixOfIRIs );

        return new EdgeLabelMappingImpl_RegexMatchToURIs( regex, prefixOfIRIs.toString() );
    }

    public EdgeLabelMapping readIRIPrefixBasedEdgeLabelMapping( final Resource elm ) {
        final URI prefixOfIRIs = getSingleMandatoryProperty_XSDURI( elm, LPGtoRDF.prefixOfIRIs );

        return new EdgeLabelMappingImpl_AllToURIs( prefixOfIRIs.toString() );
    }

    public EdgeLabelMapping readSingletonIRIEdgeLabelMapping( final Resource elm ) {
        final String label = getSingleMandatoryProperty_XSDString( elm, LPGtoRDF.label );
        final URI iri = getSingleMandatoryProperty_XSDURI( elm, LPGtoRDF.iri );

        return new EdgeLabelMappingImpl_SingleMatchToURI( label, iri.toString() );
    }

    public EdgeLabelMapping readCompositeEdgeLabelMapping( final Resource elm ) {
        final RDFNode list = getSingleMandatoryProperty( elm, LPGtoRDF.componentMappings );

        if ( ! list.canAs(RDFList.class) )
            throw new IllegalArgumentException( LPGtoRDF.componentMappings.getLocalName() + " property of " + elm.toString() + " should be a list" );

        final Iterator<RDFNode> it = list.as( RDFList.class ).iterator();

        if ( ! it.hasNext() )
            throw new IllegalArgumentException( LPGtoRDF.componentMappings.getLocalName() + " property of " + elm.toString() + " should be a nonempty (!) list" );

        final List<EdgeLabelMapping> components = new ArrayList<>();
        while ( it.hasNext() ) {
            final EdgeLabelMapping c = readEdgeLabelMapping( (Resource) it.next() );

            if ( c instanceof CompositeEdgeLabelMappingImpl )
                throw new IllegalArgumentException("CompositeEdgeLabelMapping should not contain another CompositeEdgeLabelMapping");

            components.add(c);
        }

        return new CompositeEdgeLabelMappingImpl(components);
    }


    // ------------ property name mappings ------------

    public PropertyNameMapping readPropertyNameMappingFromConfig( final Resource lpg2rdfConfig ) {
        final Resource pm = getMappingResource( lpg2rdfConfig, LPGtoRDF.propertyNameMapping );
        return readPropertyNameMapping(pm);
    }

    public PropertyNameMapping readPropertyNameMapping( final Resource pm ) {
        final RDFNode pmType = pm.getRequiredProperty(RDF.type).getObject();

        // try to detect the type based on an rdf:type statement

        if ( pmType.equals(LPGtoRDF.RegexBasedPropertyNameMapping) )
            return readRegexBasedPropertyNameMapping(pm);

        if ( pmType.equals(LPGtoRDF.IRIPrefixBasedPropertyNameMapping) )
            return readIRIPrefixBasedPropertyNameMapping(pm);

        if ( pmType.equals(LPGtoRDF.SingletonIRIPropertyNameMapping) )
            return readSingletonIRIPropertyNameMapping(pm);

        if ( pmType.equals(LPGtoRDF.CompositePropertyNameMapping) )
            return readCompositePropertyNameMapping(pm);

        if ( ! pmType.equals(LPGtoRDF.EdgeLabelMapping) )
            throw new IllegalArgumentException("PropertyNameMapping type (" + pmType + ") is unexpected!");

        // try to detect the type based on the properties

        if ( pm.hasProperty(LPGtoRDF.prefixOfIRIs) ) {
            if ( pm.hasProperty(LPGtoRDF.regex) )
                return readRegexBasedPropertyNameMapping(pm);
            else
                return readIRIPrefixBasedPropertyNameMapping(pm);
        }

        if ( pm.hasProperty(LPGtoRDF.label) || pm.hasProperty(LPGtoRDF.iri) )
            return readSingletonIRIPropertyNameMapping(pm);

        if ( pm.hasProperty(LPGtoRDF.componentMappings) )
            return readCompositePropertyNameMapping(pm);

        throw new IllegalArgumentException("Incomplete PropertyNameMapping (" + pm + ")");
    }

    public PropertyNameMapping readRegexBasedPropertyNameMapping( final Resource pm ) {
        final String regex = getSingleMandatoryProperty_XSDString( pm, LPGtoRDF.regex );
        final URI prefixOfIRIs = getSingleMandatoryProperty_XSDURI( pm, LPGtoRDF.prefixOfIRIs );

        return new PropertyNameMappingImpl_RegexMatchToURIs( regex, prefixOfIRIs.toString() );
    }

    public PropertyNameMapping readIRIPrefixBasedPropertyNameMapping( final Resource pm ) {
        final URI prefixOfIRIs = getSingleMandatoryProperty_XSDURI( pm, LPGtoRDF.prefixOfIRIs );

        return new PropertyNameMappingImpl_AllToURIs( prefixOfIRIs.toString() );
    }

    public PropertyNameMapping readSingletonIRIPropertyNameMapping( final Resource pm ) {
        final String label = getSingleMandatoryProperty_XSDString( pm, LPGtoRDF.propertyName );
        final URI iri = getSingleMandatoryProperty_XSDURI( pm, LPGtoRDF.iri );

        return new PropertyNameMappingImpl_SingleMatchToURI( label, iri.toString() );
    }

    public PropertyNameMapping readCompositePropertyNameMapping( final Resource pm ) {
        final RDFNode list = getSingleMandatoryProperty( pm, LPGtoRDF.componentMappings );

        if ( ! list.canAs(RDFList.class) )
            throw new IllegalArgumentException( LPGtoRDF.componentMappings.getLocalName() + " property of " + pm.toString() + " should be a list" );

        final Iterator<RDFNode> it = list.as( RDFList.class ).iterator();

        if ( ! it.hasNext() )
            throw new IllegalArgumentException( LPGtoRDF.componentMappings.getLocalName() + " property of " + pm.toString() + " should be a nonempty (!) list" );

        final List<PropertyNameMapping> components = new ArrayList<>();
        while ( it.hasNext() ) {
            final PropertyNameMapping c = readPropertyNameMapping( (Resource) it.next() );

            if ( c instanceof CompositePropertyNameMappingImpl )
                throw new IllegalArgumentException("CompositePropertyNameMapping should not contain another CompositePropertyNameMapping");

            components.add(c);
        }

        return new CompositePropertyNameMappingImpl(components);
    }


    // ------------- helper functions -------------

    protected RDFNode getSingleMandatoryProperty( final Resource r, final Property p ) {
        final StmtIterator it = r.listProperties(p);

        if( ! it.hasNext() ) {
            throw new IllegalArgumentException( p.getLocalName() + " property missing for " + r.toString() );
        }

        final RDFNode o = it.next().getObject();

        if ( it.hasNext() ) {
            throw new IllegalArgumentException( "More than one " + p.getLocalName() + " property given for " + r.toString() );
        }

        return o;
    }

    protected Literal getSingleMandatoryLiteralProperty( final Resource r, final Property p ) {
        final RDFNode v = getSingleMandatoryProperty(r, p);

        if ( ! v.isLiteral() )
            throw new IllegalArgumentException( p.getLocalName() + " property of " + r.toString() + " is not a literal" );

        return v.asLiteral();
    }

    protected String getSingleMandatoryProperty_XSDString( final Resource r, final Property p ) {
        final Literal l = getSingleMandatoryLiteralProperty(r, p);

        if ( ! l.getDatatypeURI().equals(XSD.xstring.getURI()) )
            throw new IllegalArgumentException( p.getLocalName() + " property of " + r.toString() + " is not of type xsd:string" );

        return l.getString();
    }

    protected URI getSingleMandatoryProperty_XSDURI( final Resource r, final Property p ) {
        final Literal l = getSingleMandatoryLiteralProperty(r, p);

        if ( ! l.getDatatypeURI().equals(XSD.anyURI.getURI()) )
            throw new IllegalArgumentException( p.getLocalName() + " property of " + r.toString() + " is not of type xsd:anyURI" );

        final String s = l.getString();
        try{
            return URI.create(s);
        }
        catch ( final Exception e ){
            throw new IllegalArgumentException( p.getLocalName() + " property of " + r.toString() + " is not a valid URI", e );
        }
    }

    protected Resource getMappingResource( final Resource confRsrc, final Property mappingProperty ) {
        return getSingleMandatoryProperty(confRsrc, mappingProperty).asResource();
    }

}
