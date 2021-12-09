package se.liu.ida.hefquin.cli;

import org.apache.jena.cmd.TerminationException;
import org.apache.jena.query.Query;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.sparql.resultset.ResultsFormat;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsOut;
import arq.cmdline.ModTime;
import se.liu.ida.hefquin.cli.modules.ModEngineConfig;
import se.liu.ida.hefquin.cli.modules.ModFederation;
import se.liu.ida.hefquin.cli.modules.ModQuery;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineBuilder;

public class RunQueryWithoutSrcSel extends CmdARQ
{
	protected final ModTime          modTime =          new ModTime();
	protected final ModQuery         modQuery =         new ModQuery();
	protected final ModFederation    modFederation =    new ModFederation();
    protected final ModResultsOut    modResults =       new ModResultsOut();
	protected final ModEngineConfig  modEngineConfig =  new ModEngineConfig();

    public static void main( final String... argv )
    {
        new RunQueryWithoutSrcSel(argv).mainRun();
    }

    public RunQueryWithoutSrcSel( final String[] argv )
    {
    	super(argv);

        addModule(modTime);
        addModule(modQuery);
        addModule(modFederation);
        addModule(modResults);
        addModule(modEngineConfig);
    }

	@Override
	protected String getSummary() {
		return getCommandName()+" --query=<query>";
	}

	@Override
	protected void exec() {
		final HeFQUINEngine e = new HeFQUINEngineBuilder()
				.setConfiguration( modEngineConfig.getConfig() )
				.setFederationCatalog( modFederation.getFederationCatalog() )
				.build();

		final Query query = getQuery();
		final ResultsFormat resFmt = modResults.getResultsFormat();

		modTime.startTimer();

		try {
			e.executeQuery(query, resFmt);
		}
		catch ( final Exception ex ) {
			System.out.flush();
			System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
		}

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.err.println("Time: " + modTime.timeStr(time) + " sec");
		}
	}

    protected Query getQuery() {
        try {
            return modQuery.getQuery();
        } catch ( final NotFoundException ex ) {
            System.err.println( "Failed to load query: "+ex.getMessage() );
            throw new TerminationException(1);
        }
    }

}
