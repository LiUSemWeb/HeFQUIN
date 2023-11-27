package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.LPG2RDFConfigurationReader;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.EdgeLabelMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.EdgeLabelMappingImpl_AllToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.LPG2RDFConfigurationImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeLabelMappingImpl_AllToLiterals;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.NodeMappingImpl_AllToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.PropertyNameMappingImpl_AllToURIs;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.*;

public class ModLPG2RDFConfiguration extends ModBase {
    protected final ArgDecl lpg2RDFDescrDecl   = new ArgDecl(ArgDecl.HasValue, "lpg2rdfconf", "lpg2rdf", "LPG2RDFConfigurationDescription");
    protected static final String NSNODE = "https://example.org/node/";
    protected static final String NSRELATIONSHIP = "https://example.org/relationship/";
    protected static final String NSPROPERTY = "https://example.org/property/";
    protected static final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";


    protected LPG2RDFConfiguration cat = null;

    @Override
    public void registerWith( final CmdGeneral cmdLine ) {
        cmdLine.getUsage().startCategory("LPG to RDF Mapping");
        cmdLine.add(lpg2RDFDescrDecl,  "--lpg2rdfconf",  "file with an RDF description of the LPG-to-RDF configuration");
    }

    @Override
    public void processArgs( final CmdArgModule cmdLine ) {
        if ( cmdLine.contains(lpg2RDFDescrDecl) ) {
            final String lpg2RDFDescrFilename = cmdLine.getValue(lpg2RDFDescrDecl);
            cat = LPG2RDFConfigurationReader.readFromFile(lpg2RDFDescrFilename);
        }
        else{
            final NodeMapping nodeMapping = new NodeMappingImpl_AllToURIs(NSNODE);
            final NodeLabelMapping nodeLabelMapping = new NodeLabelMappingImpl_AllToLiterals();
            final EdgeLabelMapping edgeLabelMapping = new EdgeLabelMappingImpl_AllToURIs(NSRELATIONSHIP);
            final PropertyNameMapping propertyNameMapping = new PropertyNameMappingImpl_AllToURIs(NSPROPERTY);
            cat = new LPG2RDFConfigurationImpl(NodeFactory.createURI(LABEL), nodeMapping, nodeLabelMapping,edgeLabelMapping,propertyNameMapping);
        }
    }


    public LPG2RDFConfiguration getLPG2RDFConfigurationCatalog() {
        return cat;
    }

}
