package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfigurationReader;
import se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl.DefaultLPG2RDFConfigurationImpl;

public class ModLPG2RDFConfiguration extends ModBase
{
    protected final ArgDecl lpg2rdfConfigDecl     = new ArgDecl( ArgDecl.HasValue, "lpg2rdfconf", "lpg2rdf", "LPG2RDFConfigurationDescription" );
    protected final ArgDecl lpg2rdfConfigURIDecl  = new ArgDecl( ArgDecl.HasValue, "lpg2rdfconfURI", "URIofLPG2RDFConfiguration" );

    protected LPG2RDFConfiguration lpg2rdfConfig = null;

    @Override
    public void registerWith( final CmdGeneral cmdLine ) {
        cmdLine.getUsage().startCategory("LPG to RDF Mapping");
        cmdLine.add(lpg2rdfConfigDecl,  "--lpg2rdfconf",  "File with an RDF description of the LPG-to-RDF configuration (optional)");
        cmdLine.add(lpg2rdfConfigURIDecl,  "--lpg2rdfconfURI",  "URI of the LPG-to-RDF configuration (optional unless the given file describes multiple such configurations)");
    }

    @Override
    public void processArgs( final CmdArgModule cmdLine ) {
        if ( cmdLine.contains(lpg2rdfConfigDecl) ) {
            final String filename = cmdLine.getValue(lpg2rdfConfigDecl);
            final LPG2RDFConfigurationReader reader = new LPG2RDFConfigurationReader();
            if ( cmdLine.contains(lpg2rdfConfigURIDecl) ) {
                final String uri = cmdLine.getValue(lpg2rdfConfigURIDecl);
                lpg2rdfConfig = reader.readFromFile(filename, uri);
            }
            else {
                lpg2rdfConfig = reader.readFromFile(filename);
            }
        }
        else{
            lpg2rdfConfig = new DefaultLPG2RDFConfigurationImpl();
        }
    }

    public LPG2RDFConfiguration getLPG2RDFConfiguration() {
        return lpg2rdfConfig;
    }

}
