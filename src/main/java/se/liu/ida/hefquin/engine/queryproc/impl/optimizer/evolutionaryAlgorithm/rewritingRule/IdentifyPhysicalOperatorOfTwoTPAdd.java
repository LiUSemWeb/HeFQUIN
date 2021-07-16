package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public class IdentifyPhysicalOperatorOfTwoTPAdd {
    protected final IdentifyPhysicalOperatorOfTPAdd popOfTPAdd;
    protected final IdentifyPhysicalOperatorOfTPAdd popOfSubquery;

    public IdentifyPhysicalOperatorOfTwoTPAdd( final PhysicalPlan pp ) {
        this.popOfTPAdd = new IdentifyPhysicalOperatorOfTPAdd(pp);
        this.popOfSubquery = new IdentifyPhysicalOperatorOfTPAdd(pp.getSubPlan(0));
    }

    public Boolean matchTwoTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public Boolean matchTwoTPAddBindJoin() {
        if ( popOfTPAdd.matchTPAddBindJoin() && popOfSubquery.matchTPAddBindJoin() ){
            return true;
        }
        return false;
    }

    public Boolean matchTwoTPAddBindJoinFILTER() {
        if ( popOfTPAdd.matchTPAddBJFILTER() && popOfSubquery.matchTPAddBJFILTER() ){
            return true;
        }
        return false;
    }

    public Boolean matchTwoTPAddBindJoinUNION() {
        if ( popOfTPAdd.matchTPAddBJUNION() && popOfSubquery.matchTPAddBJUNION() ){
            return true;
        }
        return false;
    }

    public Boolean matchTwoTPAddBindJoinVALUES() {
        if ( popOfTPAdd.matchTPAddBJVALUES() && popOfSubquery.matchTPAddBJVALUES() ){
            return true;
        }
        return false;
    }

    // Mixed of bind join and (index) nested loop join
    public Boolean matchTPAddBindJoinAndTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddBindJoin() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddBJFILTERAndTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddBJFILTER() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddBJUNIONAndTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddBJUNION() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddBJVALUESAndTPAddIndexNLJ() {
        if ( popOfTPAdd.matchTPAddBJVALUES() && popOfSubquery.matchTPAddIndexNLJ() ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddIndexNLJAndTPAddBindJoin() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddBindJoin() ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddIndexNLJAndTPAddBJFILTER() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddBJFILTER() ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddIndexNLJAndTPAddBJUNION() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddBJUNION() ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddIndexNLJAndTPAddBJVALUES() {
        if ( popOfTPAdd.matchTPAddIndexNLJ() && popOfSubquery.matchTPAddBJVALUES() ){
            return true;
        }
        return false;
    }

}
