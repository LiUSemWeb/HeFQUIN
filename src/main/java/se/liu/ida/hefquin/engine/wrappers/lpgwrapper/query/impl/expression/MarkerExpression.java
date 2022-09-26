package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;

public class MarkerExpression extends AliasedExpression{

    private static final CypherExpression marker = new LiteralExpression("$");

    public MarkerExpression(final CypherVar alias) {
        super(marker, alias);
    }

}
