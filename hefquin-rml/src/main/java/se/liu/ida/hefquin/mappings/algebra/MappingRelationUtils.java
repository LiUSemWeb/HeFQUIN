package se.liu.ida.hefquin.mappings.algebra;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

public class MappingRelationUtils
{
	/**
	 * Returns the RDF dataset resulting from the given mapping relation.
	 *
	 * @param r - the mapping relation from which the RDF dataset is extracted
	 * @return the extracted RDF dataset
	 *
	 * @throws IllegalArgumentException if the schema of the given mapping
	 *     relation does not include the expected attributes
	 *     ({@link MappingRelation#sAttr}, {@link MappingRelation#pAttr}, etc.)
	 */
	public static Dataset convertToRDF( final MappingRelation r ) {
		final List<String> schema = r.getSchema();
		final int sIdx = schema.indexOf( MappingRelation.sAttr );
		final int pIdx = schema.indexOf( MappingRelation.pAttr );
		final int oIdx = schema.indexOf( MappingRelation.oAttr );
		// TODO: extend this function to cover the graph component as well

		if ( sIdx == -1 || pIdx == -1 || oIdx == -1 )
			throw new IllegalArgumentException("The schema of the given mapping relation does not contain all relevant attributes: " + schema.toString() );

		final MappingRelationCursor c = r.getCursor();

		final Dataset ds = DatasetFactory.create();
		final Graph g = ds.asDatasetGraph().getDefaultGraph();
		while ( c.hasNext() ) {
			c.advance();

			final Node s = c.getValueOfCurrentTuple(sIdx);
			final Node p = c.getValueOfCurrentTuple(pIdx);
			final Node o = c.getValueOfCurrentTuple(oIdx);

			if (    s != MappingRelation.errorNode
			     && p != MappingRelation.errorNode
			     && o != MappingRelation.errorNode ) {
				g.add(s, p, o);
			}
		}

		return ds;
	}

}
