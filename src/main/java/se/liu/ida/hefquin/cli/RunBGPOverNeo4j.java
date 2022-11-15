package se.liu.ida.hefquin.cli;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsOut;
import arq.cmdline.ModTime;
import org.apache.jena.base.Sys;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import se.liu.ida.hefquin.cli.modules.ModQuery;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.Neo4jInterface;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.RecordsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.Neo4jRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.SPARQLStar2CypherTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.DefaultConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.Record2SolutionMappingTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.SPARQLStar2CypherTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherUnionQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryBuilder;

import java.util.*;

public class RunBGPOverNeo4j extends CmdARQ {

    protected final ModTime modTime =          new ModTime();
    protected final ModQuery modQuery =         new ModQuery();
    protected final ModResultsOut modResults =       new ModResultsOut();

    protected final ArgDecl argNeo4jUri   = new ArgDecl(ArgDecl.HasValue, "neo4juri");
    protected final ArgDecl argNaive   = new ArgDecl(ArgDecl.NoValue, "naive");
    protected final ArgDecl argVarRep   = new ArgDecl(ArgDecl.NoValue, "varrep");
    protected final ArgDecl argMerge   = new ArgDecl(ArgDecl.NoValue, "merge");

    protected RunBGPOverNeo4j(String[] argv) {
        super(argv);

        addModule(modTime);
        addModule(modResults);

        add(argNeo4jUri, "--neo4juri", "The URI of the Neo4j endpoint");
        add(argNaive, "--naive", "If you want naive translation");
        add(argVarRep, "--varrep", "If you want variable replacement");
        add(argMerge, "--merge", "If you want path merging");

        addModule(modQuery);
    }

    public static void main(String[] args) {
        new RunBGPOverNeo4j(args).mainRun();
    }

    @Override
    protected String getSummary() {
        return getCommandName()+"--query=<query> --neo4juri=<Neo4j endpoint URI> --time? --naive? --varrep? --merge?\"";
    }

    @Override
    protected void exec() {
        final String uri = getArg(argNeo4jUri).getValue();
        final Neo4jServer server = new Neo4jServer() {
            @Override
            public Neo4jInterface getInterface() {
                return new Neo4jInterfaceImpl(uri);
            }

            @Override
            public VocabularyMapping getVocabularyMapping() {
                return null;
            }
        };

        final Neo4jRequestProcessor processor = new Neo4jRequestProcessorImpl();


        final Element groupPattern = modQuery.getQuery().getQueryPattern();
        if (! (groupPattern instanceof ElementGroup)) {
            throw new IllegalArgumentException("Neo4j translation only supports BGPs");
        }
        final Element pattern = ((ElementGroup) groupPattern).get(0);
        if (! (pattern instanceof ElementPathBlock)) {
            throw new IllegalArgumentException("Neo4j translation only supports BGPs");
        }

        final Set<TriplePattern> tps = new HashSet<>();
        for (Iterator<TriplePath> it = ((ElementPathBlock) pattern).patternElts(); it.hasNext(); ) {
            final TriplePath tp = it.next();
            tps.add(new TriplePatternImpl(tp.asTriple()));
        }

        final BGP bgp = new BGPImpl(tps);
        final LPG2RDFConfiguration conf = new DefaultConfiguration();

        //Query translation
        modTime.startTimer();
        final SPARQLStar2CypherTranslator translator = new SPARQLStar2CypherTranslatorImpl();
        final Pair<CypherQuery, Map<CypherVar, Var>> translation = translator.translateBGP(bgp, conf, hasArg(argNaive));
        CypherQuery query;

        //optional optimizations
        if (hasArg(argVarRep)) {
            if (translation.object1 instanceof CypherMatchQuery)
                query = translator.rewriteJoins((CypherMatchQuery)translation.object1);
            else if (translation.object1 instanceof CypherUnionQuery) {
                query = translator.rewriteJoins((CypherUnionQuery) translation.object1);
            } else {
                throw new IllegalStateException();
            }
        } else {
            query = translation.object1;
        }

        if (hasArg(argMerge)) {
            if (query instanceof CypherMatchQuery) {
                List<MatchClause> merged = translator.mergePaths(((CypherMatchQuery) query).getMatches());
                query = new CypherQueryBuilder().addAll(merged)
                        .addAll(((CypherMatchQuery) query).getConditions())
                        .addAll(((CypherMatchQuery) query).getIterators())
                        .addAll(((CypherMatchQuery) query).getReturnExprs())
                        .build();
            } else if (query instanceof CypherUnionQuery) {
                List<CypherMatchQuery> subqueries = new ArrayList<>();
                for (final CypherMatchQuery q : ((CypherUnionQuery) query).getSubqueries()) {
                    List<MatchClause> merged = translator.mergePaths(q.getMatches());
                    subqueries.add(new CypherQueryBuilder().addAll(merged)
                            .addAll(q.getConditions())
                            .addAll(q.getIterators())
                            .addAll(q.getReturnExprs())
                            .build());
                }
                query = new CypherUnionQueryImpl(subqueries);
            } else {
                throw new IllegalStateException();
            }
        }

        if ( modTime.timingEnabled() ) {
            final long time = modTime.endTimer();
            System.out.println("Query Translation Time: " + modTime.timeStr(time) + " sec");
            System.out.println(query);
        }

        //Query Execution
        modTime.startTimer();
        final Neo4jRequest request = new Neo4jRequestImpl(query.toString());

        final RecordsResponse response;
        try {
            response = processor.performRequest(request, server);
        }
        catch ( final Exception ex ) {
            System.out.flush();
            System.err.println( ex.getMessage() );
            ex.printStackTrace( System.err );
            return;
        }
        if ( modTime.timingEnabled() ) {
            final long time = modTime.endTimer();
            System.out.println("Query Execution Time: " + modTime.timeStr(time) + " sec");
        }

        //Result parsing
        modTime.startTimer();
        List<SolutionMapping> mappingList = new Record2SolutionMappingTranslatorImpl().translateRecords(response.getResponse(), conf, query, translation.object2);
        if ( modTime.timingEnabled() ) {
            final long time = modTime.endTimer();
            System.out.println("Result Translation Time: " + modTime.timeStr(time) + " sec");
        }
        System.out.println("Results:" + mappingList.size());
    }
}
