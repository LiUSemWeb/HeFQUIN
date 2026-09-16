package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

public class ModCaching extends ModBase
{
	protected final ArgDecl argIgnoreCache  = new ArgDecl(ArgDecl.NoValue, "ignoreRetrievalCache");
	protected final ArgDecl argIgnoreCardinalityCache    = new ArgDecl(ArgDecl.NoValue, "ignoreCardinalityCache");

	protected boolean ignoreRetrievalCache;
	protected boolean ignoreCardinalityCache;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Caching");
		cmdLine.add( argIgnoreCache,
		             "--ignoreRetrievalCache", "Ignore the cache when issuing data retrieval requests");
		cmdLine.add( argIgnoreCardinalityCache,
		             "--ignoreCardinalityCache", "Ignore the cache when issuing cardinality requests");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		ignoreRetrievalCache = cmdLine.contains(argIgnoreCache);
		ignoreCardinalityCache = cmdLine.contains(argIgnoreCardinalityCache);
	}

	public boolean isIgnoreRetrievalCache() {
		return ignoreRetrievalCache;
	}

	public boolean isIgnoreCardinalityCache() {
		return ignoreCardinalityCache;
	}
}
