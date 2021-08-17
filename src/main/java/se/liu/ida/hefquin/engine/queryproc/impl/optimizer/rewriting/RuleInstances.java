package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl.RuleConvertTPAddBJFILTERToHashJoin;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl.RuleConvertTPAddBJUNIONToHashJoin;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.impl.RuleConvertTPAddIndexNLJToHashJoin;

import java.util.HashSet;
import java.util.Set;

public class RuleInstances {
    Set<RewritingRule> ruleApplications = new HashSet<>();

    public void addRuleInstances() {
        ruleApplications.add(new RuleConvertTPAddIndexNLJToHashJoin(0.15));
        ruleApplications.add(new RuleConvertTPAddBJFILTERToHashJoin(0.15));
        ruleApplications.add(new RuleConvertTPAddBJUNIONToHashJoin(0.15));
    }

    /*
    // Convert TPAdd to Join (category: D)
    // Convert to HashJoin
    public static RewritingRule rule2 = (RewritingRule) new RuleConvertTPAddBJFILTERToHashJoin();
    public static RewritingRule rule3 = (RewritingRule) new RuleConvertTPAddBJUNIONToHashJoin();
    public static RewritingRule rule4 = (RewritingRule) new RuleConvertTPAddBJVALUESToHashJoin();
    public static RewritingRule rule5 = (RewritingRule) new RuleConvertTPAddBJToHashJoin();

    // Convert to SymmetricHashJoin
    public static RewritingRule rule6 = (RewritingRule) new RuleConvertTPAddIndexNLJToSymmetricHashJoin();
    public static RewritingRule rule7 = (RewritingRule) new RuleConvertTPAddBJFILTERToSymmetricHashJoin();
    public static RewritingRule rule8 = (RewritingRule) new RuleConvertTPAddBJUNIONToSymmetricHashJoin();
    public static RewritingRule rule9 = (RewritingRule) new RuleConvertTPAddBJVALUESToSymmetricHashJoin();
    public static RewritingRule rule10 = (RewritingRule) new RuleConvertTPAddBJToSymmetricHashJoin();

    // Convert to NaiveIndexNLJ
    public static RewritingRule rule11 = (RewritingRule) new RuleConvertTPAddIndexNLJToNaiveIndexNLJ();
    public static RewritingRule rule12 = (RewritingRule) new RuleConvertTPAddBJFILTERToNaiveIndexNLJ();
    public static RewritingRule rule13 = (RewritingRule) new RuleConvertTPAddBJUNIONToNaiveIndexNLJ();
    public static RewritingRule rule14 = (RewritingRule) new RuleConvertTPAddBJVALUESToNaiveIndexNLJ();
    public static RewritingRule rule15 = (RewritingRule) new RuleConvertTPAddBJToNaiveIndexNLJ();

    // Conversion of physical algorithms (category: C)
    // Convert TPAdd(IndexNLJ) to other physical algorithms
    public static RewritingRule rule16 = (RewritingRule) new RuleConvertTPAddIndexNLJToBindJoin();
    public static RewritingRule rule17 = (RewritingRule) new RuleConvertTPAddIndexNLJToBJFILTER();
    public static RewritingRule rule18 = (RewritingRule) new RuleConvertTPAddIndexNLJToBJUNION();
    public static RewritingRule rule19 = (RewritingRule) new RuleConvertTPAddIndexNLJToBJVALUES();

    // Convert TPAdd(Bind Join) to TPAdd(IndexNLJ)
    public static RewritingRule rule20 = (RewritingRule) new RuleConvertTPAddBindJoinToIndexNLJ();
    public static RewritingRule rule21 = (RewritingRule) new RuleConvertTPAddBJFILTERToIndexNLJ();
    public static RewritingRule rule22 = (RewritingRule) new RuleConvertTPAddBJUNIONToIndexNLJ();
    public static RewritingRule rule23 = (RewritingRule) new RuleConvertTPAddBJVALUESToIndexNLJ();

    // Order tweaking of two TPAdd (category: B)
    public static RewritingRule rule24 = (RewritingRule) new RuleChangeOrderOfTwoTPAddIndexNLJ();
    public static RewritingRule rule25 = (RewritingRule) new RuleChangeOrderOfTwoTPAddBindJoin();
    public static RewritingRule rule26 = (RewritingRule) new RuleChangeOrderOfTwoTPAddBJFILTER();
    //public static RewritingRule rule27 = new RuleChangeOrderOfTwoTPAddBJUNION();
    //public static RewritingRule rule28 = new RuleChangeOrderOfTwoTPAddBJVALUES();

    public static RewritingRule rule29 = (RewritingRule) new RuleChangeOrderOfTPAddBJFILTERAndIndexNLJ();
    //public static RewritingRule rule30 = new RuleChangeOrderOfTPAddBJUNIONAndTPAddIndexNLJ();
    //public static RewritingRule rule31 = new RuleChangeOrderOfTPAddBJVALUESAndTPAddIndexNLJ();
    //public static RewritingRule rule32 = new RuleChangeOrderOfTPAddBindJoinAndTPAddIndexNLJ();

    //public static RewritingRule rule33 = new RuleChangeOrderOfTPAddIndexNLJAndTPAddBindJoin();
    //public static RewritingRule rule34 = new RuleChangeOrderOfTPAddIndexNLJAndTPAddBJFILTER();
    //public static RewritingRule rule35 = new RuleChangeOrderOfTPAddIndexNLJAndTPAddBJUNION();
    //public static RewritingRule rule36 = new RuleChangeOrderOfTPAddIndexNLJAndTPAddBJVALUES();
     */

}
