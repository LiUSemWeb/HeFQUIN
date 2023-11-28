package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.LPG2RDFConfigurationReader;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.impl.DefaultLPG2RDFConfigurationImpl;

public class ModLPG2RDFConfiguration extends ModBase {
    protected final ArgDecl lpg2RDFDescrDecl   = new ArgDecl(ArgDecl.HasValue, "lpg2rdfconf", "lpg2rdf", "LPG2RDFConfigurationDescription");

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
            cat = new DefaultLPG2RDFConfigurationImpl();
        }
    }


    public LPG2RDFConfiguration getLPG2RDFConfigurationCatalog() {
        return cat;
    }

}
