package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Record2SolutionMappingTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherUnionQuery;

/**
 * This class represents a special type of {@link AliasedExpression}. An object of this
 * class represents a marker that helps the Record to Solution Mapping translator
 * {@link Record2SolutionMappingTranslator} to identify from which sub-query any given
 * record comes from. The identification is done through the index field in a way that for
 * a given {@link CypherUnionQuery}, each of its sub-queries is enumerated consecutively.
 */
public class MarkerExpression extends AliasedExpression {

    private final int index;
    private static final String markerPrefix = "$";

    public MarkerExpression(final int index, final CypherVar alias) {
        super(new LiteralExpression(markerPrefix + index), alias);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
