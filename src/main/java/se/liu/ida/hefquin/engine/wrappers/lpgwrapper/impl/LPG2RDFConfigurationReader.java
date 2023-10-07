package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.vocabulary.LPG2RDF;

import java.net.URI;

public class LPG2RDFConfigurationReader {

    public static LPG2RDFConfigurationReader instance = new LPG2RDFConfigurationReader();

    protected LPG2RDFConfigurationReader(){}

    public static LPG2RDFConfiguration readFromFile(final String filename ) {
        return instance.parseLPG2RDFConf(filename);
    }

    public static LPG2RDFConfiguration readFromModel(final Model lpg2Rdf ) {
        return instance.parseLPG2RDFConf(lpg2Rdf);
    }

    public LPG2RDFConfiguration parseLPG2RDFConf( final String filename ) {
        final Model lpg2Rdf = RDFDataMgr.loadModel(filename);
        return parseLPG2RDFConf(lpg2Rdf);
    }

    public LPG2RDFConfiguration parseLPG2RDFConf(final Model lpg2Rdf ) {

        final ResIterator lpg2rdfConfigs = lpg2Rdf.listResourcesWithProperty(RDF.type, LPG2RDF.LPGtoRDFConfiguration);

        if (!lpg2rdfConfigs.hasNext()){
            throw new IllegalArgumentException("LPGtoRDFConfiguration is required!");
        }
        final Resource lpg2rdfConfig = lpg2rdfConfigs.next();
        if(lpg2rdfConfigs.hasNext()){
            throw new IllegalArgumentException("More than one instance of LPGtoRDFConfiguration!");
        }

        final Node label = getLabelPredicate(lpg2rdfConfig);
        final NodeMapping nodeMapping = getNodeMapping(lpg2Rdf, lpg2rdfConfig);


        return new LPG2RDFConfigurationImpl(label, nodeMapping);
    }

    public Node getLabelPredicate(final Resource lpg2rdfConfig){

        final StmtIterator labelPredicateIterator = lpg2rdfConfig.listProperties(LPG2RDF.labelPredicate);

        if(!labelPredicateIterator.hasNext()){
            throw new IllegalArgumentException("labelPredicate is required!");
        }
        final RDFNode labelPredicateObj = labelPredicateIterator.next().getObject();
        if(labelPredicateIterator.hasNext()){
            throw new IllegalArgumentException("More than one labelPredicate!");
        }

        if (!labelPredicateObj.isLiteral() || !labelPredicateObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("labelPredicate is invalid, it should be a xsd:anyURI!");
        }

        final String label = labelPredicateObj.asLiteral().getString();

        try{
            return NodeFactory.createURI(URI.create(label).toString());
        }
        catch (IllegalArgumentException exception){
            throw new IllegalArgumentException("labelPredicate is an invalid URI!");
        }

    }

    public NodeMapping getNodeMapping(final Model lpg2Rdf, final Resource lpg2rdfConfig){

        final StmtIterator nodeMappingIterator = lpg2rdfConfig.listProperties(LPG2RDF.nodeMapping);

        if(!nodeMappingIterator.hasNext()){
            throw new IllegalArgumentException("nodeMapping is required!");
        }
        final Resource nodeMappingResource = nodeMappingIterator.next().getObject().asResource();
        if(nodeMappingIterator.hasNext()){
            throw new IllegalArgumentException("More than one instance of nodeMapping!");
        }

        final RDFNode nodeMappingResourceType = lpg2Rdf.getRequiredProperty(nodeMappingResource, RDF.type).getObject();

        if ( nodeMappingResourceType.equals(LPG2RDF.IRIBasedNodeMapping)
                || (nodeMappingResourceType.equals(LPG2RDF.NodeMapping) && nodeMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)) ) {
            final StmtIterator prefixOfIRIsIterator = nodeMappingResource.listProperties(LPG2RDF.prefixOfIRIs);
            if(!prefixOfIRIsIterator.hasNext()){
                throw new IllegalArgumentException("prefixOfIRIs is required!");
            }
            final RDFNode prefixOfIRIObj = prefixOfIRIsIterator.next().getObject();
            if(prefixOfIRIsIterator.hasNext()){
                throw new IllegalArgumentException("An instance of IRIBasedNodeMapping has more than one prefixOfIRIs property.!");
            }

            if (!prefixOfIRIObj.isLiteral() || !prefixOfIRIObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
                throw new IllegalArgumentException("prefixOfIRIs is invalid, it should be a xsd:anyURI!");
            }
            final String prefixOfIRIUri = prefixOfIRIObj.asLiteral().getString();
            try{
                return new NodeMappingToURIsImpl(URI.create(prefixOfIRIUri).toString());
            }
            catch (IllegalArgumentException exception){
                throw new IllegalArgumentException("prefixOfIRIs is an invalid URI!");
            }
        }
        else if ( nodeMappingResourceType.equals(LPG2RDF.BNodeBasedNodeMapping) || (nodeMappingResourceType.equals(LPG2RDF.NodeMapping) && !nodeMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)) ) {
            return new NodeMappingToBNodesImpl();
        }
        else {
            throw new IllegalArgumentException("NodeMapping type (" + nodeMappingResourceType + ") is unexpected!");
        }
    }
}
