package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.LPG2RDFConfigurationReader;

public class ModLPG2RDFConfiguration extends ModBase {
    protected final ArgDecl lpg2RDFDescrDecl   = new ArgDecl(ArgDecl.HasValue, "LPG2RDFConfigurationDescription", "lpg2rdf");

    protected LPG2RDFConfiguration cat = null;

    @Override
    public void registerWith( final CmdGeneral cmdLine ) {
        cmdLine.getUsage().startCategory("LPG2RDFConfigurationDescription");
        cmdLine.add(lpg2RDFDescrDecl,  "--LPG2RDFConfigurationDescription",  "file with an RDF description of the LPG2RDFConfiguration");
    }

    @Override
    public void processArgs( final CmdArgModule cmdLine ) {
        if ( cmdLine.contains(lpg2RDFDescrDecl) ) {
            final String lpg2RDFDescrFilename = cmdLine.getValue(lpg2RDFDescrDecl);
            cat = LPG2RDFConfigurationReader.readFromFile(lpg2RDFDescrFilename);
        }
    }


    public LPG2RDFConfiguration getLPG2RDFConfigurationCatalog() {
        return cat;
    }

}
