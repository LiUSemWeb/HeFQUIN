package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;

public class MarkerExpression extends AliasedExpression{

    private static final String markerPrefix = "$";

    public MarkerExpression(final int index, final CypherVar alias) {
        super(new LiteralExpression(markerPrefix + index), alias);
    }

}
