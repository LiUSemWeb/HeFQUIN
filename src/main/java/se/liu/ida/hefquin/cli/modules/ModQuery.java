package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.query.Syntax;

import arq.cmdline.ModQueryIn;

public class ModQuery extends ModQueryIn
{
    public ModQuery() {
        super( Syntax.syntaxARQ );
    }

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
        cmdLine.getUsage().startCategory("Query") ;
        cmdLine.add(queryFileDecl,   "--query, --file",  "File containing a query");
        cmdLine.add(queryBaseDecl,   "--base",   "Base URI for the query");
	}

}
