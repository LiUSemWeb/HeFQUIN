package se.liu.ida.hefquin.engine.data.jenaimpl;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class TriplesToSolMapsConverter
{

	public static Iterator<? extends SolutionMapping> convert( final Iterable<Triple> itTriples, final TriplePattern tp ) {
		return convert( itTriples.iterator(), tp );
	}

	public static Iterator<? extends SolutionMapping> convert( final Iterator<Triple> itTriples, final TriplePattern tp ) {
		final org.apache.jena.graph.Triple jTP = tp.asJenaTriple();
		final String s = ( Var.isVar(jTP.getSubject()) ) ? jTP.getSubject().getName() : null;
		final String p = ( Var.isVar(jTP.getPredicate()) ) ? jTP.getPredicate().getName() : null;
		final String o = ( Var.isVar(jTP.getObject()) ) ? jTP.getObject().getName() : null;

		if ( s != null ) {
			if ( p != null && ! p.equals(s) ) {
				if ( o != null && ! o.equals(s) && ! o.equals(p) ) {
					return new ConvertingIterSPO( itTriples, tp );
				} else {
					return new ConvertingIterSP( itTriples, Var.alloc(jTP.getSubject()), Var.alloc(jTP.getPredicate()) );
				}
			} else if ( o != null && ! o.equals(s) ) {
				return new ConvertingIterSO( itTriples, Var.alloc(jTP.getSubject()), Var.alloc(jTP.getObject()) );
			} else {
				return new ConvertingIterS( itTriples, Var.alloc(jTP.getSubject()) );
			}
		} else if ( p != null ) {
			if ( o != null && ! o.equals(p) ) {
				return new ConvertingIterPO( itTriples, Var.alloc(jTP.getPredicate()), Var.alloc(jTP.getObject()) );
			} else {
				return new ConvertingIterP( itTriples, Var.alloc(jTP.getPredicate()) );
			}
		} else if ( o != null ) {
			return new ConvertingIterO( itTriples, Var.alloc(jTP.getObject()) );
		} else {
			return new ConvertingIterEmpty( itTriples );
		}
	}

	protected static abstract class ConvertingIterBase implements Iterator<JenaBasedSolutionMapping>
	{
		protected final Iterator<Triple> it;

		protected ConvertingIterBase( final Iterator<Triple> it ) {
			assert it != null;
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public JenaBasedSolutionMapping next() {
			return convert( it.next() );
		}

		protected abstract JenaBasedSolutionMapping convert( final Triple t );
	}

	protected static class ConvertingIterEmpty extends ConvertingIterBase
	{
		protected ConvertingIterEmpty( final Iterator<Triple> it ) {
			super(it);
		}

		@Override
		protected JenaBasedSolutionMapping convert( final Triple t ) {
			return JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping();
		}
	}

	protected static abstract class ConvertingIterBase1 extends ConvertingIterBase
	{
		protected final Var var;

		protected ConvertingIterBase1( final Iterator<Triple> it, final Var var ) {
			super(it);

			assert var != null;
			this.var = var;
		}

		@Override
		protected JenaBasedSolutionMapping convert( final Triple t ) {
			return JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping( var, getRelevantNode(t) );
		}

		protected abstract Node getRelevantNode( final Triple t );
	}

	protected static class ConvertingIterS extends ConvertingIterBase1
	{
		public ConvertingIterS( final Iterator<Triple> it, final Var var ) {
			super(it, var);
		}

		@Override
		protected Node getRelevantNode( final Triple t ) {
			return t.asJenaTriple().getSubject();
		}
	}

	protected static class ConvertingIterP extends ConvertingIterBase1
	{
		public ConvertingIterP( final Iterator<Triple> it, final Var var ) {
			super(it, var);
		}

		@Override
		protected Node getRelevantNode( final Triple t ) {
			return t.asJenaTriple().getPredicate();
		}
	}

	protected static class ConvertingIterO extends ConvertingIterBase1
	{
		public ConvertingIterO( final Iterator<Triple> it, final Var var ) {
			super(it, var);
		}

		@Override
		protected Node getRelevantNode( final Triple t ) {
			return t.asJenaTriple().getObject();
		}
	}

	protected static abstract class ConvertingIterBase2 extends ConvertingIterBase
	{
		protected final Var var1;
		protected final Var var2;

		protected ConvertingIterBase2( final Iterator<Triple> it, final Var var1, final Var var2 ) {
			super(it);

			assert var1 != null;
			assert var2 != null;
			this.var1 = var1;
			this.var2 = var2;
		}

		@Override
		protected JenaBasedSolutionMapping convert( final Triple t ) {
			return JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping( var1, getRelevantNode1(t), var2, getRelevantNode2(t) );
		}

		protected abstract Node getRelevantNode1( final Triple t );

		protected abstract Node getRelevantNode2( final Triple t );
	}

	protected static class ConvertingIterSP extends ConvertingIterBase2
	{
		public ConvertingIterSP( final Iterator<Triple> it, final Var var1, final Var var2 ) {
			super(it, var1, var2);
		}

		@Override
		protected Node getRelevantNode1( final Triple t ) {
			return t.asJenaTriple().getSubject();
		}

		@Override
		protected Node getRelevantNode2( final Triple t ) {
			return t.asJenaTriple().getPredicate();
		}
	}

	protected static class ConvertingIterSO extends ConvertingIterBase2
	{
		public ConvertingIterSO( final Iterator<Triple> it, final Var var1, final Var var2 ) {
			super(it, var1, var2);
		}

		@Override
		protected Node getRelevantNode1( final Triple t ) {
			return t.asJenaTriple().getSubject();
		}

		@Override
		protected Node getRelevantNode2( final Triple t ) {
			return t.asJenaTriple().getObject();
		}
	}

	protected static class ConvertingIterPO extends ConvertingIterBase2
	{
		public ConvertingIterPO( final Iterator<Triple> it, final Var var1, final Var var2 ) {
			super(it, var1, var2);
		}

		@Override
		protected Node getRelevantNode1( final Triple t ) {
			return t.asJenaTriple().getPredicate();
		}

		@Override
		protected Node getRelevantNode2( final Triple t ) {
			return t.asJenaTriple().getObject();
		}
	}

	protected static class ConvertingIterSPO extends ConvertingIterBase
	{
		protected final Var var1;
		protected final Var var2;
		protected final Var var3;

		protected ConvertingIterSPO( final Iterator<Triple> it, TriplePattern tp ) {
			super(it);

			var1 = Var.alloc( tp.asJenaTriple().getSubject() );
			var2 = Var.alloc( tp.asJenaTriple().getPredicate() );
			var3 = Var.alloc( tp.asJenaTriple().getObject() );
		}

		@Override
		protected JenaBasedSolutionMapping convert( final Triple t ) {
			final org.apache.jena.graph.Triple tt = t.asJenaTriple();
			return JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
					var1, tt.getSubject(),
					var2, tt.getPredicate(),
					var3, tt.getObject() );
		}
	}

}
