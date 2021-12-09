package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

import se.liu.ida.hefquin.engine.HeFQUINEngineConfig;

public class ModEngineConfig extends ModBase
{
	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
        // nothing to do at the moment
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		// nothing to do at the moment
	}

	public HeFQUINEngineConfig getConfig() {
		return new HeFQUINEngineConfig();
	}

}
