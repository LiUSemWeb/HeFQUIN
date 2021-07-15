package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule.impl.*;

public class RuleInstances {

    // Convert TPAdd to Join (category: D)
    // Convert to HashJoin
    public static Rule rule1 = new RuleConvertTPAddIndexNLJToHashJoin();
    public static Rule rule2 = new RuleConvertTPAddBJFilterToHashJoin();
    public static Rule rule3 = new RuleConvertTPAddBJUNIONToHashJoin();
    public static Rule rule4 = new RuleConvertTPAddBJVALUESToHashJoin();
    public static Rule rule5 = new RuleConvertTPAddBJToHashJoin();

    // Convert to SymmetricHashJoin
    public static Rule rule6 = new RuleConvertTPAddIndexNLJToSymmetricHashJoin();
    public static Rule rule7 = new RuleConvertTPAddBJFILTERToSymmetricHashJoin();
    public static Rule rule8 = new RuleConvertTPAddBJUNIONToSymmetricHashJoin();
    public static Rule rule9 = new RuleConvertTPAddBJVALUESToSymmetricHashJoin();
    public static Rule rule10 = new RuleConvertTPAddBJToSymmetricHashJoin();

    // Convert to NaiveIndexNLJ
    public static Rule rule11 = new RuleConvertTPAddIndexNLJToNaiveIndexNLJ();
    public static Rule rule12 = new RuleConvertTPAddBJFILTERToNaiveIndexNLJ();
    public static Rule rule13 = new RuleConvertTPAddBJUNIONToNaiveIndexNLJ();
    public static Rule rule14 = new RuleConvertTPAddBJVALUESToNaiveIndexNLJ();
    public static Rule rule15 = new RuleConvertTPAddBJToNaiveIndexNLJ();

    // Conversion of physical algorithm (category: C)
    public static Rule rule16 = new RuleConvertTPAddBindJoinWithFILTERToVALUES();

    // Order tweaking of two TPAdd (category: B)


}
