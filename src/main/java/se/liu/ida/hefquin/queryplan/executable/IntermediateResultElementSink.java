package se.liu.ida.hefquin.queryplan.executable;

public interface IntermediateResultElementSink<ElmtType>
{
	void send( final ElmtType element );
}
