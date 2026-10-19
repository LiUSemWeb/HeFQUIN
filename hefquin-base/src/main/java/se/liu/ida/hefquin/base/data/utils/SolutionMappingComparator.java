package se.liu.ida.hefquin.base.data.utils;

import java.util.Comparator;
import java.util.List;

import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.engine.binding.BindingComparator;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public class SolutionMappingComparator implements Comparator<SolutionMapping>
{
	private final BindingComparator bindingComparator;

	public SolutionMappingComparator( final List<SortCondition> conditions ) {
		bindingComparator = new BindingComparator(conditions);
	}

	@Override
	public int compare( final SolutionMapping sm1, final SolutionMapping sm2 ) {
		return bindingComparator.compare(sm1.asJenaBinding(), sm2.asJenaBinding());
	}

}
