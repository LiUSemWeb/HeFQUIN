package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

public class RuleInstances {

    // Convert TPAdd to Join (category: D)
    public static Rule rule1 = new RuleConvertTPAddIndexNLJToHashJoin();
    public static Rule rule2 = new RuleConvertTPAddBJFilterToHashJoin();
    public static Rule rule3 = new RuleConvertTPAddBJUNIONToHashJoin();
    public static Rule rule4 = new RuleConvertTPAddBJVALUESToHashJoin();
    public static Rule rule5 = new RuleConvertTPAddBJToHashJoin();

    public static Rule rule6 = new RuleConvertTPAddIndexNLJToSymmetricHashJoin();
    public static Rule rule7 = new RuleConvertTPAddBJFilterToSymmetricHashJoin();
    public static Rule rule8 = new RuleConvertTPAddBJUNIONToSymmetricHashJoin();
    public static Rule rule9 = new RuleConvertTPAddBJVALUESToSymmetricHashJoin();
    public static Rule rule10 = new RuleConvertTPAddBJToSymmetricHashJoin();

    // continue: Convert to NaiveIndexNLJ
    // Order tweaking of two TPAdd (category: B)
    // Conversion of physical algorithm (category: C)

}
