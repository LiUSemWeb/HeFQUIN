package se.liu.ida.hefquin.engine.utils;

public class Pair<T1,T2>
{
	final public T1 object1;
	final public T2 object2;

	public Pair ( final T1 object1, final T2 object2 ) {
		this.object1 = object1;
		this.object2 = object2;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof Pair) )
			return false; 

		final Pair<?,?> oo = (Pair<?,?>) o;
		if ( this == oo )
			return true;

		return oo.object1.equals(object1) && oo.object2.equals(object2);
	}
}
