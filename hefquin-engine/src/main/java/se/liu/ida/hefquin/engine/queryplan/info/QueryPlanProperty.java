package se.liu.ida.hefquin.engine.queryplan.info;

/**
 * Represents a particular property of a query plan as determined and
 * used during query planning. Typical examples of such properties are
 * the (estimated) cardinality of the query result produced by the plan
 * and the (estimated) number of requests that will be issued when running
 * the plan.
 * <p>
 * In addition to its type (as captured by the {@link Type} class) and its
 * value, each such property has a {@link Quality} score which indicates
 * how certain the value is.
 */
public class QueryPlanProperty
{
	public static enum Quality {
		PURE_GUESS(0),
		MIN_OR_MAX_POSSIBLE(1),
		ESTIMATE_BASED_ON_ESTIMATES(2),
		ESTIMATE_BASED_ON_ACCURATES(3),
		DIRECT_ESTIMATE(4),
		ACCURATE(5);

		final int pos;
		Quality( final int pos ) { this.pos = pos; }

		public boolean higherThan( final Quality other ) {
			return pos > other.pos;
		}
	}

	public static class Type {
		public final String name;
		public Type( final String name ) { this.name = name; }
	}

	/**
	 * A property of this type specifies the (potentially estimated) number
	 * of solution mappings that can be expected from the corresponding query
	 * plan.
	 */
	public static Type CARDINALITY = new Type("cardinality");

	/**
	 * A property of this type specifies the (potentially estimated) maximum
	 * number of solution mappings that can be expected from the corresponding
	 * query plan.
	 */
	public static Type MAX_CARDINALITY = new Type("max cardinality");

	/**
	 * A property of this type specifies the (potentially estimated) minimum
	 * number of solution mappings that can be expected from the corresponding
	 * query plan.
	 */
	public static Type MIN_CARDINALITY = new Type("min cardinality");

	/**
	 * Creates a property of type {@link #CARDINALITY}.
	 * @param value - the value to be used for the created property
	 * @param quality - the quality of the given value
	 * @return the created property
	 */
	public static QueryPlanProperty cardinality( final int value,
	                                             final Quality quality ) {
		return new QueryPlanProperty(CARDINALITY, value, quality);
	}

	/**
	 * Creates a property of type {@link #MAX_CARDINALITY}.
	 * @param value - the value to be used for the created property
	 * @param quality - the quality of the given value
	 * @return the created property
	 */
	public static QueryPlanProperty maxCardinality( final int value,
	                                                final Quality quality ) {
		return new QueryPlanProperty(MAX_CARDINALITY, value, quality);
	}

	/**
	 * Creates a property of type {@link #MIN_CARDINALITY}.
	 * @param value - the value to be used for the created property
	 * @param quality - the quality of the given value
	 * @return the created property
	 */
	public static QueryPlanProperty minCardinality( final int value,
	                                                final Quality quality ) {
		return new QueryPlanProperty(MIN_CARDINALITY, value, quality);
	}

	/**
	 * Creates a copy of the given property (its type and its value) with
	 * a reduced quality score.
	 *
	 * @param p - the property to be copied
	 * @return the copy
	 */
	public static QueryPlanProperty copyWithReducedQuality( final QueryPlanProperty p ) {
		final Quality reducedQuality = switch (p.quality) {
			case ACCURATE:
				yield Quality.ESTIMATE_BASED_ON_ACCURATES;
			case DIRECT_ESTIMATE:
				yield Quality.ESTIMATE_BASED_ON_ESTIMATES;
			case ESTIMATE_BASED_ON_ACCURATES:
				yield Quality.ESTIMATE_BASED_ON_ESTIMATES;
			case ESTIMATE_BASED_ON_ESTIMATES:
				yield Quality.ESTIMATE_BASED_ON_ESTIMATES;
			case MIN_OR_MAX_POSSIBLE:
				yield Quality.MIN_OR_MAX_POSSIBLE;
			case PURE_GUESS:
				yield Quality.PURE_GUESS;
		};

		return new QueryPlanProperty( p.type, p.value, reducedQuality );
	}

	protected final Type type;
	protected final int value;
	protected final Quality quality;

	/**
	 * Creates a new {@link QueryPlanProperty} with the given type, value,
	 * and quality score.
	 *
	 * @param type - the type of the property to be created
	 * @param value - the value of the property to be created
	 * @param quality - the quality score of the property to be created
	 */
	public QueryPlanProperty( final Type type, final int value, final Quality quality ) {
		this.type = type;
		this.value = value;
		this.quality = quality;
	}

	/**
	 * Returns the type of this property.
	 * @return the type of this property
	 */
	public Type getType() { return type; }

	/**
	 * Returns the value of this property.
	 * @return the value of this property
	 */
	public int getValue() { return value; }

	/**
	 * Returns the quality score of this property.
	 * @return the quality score of this property
	 */
	public Quality getQuality() { return quality; }

}
