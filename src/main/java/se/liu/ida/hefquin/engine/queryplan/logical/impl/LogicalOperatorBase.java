package se.liu.ida.hefquin.engine.queryplan.logical.impl;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;

public abstract class LogicalOperatorBase implements LogicalOperator {
	private static int counter = 0;  
    protected final int id;  

    public LogicalOperatorBase() {
        this.id = counter++;  
    }
    
    @Override
    public int getID() {
        return id;
    }
}
