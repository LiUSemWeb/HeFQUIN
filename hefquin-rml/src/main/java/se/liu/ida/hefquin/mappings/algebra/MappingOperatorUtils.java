package se.liu.ida.hefquin.mappings.algebra;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtract;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpJoin;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpProject;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOperatorUtils
{
	public static Set<SourceReference> extractAllSrcRefs( final MappingOperator op ) {
		if ( op instanceof MappingOpExtract e ) {
			return Set.of( e.getSourceReference() );
		}

		final Set<SourceReference> collection = new HashSet<>();
		extractAllSrcRefs(op, collection);
		return collection;
	}

	public static void extractAllSrcRefs( final MappingOperator op,
	                                      final Set<SourceReference> collection ) {
		if ( op instanceof MappingOpExtract e ) {
			collection.add( e.getSourceReference() );
		}
		else if ( op instanceof MappingOpExtend e ) {
			extractAllSrcRefs( e.getSubOp(), collection );
		}
		else if ( op instanceof MappingOpProject p ) {
			extractAllSrcRefs( p.getSubOp(), collection );
		}
		else if ( op instanceof MappingOpJoin j ) {
			extractAllSrcRefs( j.getSubOp1(), collection );
			extractAllSrcRefs( j.getSubOp2(), collection );
		}
		else if ( op instanceof MappingOpUnion u ) {
			for ( final MappingOperator subOp : u.getSubOps() ) {
				extractAllSrcRefs(subOp, collection);
			}
		}
		else {
			throw new IllegalArgumentException( op.getClass().getName() );
		}
	}

}
