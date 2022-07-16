package se.liu.ida.hefquin.jenaintegration.sparql;

import org.apache.jena.sparql.util.Symbol;

public class HeFQUINConstants {
    public static final String systemVarNS = "http://ida.liu.se/HeFQUIN/system#";

    public static final Symbol sysFederationCatalog       = Symbol.create(systemVarNS+"fedCatalog");
    public static final Symbol sysFederationAccessManager = Symbol.create(systemVarNS+"fedAccessMgr");

    public static final Symbol sysExecServiceForPlanTasks = Symbol.create(systemVarNS+"execServiceForPlanTasks");

    public static final Symbol sysQueryOptimizerFactory   = Symbol.create(systemVarNS+"optimizerFactory");

    public static final Symbol sysIsExperimentRun         = Symbol.create(systemVarNS+"isExperimentRun");
    public static final Symbol sysQueryProcStats          = Symbol.create(systemVarNS+"queryProcStats");
}
