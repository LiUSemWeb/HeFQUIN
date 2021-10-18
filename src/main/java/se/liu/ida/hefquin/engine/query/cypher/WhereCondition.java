package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Set;

/**
 * Represents the different types of conditions that can be used in the WHERE clause
 * of a Cypher Query, such as n.p=l, EXISTS(n.p) or ID(n)=22
 */
public interface WhereCondition {

    /**
     * Obtains the set of variables used in the condition
     * @return a set of CypherVar
     * For instance, this method for n.p=l returns {n}
     */
    Set<CypherVar> getVars();
}
