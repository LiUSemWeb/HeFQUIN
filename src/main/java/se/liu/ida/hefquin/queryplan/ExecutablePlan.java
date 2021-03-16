package se.liu.ida.hefquin.queryplan;

public interface ExecutablePlan
{
	ExecutableOperator<?> getRootOperator();
}
