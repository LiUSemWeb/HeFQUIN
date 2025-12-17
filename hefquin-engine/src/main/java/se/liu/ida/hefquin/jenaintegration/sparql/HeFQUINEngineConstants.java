package se.liu.ida.hefquin.jenaintegration.sparql;

import org.apache.jena.sparql.util.Symbol;

import se.liu.ida.hefquin.jenaintegration.HeFQUINConstants;

public class HeFQUINEngineConstants extends HeFQUINConstants
{
	public static final Symbol sysEngine                  = Symbol.create(systemVarNS+"engine");

	public static final Symbol sysQProcStatsAndExceptions = Symbol.create(systemVarNS+"queryProcStatsAndExceptions");
}
