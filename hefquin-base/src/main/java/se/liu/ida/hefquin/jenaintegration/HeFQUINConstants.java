package se.liu.ida.hefquin.jenaintegration;

import org.apache.jena.sparql.util.Symbol;

public class HeFQUINConstants
{
	public static final String systemVarNS = "http://ida.liu.se/HeFQUIN/system#";

	public static final Symbol sysExecuteWithJena         = Symbol.create(systemVarNS+"executeWithJena");

	public static final String DEFAULT_FED_DESCR_COUNT_STRING = "1";

	public static final Integer DEFAULT_FED_DESCR_COUNT_INT = 1;
}
