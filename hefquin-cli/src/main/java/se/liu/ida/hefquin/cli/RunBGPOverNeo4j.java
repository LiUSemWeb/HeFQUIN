package se.liu.ida.hefquin.cli;

import java.util.*;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsOut;
import arq.cmdline.ModTime;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.util.QueryExecUtils;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.utils.JenaResultSetUtils;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.cli.modules.ModLPG2RDFConfiguration;
import se.liu.ida.hefquin.cli.modules.ModQuery;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.Neo4jInterface;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.RecordsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.Neo4jRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Record2SolutionMappingTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.SPARQLStar2CypherTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.Record2SolutionMappingTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.SPARQLStar2CypherTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherUnionQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryBuilder;

public class RunBGPOverNeo4j extends CmdARQ
{
	protected final ModTime modTime =            new ModTime();
	protected final ModQuery modQuery =          new ModQuery();
	protected final ModResultsOut modResults =   new ModResultsOut();
	protected final ModLPG2RDFConfiguration modLPG2RDFConfiguration = new ModLPG2RDFConfiguration();

	protected final ArgDecl argEndpointURI   = new ArgDecl(ArgDecl.HasValue, "endpoint");
	protected final ArgDecl argNaive   = new ArgDecl(ArgDecl.NoValue, "naive");
	protected final ArgDecl argNoVarRepl   = new ArgDecl(ArgDecl.NoValue, "disableVariableReplacement");
	protected final ArgDecl argNoMerge   = new ArgDecl(ArgDecl.NoValue, "disablePathMerging");
	protected final ArgDecl argSuppressResultPrintout = new ArgDecl(ArgDecl.NoValue, "suppressResultPrintout");

	public static void main( final String[] args ) {
		new RunBGPOverNeo4j(args).mainRun();
	}

	protected RunBGPOverNeo4j( final String[] argv ) {
		super(argv);

		addModule(modTime);
		addModule(modResults);

		add(argSuppressResultPrintout, "--suppressResultPrintout", "Do not print out the query result");

		addModule(modQuery);
		addModule(modLPG2RDFConfiguration);

		add(argEndpointURI, "--endpoint", "The URI of the Neo4j endpoint");
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
		final BGP bgp = getBGP();

		final LPG2RDFConfiguration conf = modLPG2RDFConfiguration.getLPG2RDFConfiguration();

		final Pair<CypherQuery, Map<CypherVar,Var>> tRes = performQueryTranslation(bgp, conf);

		final RecordsResponse response = performQueryExecution( tRes.object1 );

		final List<SolutionMapping> result = performResultTranslation( response,
		                                                               conf,
		                                                               tRes.object1,
		                                                               tRes.object2);

		if ( ! contains(argSuppressResultPrintout) ) {
			printResult(result);
		}
	}


	protected BGP getBGP() {
		final Element groupPattern = modQuery.getQuery().getQueryPattern();
		if (! (groupPattern instanceof ElementGroup) ) {
			throw new IllegalArgumentException("Neo4j translation only supports BGPs");
		}

		final Element pattern = ( (ElementGroup) groupPattern ).get(0);
		if (! (pattern instanceof ElementPathBlock) ) {
			throw new IllegalArgumentException("Neo4j translation only supports BGPs");
		}

		return QueryPatternUtils.createBGP( (ElementPathBlock) pattern );
	}

	protected Pair<CypherQuery, Map<CypherVar,Var>> performQueryTranslation( final BGP bgp,
																			 final LPG2RDFConfiguration conf ) {
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}

		final Pair<CypherQuery, Map<CypherVar,Var>> tRes = translateToCypher(bgp, conf);

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Query Translation Time: " + modTime.timeStr(time) + " sec");
			System.out.println(tRes.object1);
			System.exit(1);
		}

		return tRes;
	}

	protected Pair<CypherQuery, Map<CypherVar,Var>> translateToCypher( final BGP bgp,
																	   final LPG2RDFConfiguration conf ) {
		final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
		final Pair<CypherQuery, Map<CypherVar,Var>> tRes = translator.translateBGP( bgp,
				conf,
				hasArg(argNaive) );

		final CypherQuery query;
		if ( hasArg(argNoVarRepl) ) {
			query = tRes.object1;
		} else {
			if ( tRes.object1 instanceof CypherMatchQuery )
				query = translator.rewriteJoins((CypherMatchQuery)tRes.object1);
			else if ( tRes.object1 instanceof CypherUnionQuery )
				query = translator.rewriteJoins((CypherUnionQuery) tRes.object1);
			else
				throw new IllegalArgumentException( tRes.object1.getClass().getName() );
		}

		if ( hasArg(argNoMerge) ) {
			if ( query == tRes.object1 )
				return tRes;
			else
				return new Pair<>(query, tRes.object2);
		}

		if ( query instanceof CypherMatchQuery ) {
			final CypherMatchQuery mQuery = (CypherMatchQuery) query;
			final List<MatchClause> merged = translator.mergePaths( mQuery.getMatches() );

			return new Pair<>( new CypherQueryBuilder()
					.addAll( merged )
					.addAll( mQuery.getConditions() )
					.addAll( mQuery.getIterators() )
					.addAll( mQuery.getReturnExprs() )
					.build(),
					tRes.object2 );
		}

		if ( query instanceof CypherUnionQuery ) {
			final CypherUnionQuery uQuery = (CypherUnionQuery) query;

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

			return new Pair<>( new CypherUnionQueryImpl(subqueries),
					tRes.object2 );
		}

		throw new IllegalArgumentException( query.getClass().getName() );
	}

	protected RecordsResponse performQueryExecution( final CypherQuery query ) {
		final Neo4jRequest request = new Neo4jRequestImpl( query.toString() );

		final String uri = getArg(argEndpointURI).getValue();
		final Neo4jInterface iface = new Neo4jInterfaceImpl(uri);
		final Neo4jServer server = new Neo4jServer() {
			@Override public Neo4jInterface getInterface() { return iface; }

			@Override public VocabularyMapping getVocabularyMapping() { return null; }
		};

		final Neo4jRequestProcessor processor = new Neo4jRequestProcessorImpl();

		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}

		final RecordsResponse response;
		try {
			response = processor.performRequest(request, server);
		} catch ( final Exception ex ) {
			System.out.flush();
			System.err.println( ex.getMessage() );
			ex.printStackTrace( System.err );
			throw new IllegalStateException(ex);
		}

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Query Execution Time: " + modTime.timeStr(time) + " sec");
		}

		return response;
	}

	protected List<SolutionMapping> performResultTranslation( final RecordsResponse response,
															  final LPG2RDFConfiguration conf,
															  final CypherQuery query,
															  final Map<CypherVar,Var> varMap ) {
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}
		final Record2SolutionMappingTranslator translator = new Record2SolutionMappingTranslatorImpl();
		final List<SolutionMapping> result = translator.translateRecords( response.getResponse(),
				conf,
				query,
				varMap );

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Result Translation Time: " + modTime.timeStr(time) + " sec");
		}

		return result;
	}

	protected void printResult( final List<SolutionMapping> result ) {
		final Query q = modQuery.getQuery();
		final ResultSet rs = JenaResultSetUtils.convertToJenaResultSet( result, q.getResultVars() );
		QueryExecUtils.outputResultSet( rs,
				q.getPrologue(),
				modResults.getResultsFormat(),
				System.out );
	}

}
