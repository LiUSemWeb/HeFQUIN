package se.liu.ida.hefquin.engine.queryplan.logical;

public class LogicalPlanCounter {
	private static int counter = 1;  
    protected int operatorID;  

    public LogicalPlanCounter() {
        this.operatorID = counter++;  
    }

    public int getOperatorID() {
        return operatorID;
    }
}
