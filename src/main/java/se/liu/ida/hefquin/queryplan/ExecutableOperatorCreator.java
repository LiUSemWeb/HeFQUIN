package se.liu.ida.hefquin.queryplan;

/**
 * Every instance of this type can create {@link ExecutableOperator} objects
 * of a specific type.
 */
public interface ExecutableOperatorCreator<OutElmtType>
{
	/**
	 * Creates and returns an {@link ExecutableOperator} for the
	 * given {@link PhysicalOperator}.
	 * 
	 * This method may work only for specific types of physical
	 * operators. If the given physical operator is not of such
	 * a type, then an {@link IllegalArgumentException} is thrown.
	 */
	ExecutableOperator<OutElmtType> createOp( final PhysicalOperator physicalOp ) throws IllegalArgumentException;
}
