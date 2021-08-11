package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class IdentifyPhysicalOperatorOfTwoTPAdd {
    protected final IdentifyPhysicalOperatorOfTPAdd popOfTPAdd;
    protected final IdentifyPhysicalOperatorOfTPAdd popOfSubquery;

    public IdentifyPhysicalOperatorOfTwoTPAdd( final PhysicalPlan pp ) {
        this.popOfTPAdd = new IdentifyPhysicalOperatorOfTPAdd(pp);
        this.popOfSubquery = new IdentifyPhysicalOperatorOfTPAdd(pp.getSubPlan(0));
    }

    public boolean matchTwoTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public boolean matchTwoTPAddBindJoin() {
        if ( popOfTPAdd.matchTPAddBindJoin() && popOfSubquery.matchTPAddBindJoin() ){
            return true;
        }
        return false;
    }

    public boolean matchTwoTPAddBindJoinFILTER() {
        if ( popOfTPAdd.matchTPAddBJFILTER() && popOfSubquery.matchTPAddBJFILTER() ){
            return true;
        }
        return false;
    }

    public boolean matchTwoTPAddBindJoinUNION() {
        if ( popOfTPAdd.matchTPAddBJUNION() && popOfSubquery.matchTPAddBJUNION() ){
            return true;
        }
        return false;
    }

    public boolean matchTwoTPAddBindJoinVALUES() {
        if ( popOfTPAdd.matchTPAddBJVALUES() && popOfSubquery.matchTPAddBJVALUES() ){
            return true;
        }
        return false;
    }

    // Mixed of bind join and (index) nested loop join
    public boolean matchTPAddBindJoinAndTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddBindJoin() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddBJFILTERAndTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddBJFILTER() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddBJUNIONAndTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddBJUNION() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddBJVALUESAndTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddBJVALUES() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddIndexNLJAndTPAddBindJoin() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddBindJoin() ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddIndexNLJAndTPAddBJFILTER() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddBJFILTER() ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddIndexNLJAndTPAddBJUNION() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddBJUNION() ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddIndexNLJAndTPAddBJVALUES() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddBJVALUES() ){
            return true;
        }
        return false;
    }

}
