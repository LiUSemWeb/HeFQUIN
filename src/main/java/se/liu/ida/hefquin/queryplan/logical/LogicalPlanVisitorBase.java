package se.liu.ida.hefquin.queryplan.logical;

public class LogicalPlanVisitorBase implements LogicalPlanVisitor
{
	public void visit( final LogicalOpRequest<?> op )        {}

	public void visit( final LogicalOpTPAdd op )             {}

	public void visit( final LogicalOpBGPAdd op )            {}

	public void visit( final LogicalOpJoin op )              {}

	public void visit( final LogicalOpUnion op )             {}

	public void visit( final LogicalOpMultiwayJoin op )      {}

	public void visit( final LogicalOpMultiwayUnion op )     {}
}
