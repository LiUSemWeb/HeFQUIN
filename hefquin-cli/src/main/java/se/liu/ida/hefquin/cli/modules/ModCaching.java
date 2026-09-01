package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

public class ModCaching extends ModBase
{
	protected final ArgDecl argIgnoreCache  = new ArgDecl(ArgDecl.NoValue, "ignoreCache");
	protected final ArgDecl argIgnoreCardinalityCache    = new ArgDecl(ArgDecl.NoValue, "ignoreCardinalityCache");

	protected boolean ignoreCache;
	protected boolean ignoreCardinalityCache;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Caching");
		cmdLine.add( argIgnoreCache,
		             "--ignoreCache", "Ignore the cache when issuing data retrieval requests");
		cmdLine.add( argIgnoreCardinalityCache,
		             "--ignoreCardinalityCache", "Ignore the cache when issuing cardinality requests");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		ignoreCache = cmdLine.contains(argIgnoreCache);
		ignoreCardinalityCache = cmdLine.contains(argIgnoreCardinalityCache);
	}

	public boolean isIgnoreCache() {
		return ignoreCache;
	}

	public boolean isIgnoreCardinalityCache() {
		return ignoreCardinalityCache;
	}
}
