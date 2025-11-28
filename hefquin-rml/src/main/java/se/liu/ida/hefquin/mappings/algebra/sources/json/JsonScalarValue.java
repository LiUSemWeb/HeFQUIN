package se.liu.ida.hefquin.mappings.algebra.sources.json;

import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;

public class JsonScalarValue implements DataObject
{
	final Object v;

	public JsonScalarValue( final String v ) {
		assert v != null;
		this.v = v;
	}

	public JsonScalarValue( final Integer v ) {
		assert v != null;
		this.v = v;
	}

	public JsonScalarValue( final Double v ) {
		assert v != null;
		this.v = v;
	}

	public JsonScalarValue( final Boolean v ) {
		assert v != null;
		this.v = v;
	}

	public Object getValue() { return v; }

	public String asString() { return v.toString(); }

	@Override
	public String toString() { return v.toString(); }

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		return (    o instanceof JsonScalarValue jsv
		         && jsv.v.equals(v) );
	}

}
