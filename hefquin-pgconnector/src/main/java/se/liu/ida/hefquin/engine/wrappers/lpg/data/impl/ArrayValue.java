package se.liu.ida.hefquin.engine.wrappers.lpg.data.impl;

import java.util.List;

import se.liu.ida.hefquin.engine.wrappers.lpg.data.Value;

public class ArrayValue implements Value
{
	protected final List<LiteralValue> array;

	public ArrayValue( final List<LiteralValue> array ) {
		this.array = array;
	}

	public Iterable<LiteralValue> getElements() {
		return array;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("[");

		for ( final LiteralValue elmt : array ) {
			final Object v = elmt.getValue();
			if ( v instanceof String ) {
				b.append("\"");
				b.append( (String) v );
				b.append("\"");
			}
			else {
				b.append( v.toString() );
			}
		}

		b.append("]");
		return b.toString();
    }

}
