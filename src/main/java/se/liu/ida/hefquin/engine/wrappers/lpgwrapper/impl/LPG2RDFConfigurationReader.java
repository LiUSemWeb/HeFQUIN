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
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.vocabulary.LPG2RDF;

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

        final String validDataType = lpg2Rdf.getNsPrefixURI("xsd").concat("anyURI");

        final Node label = getLabelPredicate(lpg2rdfConfig, validDataType);
        final NodeMapping nodeMapping = getNodeMapping(lpg2Rdf, lpg2rdfConfig);


        return new LPG2RDFConfigurationImpl(label, nodeMapping);
    }

    public Node getLabelPredicate(final Resource lpg2rdfConfig, final String validDataType){
        if(!lpg2rdfConfig.hasProperty(LPG2RDF.labelPredicate)){
            throw new IllegalArgumentException("labelPredicate is required!");
        }

        final StmtIterator labelPredicateIterator = lpg2rdfConfig.listProperties(LPG2RDF.labelPredicate);

        if(!labelPredicateIterator.hasNext()){
            throw new IllegalArgumentException("labelPredicate is required!");
        }
        final RDFNode labelPredicateObj = labelPredicateIterator.next().getObject();
        if(labelPredicateIterator.hasNext()){
            throw new IllegalArgumentException("More than one labelPredicate!");
        }
        if (!labelPredicateObj.asLiteral().getDatatypeURI().equals(validDataType)){
            throw new IllegalArgumentException("labelPredicate is invalid!");
        }
        final String label = labelPredicateObj.asLiteral().getString();

        return NodeFactory.createURI(label);
    }

    public NodeMapping getNodeMapping(final Model lpg2Rdf, final Resource lpg2rdfConfig){
        final NodeMapping nodeMapping;
        if(!lpg2rdfConfig.hasProperty(LPG2RDF.nodeMapping)){
            throw new IllegalArgumentException("nodeMapping is required!");
        }

        final Resource nodeMappingResource = lpg2rdfConfig.getProperty(LPG2RDF.nodeMapping).getResource();
        final RDFNode nodeMappingResourceType = lpg2Rdf.getRequiredProperty(nodeMappingResource, RDF.type).getObject();

        if ( nodeMappingResourceType.equals(LPG2RDF.IRIBasedNodeMapping) ) {
            final StmtIterator prefixOfIRIsIterator = nodeMappingResource.listProperties(LPG2RDF.prefixOfIRIs);
            if(!prefixOfIRIsIterator.hasNext()){
                throw new IllegalArgumentException("prefixOfIRIs is required!");
            }
            final RDFNode prefixOfIRIObj = prefixOfIRIsIterator.next().getObject();
            if(prefixOfIRIsIterator.hasNext()){
                throw new IllegalArgumentException("More than one prefixOfURI!");
            }

            final String prefixOfIRIUrl = prefixOfIRIObj.asLiteral().getString();
            nodeMapping = new NodeMappingToURIsImpl(prefixOfIRIUrl);
        }
        else if ( nodeMappingResourceType.equals(LPG2RDF.BNodeBasedNodeMapping) ) {
            nodeMapping = new NodeMappingToBNodesImpl();
        }
        else {
            throw new IllegalArgumentException("NodeMapping type (" + nodeMappingResourceType + ") is unexpected!");
        }
        return nodeMapping;
    }
}
