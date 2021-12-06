package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.*;
import org.apache.jena.sparql.resultset.ResultsFormat;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class ModResults extends ModBase {
    protected final ArgDecl resultsFmtDecl = new ArgDecl( ArgDecl.HasValue, "results", "out", "rfmt" );
    protected final ArgDecl resultsOutPath = new ArgDecl( ArgDecl.HasValue, "outPath" );

    protected ResultsFormat resultsFormat  = ResultsFormat.FMT_UNKNOWN;
    protected PrintStream printStream = System.out;

    public ModResults() {
        super();
    }
    @Override
    public void processArgs( final CmdArgModule cmdline ) throws IllegalArgumentException {
        if ( cmdline.contains(resultsFmtDecl) ) {
            final String rFmt = cmdline.getValue(resultsFmtDecl);
            resultsFormat = ResultsFormat.lookup(rFmt);
            if ( resultsFormat == null )
                cmdline.cmdError( "Unrecognized output format: " + rFmt );
        }

        if ( cmdline.contains(resultsOutPath) ) {
            final String path = cmdline.getValue(resultsOutPath);
            try {
                printStream = new PrintStream(path);
            } catch ( FileNotFoundException e ) {
                System.err.println( "Failed to load outPath: "+e.getMessage() );
                throw new TerminationException(1);
            }
        }
    }

    @Override
    public void registerWith( final CmdGeneral cmdLine ) {
        cmdLine.getUsage().startCategory("Results");
        cmdLine.add(resultsFmtDecl,
                "--results=",
                "Results format (Result set: text, XML, JSON, CSV, TSV; Graph: RDF serialization)") ;
        cmdLine.add(resultsOutPath,
                "--outPath=",
                "The path to the file storing output of the query") ;
    }

    public PrintStream getPrintStream(){
        return printStream;
    }

    public ResultsFormat getResultsFormat() {
        return resultsFormat;
    }

    public void setPrintStream( PrintStream printStream ){
        this.printStream = printStream;
    }

}