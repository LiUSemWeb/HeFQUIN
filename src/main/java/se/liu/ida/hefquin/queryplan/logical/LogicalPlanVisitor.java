package se.liu.ida.hefquin.queryplan.logical;

public interface LogicalPlanVisitor
{
	void visit( final LogicalOpRequest<?> op );

	void visit( final LogicalOpTPAdd op );
	void visit( final LogicalOpBGPAdd op );

	void visit( final LogicalOpJoin op );
	void visit( final LogicalOpUnion op );

	void visit( final LogicalOpMultiwayJoin op );
	void visit( final LogicalOpMultiwayUnion op );
}
