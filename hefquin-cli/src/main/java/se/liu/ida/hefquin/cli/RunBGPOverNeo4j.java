package se.liu.ida.hefquin.cli;

import java.util.*;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsOut;
import arq.cmdline.ModTime;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.engine.ResultSetStream;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.util.QueryExecUtils;

import se.liu.ida.hefquin.cli.modules.ModLPG2RDFConfiguration;
import se.liu.ida.hefquin.cli.modules.ModQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.Record2SolutionMappingTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpg.SPARQL2CypherTranslationResult;
import se.liu.ida.hefquin.engine.wrappers.lpg.SPARQLStar2CypherTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpg.conn.Neo4jConnectionFactory;
import se.liu.ida.hefquin.engine.wrappers.lpg.conn.Neo4jConnectionFactory.Neo4jConnection;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpg.impl.Record2SolutionMappingTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.impl.SPARQL2CypherTranslationResultImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.impl.SPARQLStar2CypherTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherUnionQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherQueryBuilder;

public class RunBGPOverNeo4j extends CmdARQ
{
	protected final ModTime modTime =            new ModTime();
	protected final ModQuery modQuery =          new ModQuery();
	protected final ModResultsOut modResults =   new ModResultsOut();
	protected final ModLPG2RDFConfiguration modLPG2RDFConfiguration = new ModLPG2RDFConfiguration();

	protected final ArgDecl argEndpointURI   = new ArgDecl(ArgDecl.HasValue, "endpoint");
	protected final ArgDecl argEndpointUsername   = new ArgDecl(ArgDecl.HasValue, "username");
	protected final ArgDecl argEndpointPassword   = new ArgDecl(ArgDecl.HasValue, "password");
	protected final ArgDecl argNaive   = new ArgDecl(ArgDecl.NoValue, "naive");
	protected final ArgDecl argNoVarRepl   = new ArgDecl(ArgDecl.NoValue, "disableVariableReplacement");
	protected final ArgDecl argNoMerge   = new ArgDecl(ArgDecl.NoValue, "disablePathMerging");
	protected final ArgDecl argPrintCypher = new ArgDecl(ArgDecl.NoValue, "printCypherQuery");
	protected final ArgDecl argSuppressResultPrintout = new ArgDecl(ArgDecl.NoValue, "suppressResultPrintout");
	protected final ArgDecl argSkipExecution = new ArgDecl(ArgDecl.NoValue, "skipExecution");
	
	public static void main( final String[] args ) {
		new RunBGPOverNeo4j(args).mainRun();
	}

	protected RunBGPOverNeo4j( final String[] argv ) {
		super(argv);

		addModule(modTime);
		addModule(modResults);

		add(argPrintCypher, "--printCypherQuery", "Print out the Cypher query(ies) produced by the query translation process");
		add(argSuppressResultPrintout, "--suppressResultPrintout", "Do not print out the query result");
		add(argSkipExecution, "--skipExecution", "Do not execute the query (but create the execution plan)");

		addModule(modQuery);
		addModule(modLPG2RDFConfiguration);

		add(argEndpointURI, "--endpoint", "The URI of the Neo4j endpoint");
		add(argEndpointUsername, "--username", "Username for the Neo4j endpoint");
		add(argEndpointPassword, "--password", "Password for the Neo4j endpoint");
		add(argNaive, "--naive", "If you want naive translation");
		add(argNoVarRepl, "--disableVariableReplacement", "If you want to disable variable replacement");
		add(argNoMerge, "--disablePathMerging", "If you want to disable path merging");
	}

	@Override
	protected String getSummary() {
		return getCommandName() + " --query=<query file> --endpoint=<Neo4j endpoint URI> --time? --naive? --disableVariableReplacement? --disablePathMerging?";
	}

	@Override
	protected void exec() {
		final Set<Triple> bgp = getBGP();

		final LPG2RDFConfiguration conf = modLPG2RDFConfiguration.getLPG2RDFConfiguration();

		final SPARQL2CypherTranslationResult tRes = performQueryTranslation(bgp, conf);

		if ( contains(argPrintCypher) ) {
			System.out.println( tRes.getCypherQuery().toString() );
		}

		if ( contains(argSkipExecution) ) {
			return;
		}

		final List<TableRecord> response = performQueryExecution( tRes.getCypherQuery() );

		final List<Binding> result = performResultTranslation(response, conf, tRes);

		if ( ! contains(argSuppressResultPrintout) ) {
			printResult(result);
		}
	}


	protected Set<Triple> getBGP() {
		final Element groupPattern = modQuery.getQuery().getQueryPattern();
		if (! (groupPattern instanceof ElementGroup) ) {
			throw new IllegalArgumentException("Neo4j translation only supports BGPs");
		}

		final Element pattern = ( (ElementGroup) groupPattern ).get(0);
		if (! (pattern instanceof ElementPathBlock) ) {
			throw new IllegalArgumentException("Neo4j translation only supports BGPs");
		}

		final Set<Triple> bgp = new HashSet<>();
		final Iterator<TriplePath> it = ( (ElementPathBlock) pattern ).getPattern().iterator();
		while ( it.hasNext() ) {
			final TriplePath tp = it.next();
			if ( ! tp.isTriple() ) {
				throw new IllegalArgumentException( "the given PathBlock contains a property path pattern (" + tp.toString() + ")" );
			}

			bgp.add( tp.asTriple() );
		}

		return bgp;
	}

