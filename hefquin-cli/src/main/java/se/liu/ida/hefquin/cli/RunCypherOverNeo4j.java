package se.liu.ida.hefquin.cli;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModTime;
import org.apache.jena.cmd.ArgDecl;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.Neo4jInterface;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.RecordsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.Neo4jRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunCypherOverNeo4j extends CmdARQ {
    protected final ModTime modTime =          new ModTime();

    protected final ArgDecl argNeo4jUri   = new ArgDecl(ArgDecl.HasValue, "neo4juri");
    protected final ArgDecl argCypherQuery   = new ArgDecl(ArgDecl.HasValue, "query");

    protected RunCypherOverNeo4j(String[] argv) {
        super(argv);

        addModule(modTime);

        add(argNeo4jUri, "--neo4juri", "The URI of the Neo4j endpoint");
        add(argCypherQuery, "--query", "The path to a file containing a Cypher query");

    }

    public static void main(String[] args) {
        new RunCypherOverNeo4j(args).mainRun();
    }

    @Override
    protected String getSummary() {
        return getCommandName()+"--query=<query> --neo4juri=<Neo4j endpoint URI> --time? --naive?\"";
    }

    @Override
    protected void exec() {
        final String uri = getArg(argNeo4jUri).getValue();
        final String filepath = getArg(argCypherQuery).getValue();
        final String cypher;
        try {
            cypher = Files.readString(Paths.get(filepath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        //Query Execution
        modTime.startTimer();
        final Neo4jRequest request = new Neo4jRequestImpl(cypher);

        final RecordsResponse response;
        try {
            response = processor.performRequest(request, server);
        } catch (final Exception ex) {
            System.out.flush();
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            return;
        }
        if (modTime.timingEnabled()) {
            final long time = modTime.endTimer();
            System.out.println("Query Execution Time: " + modTime.timeStr(time) + " sec");
        }
        System.out.println("Results:" + response.getResponse().size());
    }
}
