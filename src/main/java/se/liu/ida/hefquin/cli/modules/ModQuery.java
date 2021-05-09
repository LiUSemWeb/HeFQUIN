package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.query.Syntax;

import arq.cmdline.ModQueryIn;
import jena.cmd.CmdGeneral;

public class ModQuery extends ModQueryIn
{
    public ModQuery() {
        super( Syntax.syntaxSPARQL_11 );
    }

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
        cmdLine.getUsage().startCategory("Query") ;
        cmdLine.add(queryFileDecl,   "--query, --file",  "File containing a query");
        cmdLine.add(queryBaseDecl,   "--base",   "Base URI for the query");
	}

}
