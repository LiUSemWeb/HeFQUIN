package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.CombinedEdgeLabelMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.CombinedNodeLabelMappingImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.CombinedPropertyNameMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.EdgeLabelMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.EdgeLabelMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.LPG2RDFConfigurationImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMappingToLiteralsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeMappingToBNodesImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.RegexBasedEdgeLabelMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.RegexBasedNodeLabelMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.RegexBasedPropertyNameMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.SingleEdgeLabelMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.SingleNodeLabelMappingToLiteralsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.SingleNodeLabelMappingToURIsImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.SinglePropertyNameMappingToURIsImpl;
import se.liu.ida.hefquin.vocabulary.LPG2RDF;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        final NodeLabelMapping nodeLabelMapping = getNodeLabelMapping(lpg2Rdf, lpg2rdfConfig);
        final EdgeLabelMapping edgeLabelMapping = getEdgeLabelMapping(lpg2Rdf, lpg2rdfConfig);
        final PropertyNameMapping propertyNameMapping = getPropertyNameMapping(lpg2Rdf,lpg2rdfConfig);


        return new LPG2RDFConfigurationImpl(label, nodeMapping, nodeLabelMapping, edgeLabelMapping,propertyNameMapping);
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
        catch (final IllegalArgumentException e){
            throw new IllegalArgumentException("labelPredicate (" + label + ") is an invalid URI!");
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
                throw new IllegalArgumentException("An instance of IRIBasedNodeMapping has more than one prefixOfIRIs property!");
            }

            if (!prefixOfIRIObj.isLiteral() || !prefixOfIRIObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
                throw new IllegalArgumentException("prefixOfIRIs is invalid, it should be a xsd:anyURI!");
            }
            final String prefixOfIRIUri = prefixOfIRIObj.asLiteral().getString();
            try{
                return new NodeMappingToURIsImpl(URI.create(prefixOfIRIUri).toString());
            }
            catch (final IllegalArgumentException e){
                throw new IllegalArgumentException("prefixOfIRIs (" + prefixOfIRIUri + ") is an invalid URI!");
            }
        }
        else if ( nodeMappingResourceType.equals(LPG2RDF.BNodeBasedNodeMapping) || (nodeMappingResourceType.equals(LPG2RDF.NodeMapping) && !nodeMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)) ) {
            return new NodeMappingToBNodesImpl();
        }
        else {
            throw new IllegalArgumentException("NodeMapping type (" + nodeMappingResourceType + ") is unexpected!");
        }
    }

    public NodeLabelMapping createIRIBasedNodeLabelMapping(final Resource nodeLabelMappingResource){
        final StmtIterator prefixOfIRIsIterator = nodeLabelMappingResource.listProperties(LPG2RDF.prefixOfIRIs);
        if(!prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("prefixOfIRIs is required!");
        }
        final RDFNode prefixOfIRIObj = prefixOfIRIsIterator.next().getObject();
        if(prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("An instance of IRIBasedNodeLabelMapping has more than one prefixOfIRIs property!");
        }

        if (!prefixOfIRIObj.isLiteral() || !prefixOfIRIObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("prefixOfIRIs is invalid, it should be a xsd:anyURI!");
        }
        final String prefixOfIRIUri = prefixOfIRIObj.asLiteral().getString();
        try{
            return new NodeLabelMappingToURIsImpl(URI.create(prefixOfIRIUri).toString());
        }
        catch (final IllegalArgumentException e){
            throw new IllegalArgumentException("prefixOfIRIs (" + prefixOfIRIUri + ") is an invalid URI!");
        }
    }

    public NodeLabelMapping createRegexIRIBasedNodeLabelMapping(final Resource nodeLabelMappingResource){
        final StmtIterator regexIterator = nodeLabelMappingResource.listProperties(LPG2RDF.regex);
        if (!regexIterator.hasNext()) {
            throw new IllegalArgumentException("regex is required!");
        }
        final RDFNode regexObj = regexIterator.next().getObject();
        if (regexIterator.hasNext()) {
            throw new IllegalArgumentException("An instance of RegexIRIBasedNodeLabelMapping has more than one regex property!");
        }

        if (!regexObj.isLiteral() || !regexObj.asLiteral().getDatatypeURI().equals(XSD.xstring.getURI())) {
            throw new IllegalArgumentException("Regex is invalid, it should be a xsd:string!");
        }
        final StmtIterator prefixOfIRIsIterator = nodeLabelMappingResource.listProperties(LPG2RDF.prefixOfIRIs);
        if(!prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("prefixOfIRIs is required!");
        }
        final RDFNode prefixOfIRIObj = prefixOfIRIsIterator.next().getObject();
        if(prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("An instance of RegexIRIBasedNodeLabelMapping has more than one prefixOfIRIs property!");
        }

        if (!prefixOfIRIObj.isLiteral() || !prefixOfIRIObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("prefixOfIRIs is invalid, it should be a xsd:anyURI!");
        }
        final String regex = regexObj.asLiteral().getString();
        final String prefixOfIRIUri = prefixOfIRIObj.asLiteral().getString();
        try {
            return new RegexBasedNodeLabelMappingToURIsImpl(regex, URI.create(prefixOfIRIUri).toString());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("prefixOfIRIs (" + prefixOfIRIUri + ") is an invalid URI!");
        }
    }

    public NodeLabelMapping createSingleIRIBasedNodeLabelMapping(final Resource nodeLabelMappingResource){
        final StmtIterator labelIterator = nodeLabelMappingResource.listProperties(LPG2RDF.label);
        if (!labelIterator.hasNext()) {
            throw new IllegalArgumentException("label is required!");
        }
        final RDFNode labelObj = labelIterator.next().getObject();
        if (labelIterator.hasNext()) {
            throw new IllegalArgumentException("An instance of SingleIRIBasedNodeLabelMapping has more than one label property!");
        }

        if (!labelObj.isLiteral() || !labelObj.asLiteral().getDatatypeURI().equals(XSD.xstring.getURI())) {
            throw new IllegalArgumentException("Label is invalid, it should be a xsd:string!");
        }
        final StmtIterator iriIterator = nodeLabelMappingResource.listProperties(LPG2RDF.iri);
        if(!iriIterator.hasNext()){
            throw new IllegalArgumentException("iri is required!");
        }
        final RDFNode iriObj = iriIterator.next().getObject();
        if(iriIterator.hasNext()){
            throw new IllegalArgumentException("An instance of SingleIRIBasedNodeLabelMapping has more than one iri property!");
        }

        if (!iriObj.isLiteral() || !iriObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("iri is invalid, it should be a xsd:anyURI!");
        }
        final String label = labelObj.asLiteral().getString();
        final String iri = iriObj.asLiteral().getString();
        try {
            return new SingleNodeLabelMappingToURIsImpl(label,iri);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("iri (" + iri +") is an invalid URI!");
        }
    }

    public NodeLabelMapping createSingleLiteralBasedNodeLabelMapping(final Resource nodeLabelMappingResource){
        final StmtIterator labelIterator = nodeLabelMappingResource.listProperties(LPG2RDF.label);
        if (!labelIterator.hasNext()) {
            throw new IllegalArgumentException("label is required!");
        }
        final RDFNode labelObj = labelIterator.next().getObject();
        if (labelIterator.hasNext()) {
            throw new IllegalArgumentException("An instance of SingleLiteralBasedNodeLabelMapping has more than one label property!");
        }

        if (!labelObj.isLiteral() || !labelObj.asLiteral().getDatatypeURI().equals(XSD.xstring.getURI())) {
            throw new IllegalArgumentException("Label is invalid, it should be a xsd:string!");
        }
        final StmtIterator literalIterator = nodeLabelMappingResource.listProperties(LPG2RDF.literal);
        if(!literalIterator.hasNext()){
            throw new IllegalArgumentException("literal is required!");
        }
        final RDFNode literalObj = literalIterator.next().getObject();
        if(literalIterator.hasNext()){
            throw new IllegalArgumentException("An instance of SingleLiteralBasedNodeLabelMapping has more than one literal property!");
        }

        if (!literalObj.isLiteral() || !literalObj.asLiteral().getDatatypeURI().equals(XSD.xstring.getURI())){
            throw new IllegalArgumentException("literal is invalid, it should be a xsd:string!");
        }
        final String label = labelObj.asLiteral().getString();
        final String literal = literalObj.asLiteral().getString();
        return new SingleNodeLabelMappingToLiteralsImpl(label,literal);
    }

    public NodeLabelMapping createCombinedNodeLabelMapping(final Resource nodeLabelMappingResource){
        final List<NodeLabelMapping> nodeLabelMappings = new ArrayList<>();
        final StmtIterator nodeLabelMappingsPropertyIterator = nodeLabelMappingResource.listProperties(LPG2RDF.nodeLabelMappings);
        if (!nodeLabelMappingsPropertyIterator.hasNext()) {
            throw new IllegalArgumentException("nodeLabelMappings is required!");
        }
        final RDFNode nodeLabelMappingsList = nodeLabelMappingsPropertyIterator.next().getObject();
        if(!nodeLabelMappingsList.canAs(RDFList.class)){
            throw new IllegalArgumentException("NodeLabelMappings property of CombinedNodeLabelMapping should be a list!");
        }
        final Iterator nodeLabelMappingsIterator = nodeLabelMappingsList.as(RDFList.class).iterator();

        if (!nodeLabelMappingsIterator.hasNext()) {
            throw new IllegalArgumentException("NodeLabelMappings list of CombinedNodeLabelMapping should not be empty!");
        }

        do{
            final Resource nodeLabelMapping = (Resource)nodeLabelMappingsIterator.next();
            final RDFNode nodeLabelMappingType = nodeLabelMapping.getProperty(RDF.type).getObject();
            if(nodeLabelMappingType.equals(LPG2RDF.CombinedNodeLabelMapping)
                    || (nodeLabelMappingType.equals(LPG2RDF.NodeLabelMapping) && nodeLabelMapping.hasProperty(LPG2RDF.nodeLabelMappings))){
                throw new IllegalArgumentException("CombinedNodeLabelMapping Should not have an object of CombinedNodeLabelMapping as nodeLabelMapping!");
            }
            nodeLabelMappings.add(createNodeLabelMapping(nodeLabelMapping, nodeLabelMappingType));
        }while(nodeLabelMappingsIterator.hasNext());
        return new CombinedNodeLabelMappingImpl(nodeLabelMappings);
    }

    public NodeLabelMapping createNodeLabelMapping(final Resource nodeLabelMappingResource, final RDFNode nodeLabelMappingResourceType){
        if (nodeLabelMappingResourceType.equals(LPG2RDF.RegexIRIBasedNodeLabelMapping)
                || (nodeLabelMappingResourceType.equals(LPG2RDF.NodeLabelMapping) && nodeLabelMappingResource.hasProperty(LPG2RDF.regex)&& nodeLabelMappingResource.hasProperty(LPG2RDF.prefixOfIRIs))) {
            return createRegexIRIBasedNodeLabelMapping(nodeLabelMappingResource);
        }else if ( nodeLabelMappingResourceType.equals(LPG2RDF.IRIBasedNodeLabelMapping)
                || (nodeLabelMappingResourceType.equals(LPG2RDF.NodeLabelMapping) && nodeLabelMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)) ) {
            return createIRIBasedNodeLabelMapping(nodeLabelMappingResource);
        } else if (nodeLabelMappingResourceType.equals(LPG2RDF.LiteralBasedNodeLabelMapping)) {
            return new NodeLabelMappingToLiteralsImpl();
        } else if (nodeLabelMappingResourceType.equals(LPG2RDF.SingleIRIBasedNodeLabelMapping)
                || (nodeLabelMappingResourceType.equals(LPG2RDF.NodeLabelMapping) && nodeLabelMappingResource.hasProperty(LPG2RDF.label)
                && nodeLabelMappingResource.hasProperty(LPG2RDF.iri))) {
            return createSingleIRIBasedNodeLabelMapping(nodeLabelMappingResource);
        }else if (nodeLabelMappingResourceType.equals(LPG2RDF.SingleLiteralBasedNodeLabelMapping)
                || (nodeLabelMappingResourceType.equals(LPG2RDF.NodeLabelMapping) && nodeLabelMappingResource.hasProperty(LPG2RDF.label)
                && nodeLabelMappingResource.hasProperty(LPG2RDF.literal))) {
            return createSingleLiteralBasedNodeLabelMapping(nodeLabelMappingResource);
        } else if (nodeLabelMappingResourceType.equals(LPG2RDF.CombinedNodeLabelMapping)
                || (nodeLabelMappingResourceType.equals(LPG2RDF.NodeLabelMapping) && nodeLabelMappingResource.hasProperty(LPG2RDF.nodeLabelMappings))) {
            return createCombinedNodeLabelMapping(nodeLabelMappingResource);
        } else {
            throw new IllegalArgumentException("NodeLabelMapping type (" + nodeLabelMappingResourceType + ") is unexpected!");
        }
    }

    public NodeLabelMapping getNodeLabelMapping(final Model lpg2Rdf, final Resource lpg2rdfConfig){

        final StmtIterator nodeLabelMappingIterator = lpg2rdfConfig.listProperties(LPG2RDF.nodeLabelMapping);

        if(!nodeLabelMappingIterator.hasNext()){
            throw new IllegalArgumentException("nodeLabelMapping is required!");
        }
        final Resource nodeLabelMappingResource = nodeLabelMappingIterator.next().getObject().asResource();
        if(nodeLabelMappingIterator.hasNext()){
            throw new IllegalArgumentException("More than one instance of nodeLabelMapping!");
        }

        final RDFNode nodeLabelMappingResourceType = lpg2Rdf.getRequiredProperty(nodeLabelMappingResource, RDF.type).getObject();

        return createNodeLabelMapping(nodeLabelMappingResource, nodeLabelMappingResourceType);
    }

    public EdgeLabelMapping createIRIBasedEdgeLabelMapping(final Resource edgeLabelMappingResource){
        final StmtIterator prefixOfIRIsIterator = edgeLabelMappingResource.listProperties(LPG2RDF.prefixOfIRIs);
        if(!prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("prefixOfIRIs is required!");
        }
        final RDFNode prefixOfIRIObj = prefixOfIRIsIterator.next().getObject();
        if(prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("An instance of IRIBasedEdgeLabelMapping has more than one prefixOfIRIs property!");
        }

        if (!prefixOfIRIObj.isLiteral() || !prefixOfIRIObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("prefixOfIRIs is invalid, it should be a xsd:anyURI!");
        }
        final String prefixOfIRIUri = prefixOfIRIObj.asLiteral().getString();
        try{
            return new EdgeLabelMappingToURIsImpl(URI.create(prefixOfIRIUri).toString());
        }
        catch (final IllegalArgumentException e){
            throw new IllegalArgumentException("prefixOfIRIs (" + prefixOfIRIUri + ") is an invalid URI!");
        }
    }

    public EdgeLabelMapping createRegexBasedEdgeLabelMapping(final Resource edgeLabelMappingResource){
        final StmtIterator regexIterator = edgeLabelMappingResource.listProperties(LPG2RDF.regex);
        if (!regexIterator.hasNext()) {
            throw new IllegalArgumentException("regex is required!");
        }
        final RDFNode regexObj = regexIterator.next().getObject();
        if (regexIterator.hasNext()) {
            throw new IllegalArgumentException("An instance of RegexEdgeLabelMapping has more than one regex property!");
        }

        if (!regexObj.isLiteral() || !regexObj.asLiteral().getDatatypeURI().equals(XSD.xstring.getURI())) {
            throw new IllegalArgumentException("Regex is invalid, it should be a xsd:string!");
        }
        final StmtIterator prefixOfIRIsIterator = edgeLabelMappingResource.listProperties(LPG2RDF.prefixOfIRIs);
        if(!prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("prefixOfIRIs is required!");
        }
        final RDFNode prefixOfIRIObj = prefixOfIRIsIterator.next().getObject();
        if(prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("An instance of IRIBasedEdgeLabelMapping has more than one prefixOfIRIs property!");
        }

        if (!prefixOfIRIObj.isLiteral() || !prefixOfIRIObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("prefixOfIRIs is invalid, it should be a xsd:anyURI!");
        }
        final String regex = regexObj.asLiteral().getString();
        final String prefixOfIRIUri = prefixOfIRIObj.asLiteral().getString();
        try {
            return new RegexBasedEdgeLabelMappingToURIsImpl(regex, URI.create(prefixOfIRIUri).toString());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("prefixOfIRIs (" + prefixOfIRIUri + ") is an invalid URI!");
        }
    }

    public EdgeLabelMapping createSingleEdgeLabelMapping(final Resource edgeLabelMappingResource){
        final StmtIterator labelIterator = edgeLabelMappingResource.listProperties(LPG2RDF.label);
        if (!labelIterator.hasNext()) {
            throw new IllegalArgumentException("label is required!");
        }
        final RDFNode labelObj = labelIterator.next().getObject();
        if (labelIterator.hasNext()) {
            throw new IllegalArgumentException("An instance of SingleEdgeLabelMapping has more than one label property!");
        }

        if (!labelObj.isLiteral() || !labelObj.asLiteral().getDatatypeURI().equals(XSD.xstring.getURI())) {
            throw new IllegalArgumentException("Label is invalid, it should be a xsd:string!");
        }
        final StmtIterator iriIterator = edgeLabelMappingResource.listProperties(LPG2RDF.iri);
        if(!iriIterator.hasNext()){
            throw new IllegalArgumentException("iri is required!");
        }
        final RDFNode iriObj = iriIterator.next().getObject();
        if(iriIterator.hasNext()){
            throw new IllegalArgumentException("An instance of SingleEdgeLabelMapping has more than one iri property!");
        }

        if (!iriObj.isLiteral() || !iriObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("iri is invalid, it should be a xsd:anyURI!");
        }
        final String label = labelObj.asLiteral().getString();
        final String iri = iriObj.asLiteral().getString();
        try {
            return new SingleEdgeLabelMappingToURIsImpl(label,iri);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("iri (" + iri +") is an invalid URI!");
        }
    }

    public EdgeLabelMapping createCombinedEdgeLabelMapping(final Resource edgeLabelMappingResource){
        final List<EdgeLabelMapping> edgeLabelMappings = new ArrayList<>();
        final StmtIterator edgeLabelMappingsPropertyIterator = edgeLabelMappingResource.listProperties(LPG2RDF.edgeLabelMappings);
        if (!edgeLabelMappingsPropertyIterator.hasNext()) {
            throw new IllegalArgumentException("edgeLabelMappings is required!");
        }
        final RDFNode edgeLabelMappingsList = edgeLabelMappingsPropertyIterator.next().getObject();
        if(!edgeLabelMappingsList.canAs(RDFList.class)){
            throw new IllegalArgumentException("EdgeLabelMappings property of CombinedEdgeLabelMapping should be a list!");
        }
        final Iterator edgeLabelMappingsIterator = edgeLabelMappingsList.as(RDFList.class).iterator();

        if (!edgeLabelMappingsIterator.hasNext()) {
            throw new IllegalArgumentException("EdgeLabelMappings list of CombinedEdgeLabelMapping should not be empty!");
        }

        do{
            final Resource edgeLabelMapping = (Resource)edgeLabelMappingsIterator.next();
            final RDFNode edgeLabelMappingType = edgeLabelMapping.getProperty(RDF.type).getObject();
            if(edgeLabelMappingType.equals(LPG2RDF.CombinedEdgeLabelMapping)
                    || (edgeLabelMappingType.equals(LPG2RDF.EdgeLabelMapping) && edgeLabelMapping.hasProperty(LPG2RDF.edgeLabelMappings))){
                throw new IllegalArgumentException("CombinedEdgeLabelMapping Should not have an object of CombinedEdgeLabelMapping as edgeLabelMapping!");
            }
            edgeLabelMappings.add(createEdgeLabelMapping(edgeLabelMapping, edgeLabelMappingType));
        }while(edgeLabelMappingsIterator.hasNext());
        return new CombinedEdgeLabelMappingToURIsImpl(edgeLabelMappings);
    }

    public EdgeLabelMapping createEdgeLabelMapping(final Resource edgeLabelMappingResource, final RDFNode edgeLabelMappingResourceType){
        if ( edgeLabelMappingResourceType.equals(LPG2RDF.RegexBasedEdgeLabelMapping)
                || (edgeLabelMappingResourceType.equals(LPG2RDF.EdgeLabelMapping) && edgeLabelMappingResource.hasProperty(LPG2RDF.regex)) ) {
            return createRegexBasedEdgeLabelMapping(edgeLabelMappingResource);
        }
        else if ( edgeLabelMappingResourceType.equals(LPG2RDF.IRIBasedEdgeLabelMapping)
                || (edgeLabelMappingResourceType.equals(LPG2RDF.EdgeLabelMapping) && edgeLabelMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)) ) {
            return createIRIBasedEdgeLabelMapping(edgeLabelMappingResource);
        }
        else if ( edgeLabelMappingResourceType.equals(LPG2RDF.SingleEdgeLabelMapping)
                || (edgeLabelMappingResourceType.equals(LPG2RDF.EdgeLabelMapping) && edgeLabelMappingResource.hasProperty(LPG2RDF.label)
                && edgeLabelMappingResource.hasProperty(LPG2RDF.iri))) {
            return createSingleEdgeLabelMapping(edgeLabelMappingResource);
        }
        else if ( edgeLabelMappingResourceType.equals(LPG2RDF.CombinedEdgeLabelMapping)
                || (edgeLabelMappingResourceType.equals(LPG2RDF.EdgeLabelMapping) && edgeLabelMappingResource.hasProperty(LPG2RDF.edgeLabelMappings))) {
            return createCombinedEdgeLabelMapping(edgeLabelMappingResource);
        }
        else {
            throw new IllegalArgumentException("EdgeLabelMapping type (" + edgeLabelMappingResourceType + ") is unexpected!");
        }
    }

    public EdgeLabelMapping getEdgeLabelMapping(final Model lpg2Rdf, final Resource lpg2rdfConfig){

        final StmtIterator edgeLabelMappingIterator = lpg2rdfConfig.listProperties(LPG2RDF.edgeLabelMapping);

        if(!edgeLabelMappingIterator.hasNext()){
            throw new IllegalArgumentException("edgeLabelMapping is required!");
        }
        final Resource edgeLabelMappingResource = edgeLabelMappingIterator.next().getObject().asResource();
        if(edgeLabelMappingIterator.hasNext()){
            throw new IllegalArgumentException("More than one instance of edgeLabelMapping!");
        }

        final RDFNode edgeLabelMappingResourceType = lpg2Rdf.getRequiredProperty(edgeLabelMappingResource, RDF.type).getObject();

        return createEdgeLabelMapping(edgeLabelMappingResource, edgeLabelMappingResourceType);
    }

    public PropertyNameMapping createIRIBasedPropertyNameMapping(final Resource propertyNameMappingResource){
        final StmtIterator prefixOfIRIsIterator = propertyNameMappingResource.listProperties(LPG2RDF.prefixOfIRIs);
        if(!prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("prefixOfIRIs is required!");
        }
        final RDFNode prefixOfIRIObj = prefixOfIRIsIterator.next().getObject();
        if(prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("An instance of IRIBasedPropertyNameMapping has more than one prefixOfIRIs property!");
        }

        if (!prefixOfIRIObj.isLiteral() || !prefixOfIRIObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("prefixOfIRIs is invalid, it should be a xsd:anyURI!");
        }
        final String prefixOfIRIUri = prefixOfIRIObj.asLiteral().getString();
        try{
            return new PropertyNameMappingToURIsImpl(URI.create(prefixOfIRIUri).toString());
        }
        catch (final IllegalArgumentException e){
            throw new IllegalArgumentException("prefixOfIRIs (" + prefixOfIRIUri + ") is an invalid URI!");
        }
    }

    public PropertyNameMapping createRegexBasedPropertyNameMapping(final Resource propertyNameMappingResource){
        final StmtIterator regexIterator = propertyNameMappingResource.listProperties(LPG2RDF.regex);
        if (!regexIterator.hasNext()) {
            throw new IllegalArgumentException("regex is required!");
        }
        final RDFNode regexObj = regexIterator.next().getObject();
        if (regexIterator.hasNext()) {
            throw new IllegalArgumentException("An instance of RegexPropertyNameMapping has more than one regex property!");
        }

        if (!regexObj.isLiteral() || !regexObj.asLiteral().getDatatypeURI().equals(XSD.xstring.getURI())) {
            throw new IllegalArgumentException("Regex is invalid, it should be a xsd:string!");
        }
        final StmtIterator prefixOfIRIsIterator = propertyNameMappingResource.listProperties(LPG2RDF.prefixOfIRIs);
        if(!prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("prefixOfIRIs is required!");
        }
        final RDFNode prefixOfIRIObj = prefixOfIRIsIterator.next().getObject();
        if(prefixOfIRIsIterator.hasNext()){
            throw new IllegalArgumentException("An instance of RegexBasedPropertyNameMapping has more than one prefixOfIRIs property!");
        }

        if (!prefixOfIRIObj.isLiteral() || !prefixOfIRIObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("prefixOfIRIs is invalid, it should be a xsd:anyURI!");
        }
        final String regex = regexObj.asLiteral().getString();
        final String prefixOfIRIUri = prefixOfIRIObj.asLiteral().getString();
        try {
            return new RegexBasedPropertyNameMappingToURIsImpl(regex, URI.create(prefixOfIRIUri).toString());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("prefixOfIRIs (" + prefixOfIRIUri + ") is an invalid URI!");
        }
    }

    public PropertyNameMapping createSinglePropertyNameMapping(final Resource propertyNameMappingResource){
        final StmtIterator propertyNameIterator = propertyNameMappingResource.listProperties(LPG2RDF.propertyName);
        if (!propertyNameIterator.hasNext()) {
            throw new IllegalArgumentException("propertyName is required!");
        }
        final RDFNode propertyNameObj = propertyNameIterator.next().getObject();
        if (propertyNameIterator.hasNext()) {
            throw new IllegalArgumentException("An instance of SinglePropertyNameMapping has more than one propertyName property!");
        }

        if (!propertyNameObj.isLiteral() || !propertyNameObj.asLiteral().getDatatypeURI().equals(XSD.xstring.getURI())) {
            throw new IllegalArgumentException("Property Name is invalid, it should be a xsd:string!");
        }
        final StmtIterator iriIterator = propertyNameMappingResource.listProperties(LPG2RDF.iri);
        if(!iriIterator.hasNext()){
            throw new IllegalArgumentException("iri is required!");
        }
        final RDFNode iriObj = iriIterator.next().getObject();
        if(iriIterator.hasNext()){
            throw new IllegalArgumentException("An instance of SinglePropertyNameMapping has more than one iri property!");
        }

        if (!iriObj.isLiteral() || !iriObj.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI())){
            throw new IllegalArgumentException("iri is invalid, it should be a xsd:anyURI!");
        }
        final String propertyName = propertyNameObj.asLiteral().getString();
        final String iri = iriObj.asLiteral().getString();
        try {
            return new SinglePropertyNameMappingToURIsImpl(propertyName,iri);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("iri (" + iri + ") is an invalid URI!");
        }
    }

    public PropertyNameMapping createCombinedPropertyNameMapping(final Resource propertyNameMappingResource){
        final List<PropertyNameMapping> propertyNameMappings = new ArrayList<>();
        final StmtIterator propertyNameMappingsPropertyIterator = propertyNameMappingResource.listProperties(LPG2RDF.propertyNameMappings);
        if (!propertyNameMappingsPropertyIterator.hasNext()) {
            throw new IllegalArgumentException("propertyNameMappings is required!");
        }
        final RDFNode propertyNameMappingsList = propertyNameMappingsPropertyIterator.next().getObject();
        if(!propertyNameMappingsList.canAs(RDFList.class)){
            throw new IllegalArgumentException("PropertyNameMappings property of CombinedPropertyNameMapping should be a list!");
        }
        final Iterator propertyNameMappingsIterator = propertyNameMappingsList.as(RDFList.class).iterator();

        if (!propertyNameMappingsIterator.hasNext()) {
            throw new IllegalArgumentException("PropertyNameMappings list of CombinedPropertyNameMapping should not be empty!");
        }

        do{
            final Resource propertyNameMapping = (Resource)propertyNameMappingsIterator.next();
            final RDFNode propertyNameMappingType = propertyNameMapping.getProperty(RDF.type).getObject();
            if(propertyNameMappingType.equals(LPG2RDF.CombinedPropertyNameMapping)
                    || (propertyNameMappingType.equals(LPG2RDF.PropertyNameMapping) && propertyNameMapping.hasProperty(LPG2RDF.propertyNameMappings))){
                throw new IllegalArgumentException("CombinedPropertyNameMapping Should not have an object of CombinedPropertyNameMapping as propertyNameMapping!");
            }
            propertyNameMappings.add(createPropertyNameMapping(propertyNameMapping, propertyNameMappingType));
        }while(propertyNameMappingsIterator.hasNext());
        return new CombinedPropertyNameMappingToURIsImpl(propertyNameMappings);
    }

    public PropertyNameMapping createPropertyNameMapping(final Resource propertyNameMappingResource, final RDFNode propertyNameMappingResourceType){
        if ( propertyNameMappingResourceType.equals(LPG2RDF.RegexBasedPropertyNameMapping)
                || (propertyNameMappingResourceType.equals(LPG2RDF.PropertyNameMapping) && propertyNameMappingResource.hasProperty(LPG2RDF.regex)) ) {
            return createRegexBasedPropertyNameMapping(propertyNameMappingResource);
        }
        else if ( propertyNameMappingResourceType.equals(LPG2RDF.IRIBasedPropertyNameMapping)
                || (propertyNameMappingResourceType.equals(LPG2RDF.PropertyNameMapping) && propertyNameMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)) ) {
            return createIRIBasedPropertyNameMapping(propertyNameMappingResource);
        }
        else if ( propertyNameMappingResourceType.equals(LPG2RDF.SinglePropertyNameMapping)
                || (propertyNameMappingResourceType.equals(LPG2RDF.PropertyNameMapping) && propertyNameMappingResource.hasProperty(LPG2RDF.propertyName)
                && propertyNameMappingResource.hasProperty(LPG2RDF.iri))) {
            return createSinglePropertyNameMapping(propertyNameMappingResource);
        }
        else if ( propertyNameMappingResourceType.equals(LPG2RDF.CombinedPropertyNameMapping)
                || (propertyNameMappingResourceType.equals(LPG2RDF.PropertyNameMapping) && propertyNameMappingResource.hasProperty(LPG2RDF.propertyNameMappings))) {
            return createCombinedPropertyNameMapping(propertyNameMappingResource);
        }
        else {
            throw new IllegalArgumentException("PropertyNameMapping type (" + propertyNameMappingResourceType + ") is unexpected!");
        }
    }

    public PropertyNameMapping getPropertyNameMapping(final Model lpg2Rdf, final Resource lpg2rdfConfig){

        final StmtIterator propertyNameMappingIterator = lpg2rdfConfig.listProperties(LPG2RDF.propertyNameMapping);

        if(!propertyNameMappingIterator.hasNext()){
            throw new IllegalArgumentException("propertyNameMapping is required!");
        }
        final Resource propertyNameMappingResource = propertyNameMappingIterator.next().getObject().asResource();
        if(propertyNameMappingIterator.hasNext()){
            throw new IllegalArgumentException("More than one instance of propertyNameMapping!");
        }

        final RDFNode propertyNameMappingResourceType = lpg2Rdf.getRequiredProperty(propertyNameMappingResource, RDF.type).getObject();

        return createPropertyNameMapping(propertyNameMappingResource, propertyNameMappingResourceType);
    }
}
