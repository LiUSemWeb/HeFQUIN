package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExpression;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;

public class MappingOpExtend extends BaseForUnaryMappingOperator
{
	protected final ExtendExpression expr;
	protected final String attribute;

	protected final Set<String> schema;
	protected final boolean valid;

	public MappingOpExtend( final MappingOperator subOp,
	                        final ExtendExpression expr,
	                        final String attribute ) {
		super(subOp);
		assert expr != null;
		assert attribute != null;

		this.expr = expr;
		this.attribute = attribute;

		final Set<String> schemaOfSubOp = subOp.getSchema();
		schema = new HashSet<>();
		schema.addAll(schemaOfSubOp);
		schema.add(attribute);

		if ( ! subOp.isValid() ) {
			valid = false;
		}
		else if ( schemaOfSubOp.size() == schema.size() ) {
			valid = false;
		}
		else {
			final Set<String> varsInExpr = expr.getAllMentionedAttributes();
			valid = schemaOfSubOp.containsAll(varsInExpr);
		}
	}

	public String getAttribute() { return attribute; }

	public ExtendExpression getExtendExpression() { return expr; }

	@Override
	public Set<String> getSchema() {
		return schema;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		return subOp.isValidInput(srMap);
	}
}
