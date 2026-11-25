package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;

import se.liu.ida.hefquin.jenaext.query.SyntaxForHeFQUIN;

/**
 * Command-line argument module for specifying query-related arguments.
 */
public class ModQuery extends ModBase
{
	protected final ArgDecl queryFileDecl = new ArgDecl( ArgDecl.HasValue, "query", "file" );
	protected final ArgDecl queryBaseDecl = new ArgDecl( ArgDecl.HasValue, "base" );

	private Syntax querySyntax = SyntaxForHeFQUIN.syntaxSPARQL_12_HeFQUIN;
	private String queryFilename = null;
	private Query query = null;
	private String baseURI = null;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory( "Query" );
		cmdLine.add( queryFileDecl, "--query, --file", "File containing a query" );
		cmdLine.add( queryBaseDecl, "--base", "Base URI for the query" );
	}

	@Override
	public void processArgs( CmdArgModule cmdline ) {
		if ( cmdline.contains( queryBaseDecl ) ) {
			baseURI = cmdline.getValue( queryBaseDecl );
		}

		if ( cmdline.contains( queryFileDecl ) ) {
			queryFilename = cmdline.getValue( queryFileDecl );
		}

		if ( queryFilename == null ) {
			cmdline.cmdError( "No query file", false );
		}
	}

	public Query getQuery() {
		if ( queryFilename == null ) {
			return null;
		}

		if ( query != null ) {
			return query;
		}

		return QueryFactory.read( queryFilename, baseURI, querySyntax );
	}
}
