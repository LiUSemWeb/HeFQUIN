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
                throw new IllegalArgumentException("An instance of IRIBasedNodeMapping has more than one prefixOfIRIs property!");
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

        if ( nodeLabelMappingResourceType.equals(LPG2RDF.IRIBasedNodeLabelMapping)
                || (nodeLabelMappingResourceType.equals(LPG2RDF.NodeLabelMapping) && nodeLabelMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)) ) {
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
            catch (IllegalArgumentException exception){
                throw new IllegalArgumentException("prefixOfIRIs is an invalid URI!");
            }
        }
        else if ( nodeLabelMappingResourceType.equals(LPG2RDF.LiteralBasedNodeLabelMapping)) {
            return new NodeLabelMappingToLiteralsImpl();
        }
        else {
            throw new IllegalArgumentException("NodeLabelMapping type (" + nodeLabelMappingResourceType + ") is unexpected!");
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

        if ( edgeLabelMappingResourceType.equals(LPG2RDF.IRIBasedEdgeLabelMapping)
                || (edgeLabelMappingResourceType.equals(LPG2RDF.EdgeLabelMapping) && edgeLabelMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)) ) {
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
            catch (IllegalArgumentException exception){
                throw new IllegalArgumentException("prefixOfIRIs is an invalid URI!");
            }
        }
        else if ( edgeLabelMappingResourceType.equals(LPG2RDF.RegexBasedEdgeLabelMapping)
                || (edgeLabelMappingResourceType.equals(LPG2RDF.EdgeLabelMapping) && edgeLabelMappingResource.hasProperty(LPG2RDF.regex)) ) {
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
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("prefixOfIRIs is an invalid URI!");
            }
        }

        else if ( edgeLabelMappingResourceType.equals(LPG2RDF.SingleEdgeLabelMapping)
                || (edgeLabelMappingResourceType.equals(LPG2RDF.EdgeLabelMapping) && edgeLabelMappingResource.hasProperty(LPG2RDF.label)
                && edgeLabelMappingResource.hasProperty(LPG2RDF.iri))) {
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
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("iri is an invalid URI!");
            }
        }

        else {
            throw new IllegalArgumentException("EdgeLabelMapping type (" + edgeLabelMappingResourceType + ") is unexpected!");
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

        if ( propertyNameMappingResourceType.equals(LPG2RDF.IRIBasedPropertyNameMapping)
                || (propertyNameMappingResourceType.equals(LPG2RDF.PropertyNameMapping) && propertyNameMappingResource.hasProperty(LPG2RDF.prefixOfIRIs)) ) {
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
            catch (IllegalArgumentException exception){
                throw new IllegalArgumentException("prefixOfIRIs is an invalid URI!");
            }
        }
        else {
            throw new IllegalArgumentException("PropertyNameMapping type (" + propertyNameMappingResourceType + ") is unexpected!");
        }
    }
}
