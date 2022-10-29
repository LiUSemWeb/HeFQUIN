package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class LiteralExpression implements CypherExpression {

    protected final String value;
    protected final XSDDatatype datatype;

    public LiteralExpression(final String value) {
        assert value != null;
        this.value = value;
        this.datatype = XSDDatatype.XSDstring;
    }

    public LiteralExpression(final String value, final XSDDatatype datatype) {
        assert value != null;
        this.value = value;
        this.datatype = datatype;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.emptySet();
    }

    @Override
    public void acceptVisitor(final CypherExpressionVisitor visitor) {
        visitor.visitLiteral(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiteralExpression that = (LiteralExpression) o;
        return value.equals(that.value)  && datatype.equals(that.datatype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, datatype);
    }

    @Override
    public String toString() {
        if (datatype.equals(XSDDatatype.XSDdate) || datatype.equals(XSDDatatype.XSDstring))
            return "'" + value + "'";
        else return "" + value;
    }
}
