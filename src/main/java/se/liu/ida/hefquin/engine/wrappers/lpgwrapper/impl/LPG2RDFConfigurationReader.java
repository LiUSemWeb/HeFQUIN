package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceRequiredException;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import se.liu.ida.hefquin.vocabulary.LPG2RDF;

import java.net.URI;
import java.net.URISyntaxException;

public class LPG2RDFConfigurationReader {

    public static LPG2RDFConfigurationReader instance = new LPG2RDFConfigurationReader();

    protected LPG2RDFConfigurationReader(){}

    public static LPG2RDFConfigurationImpl readFromFile( final String filename ) {
        return instance.parseLPG2RDFConf(filename);
    }

    public static LPG2RDFConfigurationImpl readFromModel(final Model lpg2Rdf ) {
        return instance.parseLPG2RDFConf(lpg2Rdf);
    }

    public LPG2RDFConfigurationImpl parseLPG2RDFConf( final String filename ) {
        final Model lpg2Rdf = RDFDataMgr.loadModel(filename);
        return parseLPG2RDFConf(lpg2Rdf);
    }

    public LPG2RDFConfigurationImpl parseLPG2RDFConf(final Model lpg2Rdf ) {
        final NodeMapping nodeMapping;
        final String label;

        final ResIterator lpg2rdfConfigs = lpg2Rdf.listResourcesWithProperty(RDF.type, LPG2RDF.LPGtoRDFConfiguration);

        if (!lpg2rdfConfigs.hasNext()){
            throw new IllegalArgumentException("LPGtoRDFConfiguration is required!");
        }
        else {
            final Resource lpg2rdfConfig = lpg2rdfConfigs.next();

            verifyLPG2RDFConfiguration(lpg2rdfConfig);

            if(!lpg2rdfConfig.hasProperty(LPG2RDF.labelPredicate)){
                throw new IllegalArgumentException("labelPredicate is required!");
            }
            final RDFNode labelPredicateObj = lpg2rdfConfig.getProperty(LPG2RDF.labelPredicate).getObject();
            String url;
            try {
                url = labelPredicateObj.asResource().getURI();
            }
            catch (ResourceRequiredException re){
                throw new IllegalArgumentException("labelPredicate is invalid!");
            }
            label = verifyExpectedURI(url).toString();

            final Resource nodeMappingResource = lpg2rdfConfig.getProperty(LPG2RDF.nodeMapping).getResource();
            final RDFNode nodeMappingResourceType = lpg2Rdf.getRequiredProperty(nodeMappingResource, RDF.type).getObject();

            if ( nodeMappingResourceType.equals(LPG2RDF.IRIBasedNodeMapping) ) {
                if(!nodeMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)){
                    throw new IllegalArgumentException("prefixOfIRIs is required!");
                }
                final RDFNode prefixOfIRIObj = nodeMappingResource.getProperty(LPG2RDF.prefixOfIRIs).getObject();
                final String prefixOfIRIUrl = verifyExpectedURI(prefixOfIRIObj.asResource().getURI()).toString();
                nodeMapping = new NodeMappingToURIsImpl(prefixOfIRIUrl);
            }
            else if ( nodeMappingResourceType.equals(LPG2RDF.BNodeBasedNodeMapping) ) {
                nodeMapping = new NodeMappingToBNodesImpl();
            }
            else {
                throw new IllegalArgumentException("Type of Resource in NodeMapping is not defined.");
            }
        }
        return new LPG2RDFConfigurationImpl(NodeFactory.createURI(label), nodeMapping);
    }


    /**
     * Verifies that the given string represents an HTTP URI
     * or an HTTPS URI and, if so, returns that URI.
     */
    protected URI verifyExpectedURI(final String uriString ) {
        final URI uri;
        try {
            uri = new URI(uriString);
            if (    ! uri.isAbsolute()
                    || (uri.getScheme().equals("http") && uri.getScheme().equals("https")) ) {
                throw new IllegalArgumentException( "The following URI is of an unexpected type; it should be an HTTP URI or an HTTPS URI: " + uriString );
            }
        }
        catch ( final URISyntaxException ex ) {
            throw new IllegalArgumentException("URI parse exception (" + ex.getMessage() + ")");
        }

        return uri;
    }

    protected void verifyLPG2RDFConfiguration(final Resource lpg2rdfConf){
        if(!lpg2rdfConf.hasProperty(LPG2RDF.nodeMapping)){
            throw new IllegalArgumentException("nodeMapping is required!");
        }
        else if(!lpg2rdfConf.hasProperty(LPG2RDF.labelPredicate)){
            throw new IllegalArgumentException("labelPredicate is required!");
        }
    }
}
