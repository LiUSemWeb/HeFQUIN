package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingTuple;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExpression;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExpressionUtils;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOpExtend extends BaseForMappingOperator
{
	protected final MappingOperator subOp;
	protected final ExtendExpression expr;
	protected final String attribute;

	protected final Set<String> schema;
	protected final boolean valid;

	public MappingOpExtend( final MappingOperator subOp,
	                        final ExtendExpression expr,
	                        final String attribute ) {
		assert subOp != null;
		assert expr != null;
		assert attribute != null;

		this.subOp = subOp;
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
			final Set<String> varsInExpr = ExtendExpressionUtils.getAllMentionedAttributes(expr);
			valid = schemaOfSubOp.containsAll(varsInExpr);
		}
	}

	@Override
	public Set<String> getSchema() {
		return schema;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		return subOp.isValidInput(srMap);
	}

	@Override
	public Iterator<MappingTuple> evaluate( final Map<SourceReference, DataObject> srMap ) {
		// TODO
		throw new UnsupportedOperationException();
	}

}
