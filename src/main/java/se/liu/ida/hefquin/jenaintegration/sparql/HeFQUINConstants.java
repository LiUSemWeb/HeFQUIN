package se.liu.ida.hefquin.jenaintegration.sparql;

import org.apache.jena.sparql.util.Symbol;

public class HeFQUINConstants {
	public static final String systemVarNS = "http://ida.liu.se/HeFQUIN/system#";

	public static final Symbol sysEngine                  = Symbol.create(systemVarNS+"engine");

	public static final Symbol sysQueryProcStats          = Symbol.create(systemVarNS+"queryProcStats");
	public static final Symbol sysQueryProcExceptions     = Symbol.create(systemVarNS+"queryProcExceptions");
}
