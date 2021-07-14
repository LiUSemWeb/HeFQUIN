package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

public class RuleInstances {
    public static Rule rule1 = new RuleConvertTPAddIndexNLJToHashJoin();
    public static Rule rule2 = new RuleConvertTPAddBJFilterToHashJoin();

}
