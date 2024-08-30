package se.liu.ida.hefquin.cli;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModTime;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conn.Neo4jConnectionFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conn.Neo4jConnectionFactory.Neo4jConnection;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;

import org.apache.jena.cmd.ArgDecl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

        //Query Execution
        modTime.startTimer();

        final Neo4jConnection conn = Neo4jConnectionFactory.connect(uri);

		final List<TableRecord> result;
		try {
			result = conn.execute(cypher);
		}
		catch (final Exception ex) {
            System.out.flush();
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
            return;
        }
        if (modTime.timingEnabled()) {
            final long time = modTime.endTimer();
            System.out.println("Query Execution Time: " + modTime.timeStr(time) + " sec");
        }
        System.out.println("Result size:" + result.size());
    }
}
