package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

import se.liu.ida.hefquin.engine.HeFQUINEngineConfig;

public class ModEngineConfig extends ModBase
{
	protected final ArgDecl ignorePhysicalOpsForLogicalAddOpsDecl = new ArgDecl(ArgDecl.NoValue, "ignorePhysicalOpsForLogicalAddOps");
	protected final ArgDecl ignoreParallelMultiLeftJoinDecl       = new ArgDecl(ArgDecl.NoValue, "ignoreParallelMultiLeftJoin");

	protected HeFQUINEngineConfig config = null;


	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("HeFQUIN engine configuration") ;

		cmdLine.add(ignorePhysicalOpsForLogicalAddOpsDecl, "--ignorePhysicalOpsForLogicalAddOps",
		            "When converting logical to physical plan, do not try to use physical operators for tpAdd");

		cmdLine.add(ignoreParallelMultiLeftJoinDecl, "--ignoreParallelMultiLeftJoin",
		            "When converting logical to physical plan, do not try to use the parallel multi-left-join operator");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		config = new HeFQUINEngineConfig( cmdLine.contains(ignorePhysicalOpsForLogicalAddOpsDecl),
		                                  cmdLine.contains(ignoreParallelMultiLeftJoinDecl) );
	}

	public HeFQUINEngineConfig getConfig() {
		return config;
	}

}
