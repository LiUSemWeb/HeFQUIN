package se.liu.ida.hefquin.engine.queryproc;

public interface SourcePlannerFactory
{
	SourcePlanner createSourcePlanner( QueryProcContext ctxt );
}
