package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesMarshallable;
import net.openhft.chronicle.bytes.BytesOut;
import se.liu.ida.hefquin.base.data.SolutionMapping;

/**
 * ChronicleMap-native ({@link BytesMarshallable}) {@link SolutionMapping}
 * implementation backed by a serialized map of variable bindings.
 */
public class MarshallableSolutionMapping implements SolutionMapping, BytesMarshallable
{
	private Map<String, String> bindings = new LinkedHashMap<>();
	
	public MarshallableSolutionMapping(){}

	public MarshallableSolutionMapping( final SolutionMapping solutionMapping ){
		assert solutionMapping != null;

        final Binding binding = solutionMapping.asJenaBinding();
        final Iterator<Var> vars = binding.vars();
        while (vars.hasNext()) {
            final Var var = vars.next();
            final Node node = binding.get(var);

			bindings.put( var.getVarName(), node == null ? null : serializeNode(node) );
        }
	}

	protected static String serializeNode( final Node node ) {
        return NodeFmtLib.strNT(node);
    }

	protected static Node deserializeNode( final String s ) {
        return NodeFactoryExtra.parseNode(s);
    }

    @Override
	public void writeMarshallable( final BytesOut<?> out ) {
		out.writeInt( bindings.size() );
		for ( final Map.Entry<String, String> e : bindings.entrySet() ) {
			out.writeUtf8( e.getKey() );
			out.writeUtf8( e.getValue() );
        }
    }

    @Override
	public void readMarshallable( final BytesIn<?> in ) {
		final int size = in.readInt();
        bindings = new LinkedHashMap<>(size);

        for (int i = 0; i < size; i++) {
            final String varName = in.readUtf8();
            final String nodeString = in.readUtf8();
            bindings.put(varName, nodeString);
        }
    }

	@Override
	public Binding asJenaBinding() {
		final BindingBuilder builder = BindingBuilder.create();

		for ( final Map.Entry<String, String> e : bindings.entrySet() ) {
			final String varName = e.getKey();
			final String encoded = e.getValue();

			if ( encoded != null ) {
				final Node node = deserializeNode(encoded);
				builder.add( Var.alloc(varName), node );
			}
		}

		return builder.build();
	}

	@Override
	public boolean equals( final Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null || getClass() != obj.getClass() )
			return false;
		return bindings.equals( ((MarshallableSolutionMapping) obj).bindings );
	}

	@Override
	public int hashCode() {
		return bindings.hashCode();
	}

	@Override
	public String toString() {
		return "MarshallableSolutionMapping{bindings=" + bindings.size() + "}";
	}
}
