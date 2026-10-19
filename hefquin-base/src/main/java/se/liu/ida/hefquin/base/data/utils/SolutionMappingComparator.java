package se.liu.ida.hefquin.base.data.utils;

import java.util.Comparator;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public class SolutionMappingComparator implements Comparator<SolutionMapping>
{
	protected final List<SortCondition> conditions;

	public SolutionMappingComparator( final List<SortCondition> conditions ) {
		assert conditions != null;
		this.conditions = conditions;
	}

	@Override
	public int compare( final SolutionMapping sm1, final SolutionMapping sm2 ) {
		for ( final SortCondition sc : conditions ) {
			final Binding b1 = sm1.asJenaBinding();
			final Binding b2 = sm2.asJenaBinding();

			final NodeValue nv1 = ExprUtils.eval(sc.expression, b1);
			final NodeValue nv2 = ExprUtils.eval(sc.expression, b2);

			int comparison = BindingComparator.compareNodesRaw(nv1, nv2);

			if ( sc.direction == Query.ORDER_DESCENDING ) {
				comparison = -comparison;
			}

			if ( comparison != 0 ) {
				return comparison;
			}
		}

		return 0;
	}

}
