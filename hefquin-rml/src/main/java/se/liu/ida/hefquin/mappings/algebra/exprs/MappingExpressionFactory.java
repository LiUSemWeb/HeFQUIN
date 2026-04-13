package se.liu.ida.hefquin.mappings.algebra.exprs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.lib.Pair;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpConstant;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtract;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpJoin;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpProject;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.sources.DataObject;

public class MappingExpressionFactory
{
	public static MappingExpression create(
			final MappingOperator rootOp,
			final MappingExpression ... subExprs ) {
		return worker.createMappingExpression(rootOp, subExprs);
	}

	protected static MyWorker worker = new MyWorker();

	protected static class MyWorker implements MappingOperatorVisitor {
		protected MappingExpression[] subExprs;

		protected Set<String> schema;
		protected boolean valid;

		protected MappingExpression createMappingExpression(
				final MappingOperator rootOp,
				final MappingExpression ... subExprs ) {
			this.subExprs = subExprs;
			schema = null;
			valid = false;

			rootOp.visit(this);

			// At this point, 'valid' is set only with respect to the given
			// root operator. The validity of the mapping expression to be
			// created depends also on the validity of the sub-expressions
			// (but we do not need to check those if the root operator is
			// already invalid).
			if ( valid && subExprs != null ) {
				int i = 0;
				while ( valid && i < subExprs.length ) {
					valid = subExprs[i++].isValid(); 
				}
			}

			return new MappingExpressionImpl(valid, schema, rootOp, subExprs);
		}

		@Override
		public <DDS extends DataObject,
		        DC1 extends DataObject,
		        DC2 extends DataObject,
		        QL1 extends Query,
		        QL2 extends Query>
		void visit( final MappingOpExtract<DDS, DC1, DC2, QL1, QL2> op ) {
			assert subExprs == null || subExprs.length == 0;

			valid = true;
			schema = op.getAttributesOfP();
		}

		@Override
		public void visit( final MappingOpConstant op ) {
			assert subExprs == null || subExprs.length == 0;

			valid = true;
			schema = new HashSet<>( op.getMappingRelation().getSchema() );
		}

		@Override
		public void visit( final MappingOpExtend op ) {
			assert subExprs != null || subExprs.length == 1;

			final Set<String> schemaOfSubExpr = subExprs[0].getSchema();

			schema = new HashSet<>(schemaOfSubExpr);
			final boolean added = schema.add( op.getAttribute() );

			if ( added == false ) {
				valid = false;
			}
			else {
				final Set<String> varsInExpr = op.getExtendExpression().getAllMentionedAttributes();
				valid = schemaOfSubExpr.containsAll(varsInExpr);
			}
		}

		@Override
		public void visit( final MappingOpProject op ) {
			assert subExprs != null || subExprs.length == 1;

			final Set<String> schemaOfSubExpr = subExprs[0].getSchema();

			if ( schemaOfSubExpr.containsAll(op.getP()) ) {
				schema = op.getP();
				valid = true;
			}
			else {
				valid = false;
				schema = new HashSet<>();
				for ( final String attr : op.getP() ) {
					if ( schemaOfSubExpr.contains(attr) )
						schema.add(attr);
				}
			}
		}

		@Override
		public void visit( final MappingOpJoin op ) {
			assert subExprs != null || subExprs.length == 2;

			final Set<String> schemaOfSubExpr1 = subExprs[0].getSchema();
			final Set<String> schemaOfSubExpr2 = subExprs[1].getSchema();

			schema = new HashSet<>();
			schema.addAll(schemaOfSubExpr1);
			schema.addAll(schemaOfSubExpr2);

			if ( schema.size() != schemaOfSubExpr1.size()+schemaOfSubExpr2.size() ) {
				valid = false;
			}
			else {
				valid = isValid( op.getJ(), schemaOfSubExpr1, schemaOfSubExpr2 );
			}
		}

		protected static boolean isValid( final List<Pair<String,String>> J,
		                                  final Set<String> schemaOfSubExpr1,
		                                  final Set<String> schemaOfSubExpr2 ) {
			for ( final Pair<String,String> j : J ) {
				if ( ! schemaOfSubExpr1.contains(j.getLeft()) ) return false;
				if ( ! schemaOfSubExpr2.contains(j.getRight()) ) return false;
			}

			return true;
		}

		@Override
		public void visit( final MappingOpUnion op ) {
			assert subExprs != null || subExprs.length > 0;

			schema = subExprs[0].getSchema();

			valid = true;
			for ( int i = 1; i < subExprs.length; i++ ) {
				final Set<String> schemaOfSubExpr = subExprs[i].getSchema();

				if ( valid && ! schema.equals(schemaOfSubExpr) ) {
					schema = new HashSet<>(schema);
					schema.addAll(schemaOfSubExpr);
					valid = false;
				}
				else if ( ! valid ) {
					schema.addAll(schemaOfSubExpr);
				}
			}
		}

	} // end of MyWorker

}
