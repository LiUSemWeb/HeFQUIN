package se.liu.ida.hefquin.cli;

import org.apache.jena.cmd.TerminationException;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.QueryExecUtils;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsOut;
import arq.cmdline.ModTime;
import se.liu.ida.hefquin.cli.modules.ModFederation;
import se.liu.ida.hefquin.cli.modules.ModQuery;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.AsyncFederationAccessManagerImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.*;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;
import se.liu.ida.hefquin.jenaintegration.sparql.engine.main.OpExecutorHeFQUIN;

public class RunQueryWithoutSrcSel extends CmdARQ
{
	protected final ModTime        modTime =        new ModTime();
	protected final ModQuery       modQuery =       new ModQuery();
	protected final ModFederation  modFederation =  new ModFederation();
    protected final ModResultsOut  modResults =     new ModResultsOut();

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
    }

	@Override
	protected String getSummary() {
		return getCommandName()+" --query=<query>";
	}

	@Override
	protected void exec() {
		final Context ctxt = ARQ.getContext();
		QC.setFactory( ctxt, OpExecutorHeFQUIN.factory );
		ctxt.set( HeFQUINConstants.sysFederationCatalog, modFederation.getFederationCatalog() );
		ctxt.set( HeFQUINConstants.sysFederationAccessManager, createFedAccessMgr() );

		final Query query = getQuery();
		final ResultsFormat resFmt = modResults.getResultsFormat();

		modTime.startTimer();

		try {
			final QueryExecution qe = QueryExecutionFactory.create( query, DatasetGraphFactory.createGeneral() );
			QueryExecUtils.executeQuery(query, qe, resFmt);
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

    protected FederationAccessManager createFedAccessMgr() {
		final SPARQLRequestProcessor reqProcSPARQL = new SPARQLRequestProcessorImpl();
		// TODO: replace the following once we have an implementation of TPFRequestProcessor
		final TPFRequestProcessor reqProcTPF = new TPFRequestProcessor() {
			@Override public TPFResponse performRequest(TPFRequest req, TPFServer fm) { throw new UnsupportedOperationException(); }
			@Override public TPFResponse performRequest(TPFRequest req, BRTPFServer fm) { throw new UnsupportedOperationException(); }
		};
		// TODO: replace the following once we have an implementation of BRTPFRequestProcessor
		final BRTPFRequestProcessor reqProcBRTPF = new BRTPFRequestProcessor() {
			@Override public TPFResponse performRequest(BRTPFRequest req, BRTPFServer fm) { throw new UnsupportedOperationException(); }
		};

		final Neo4jRequestProcessor reqProcNeo4j = new Neo4jRequestProcessorImpl();

		return new AsyncFederationAccessManagerImpl(reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);
    }

}