	protected SPARQL2CypherTranslationResult performQueryTranslation( final Set<Triple> bgp,
	                                                                  final LPG2RDFConfiguration conf ) {
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}

		final SPARQL2CypherTranslationResult tRes = translateToCypher(bgp, conf);

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println( "Query Translation Time: " + modTime.timeStr(time) + " sec" );
			System.out.println( tRes.getCypherQuery().toString() );
			System.exit(1);
		}

		return tRes;
	}

	protected SPARQL2CypherTranslationResult translateToCypher( final Set<Triple> bgp,
	                                                            final LPG2RDFConfiguration conf ) {
		final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
		final SPARQL2CypherTranslationResult tRes = translator.translateBGP( bgp,
		                                                                     conf,
		                                                                     hasArg(argNaive) );

		final CypherQuery tResQuery = tRes.getCypherQuery();
		final CypherQuery resultQuery;
		if ( hasArg(argNoVarRepl) ) {
			resultQuery = tResQuery;
		}
		else {
			if ( tResQuery instanceof CypherMatchQuery )
				resultQuery = translator.rewriteJoins( (CypherMatchQuery) tResQuery );
			else if ( tResQuery instanceof CypherUnionQuery )
				resultQuery = translator.rewriteJoins( (CypherUnionQuery) tResQuery );
			else
				throw new IllegalArgumentException( tResQuery.getClass().getName() );
		}

		if ( hasArg(argNoMerge) ) {
			if ( resultQuery == tResQuery )
				return tRes;
			else
				return new SPARQL2CypherTranslationResultImpl( resultQuery,
				                                               tRes.getVariablesMapping() );
		}

		if ( resultQuery instanceof CypherMatchQuery ) {
			final CypherMatchQuery mQuery = (CypherMatchQuery) resultQuery;
			final List<MatchClause> merged = translator.mergePaths( mQuery.getMatches() );

			return new SPARQL2CypherTranslationResultImpl( new CypherQueryBuilder().addAll( merged )
			                                                                       .addAll( mQuery.getConditions() )
			                                                                       .addAll( mQuery.getIterators() )
			                                                                       .addAll( mQuery.getReturnExprs() )
			                                                                       .build(),
			                                               tRes.getVariablesMapping() );
		}

		if ( resultQuery instanceof CypherUnionQuery ) {
			final CypherUnionQuery uQuery = (CypherUnionQuery) resultQuery;

			final List<CypherMatchQuery> subqueries = new ArrayList<>();
			for ( final CypherMatchQuery q : uQuery.getSubqueries() ) {
				final List<MatchClause> merged = translator.mergePaths( q.getMatches() );
				final CypherMatchQuery newSubQ = new CypherQueryBuilder()
						.addAll( merged )
						.addAll( q.getConditions() )
						.addAll( q.getIterators() )
						.addAll( q.getReturnExprs() )
						.build();
				subqueries.add(newSubQ);
			}

			return new SPARQL2CypherTranslationResultImpl( new CypherUnionQueryImpl(subqueries),
			                                               tRes.getVariablesMapping() );
		}

		throw new IllegalArgumentException( resultQuery.getClass().getName() );
	}

	protected List<TableRecord> performQueryExecution( final CypherQuery query ) {
		final String uri = getArg(argEndpointURI).getValue();
		final String neo4jUsername = contains(argEndpointUsername) ?
		                             getArg(argEndpointUsername).getValue() : null;
		final String neo4jPassword = contains(argEndpointPassword) ?
		                             getArg(argEndpointPassword).getValue() : null;
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}

		final Neo4jConnection conn = Neo4jConnectionFactory.connect( uri, neo4jUsername, neo4jPassword );

		final List<TableRecord> result;
		try {
			result = conn.execute(query);
		}
		catch ( final Exception ex ) {
			System.out.flush();
			System.err.println( ex.getMessage() );
			ex.printStackTrace( System.err );
			throw new IllegalStateException(ex);
		}

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Query Execution Time: " + modTime.timeStr(time) + " sec");
		}

		return result;
	}

	protected List<Binding> performResultTranslation( final List<TableRecord> response,
	                                                          final LPG2RDFConfiguration conf,
	                                                          final SPARQL2CypherTranslationResult tRes ) {
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}
		final Record2SolutionMappingTranslator translator = new Record2SolutionMappingTranslatorImpl();
		final List<Binding> result = translator.translateRecords(response, conf, tRes);

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Result Translation Time: " + modTime.timeStr(time) + " sec");
		}

		return result;
	}

	protected void printResult( final List<Binding> result ) {
		final Query q = modQuery.getQuery();
		final ResultSet rs = ResultSetStream.create( q.getProjectVars(), result.iterator() );
		QueryExecUtils.outputResultSet( rs,
		                                q.getPrologue(),
		                                modResults.getResultsFormat(),
		                                System.out );
	}

}
