package se.liu.ida.hefquin.jenaext.sparql.algebra;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnfold;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_Path1;
import org.apache.jena.sparql.path.P_Path2;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.util.VarUtils;

/**
 * This class provides useful functionality related to Jena's
 * {@link Op} interface and the classes that implement it.
 */
public class OpUtils
{
	/**
	 * Returns a version of the given operator (including its sub-operators)
	 * in which all triple patterns that contained Jena-renamed variables
	 * are replaced by versions in which the renaming of the variables is
	 * undone. 
	 */
	public static Op removeRenamedVariables( final Op op ) {
		return Transformer.transform(renamedVarsRemover, op);
	}

	/**
	 * Returns the set of all variables mentioned in the graph pattern
	 * represented  by the given operator, except for the variables that
	 * occur only in expressions (in FILTER or in BIND).
	 */
	public static Set<Var> getVariablesInPattern( final Op op ) {
		// TODO: It is better to implement this function using an OpVisitor.

		final Set<Var> result = new HashSet<>();
		addVariablesFromPattern(result, op);
		return result;
	}

	/**
	 * Adds all variables mentioned in the graph pattern represented by the
	 * given operator to the given set of variables, except for the variables
	 * that occur only in expressions (in FILTER or in BIND).
	 */
	public static void addVariablesFromPattern( final Set<Var> acc, final Op op ) {
		// TODO: It is better to implement this function using an OpVisitor.

		if ( op instanceof OpBGP opBGP ) {
			VarUtils.addVars( acc, opBGP.getPattern() );
		}
		else if ( op instanceof OpJoin || op instanceof OpLeftJoin || op instanceof OpUnion ) {
			addVariablesFromPattern( acc, (Op2) op );
		}
		else if ( op instanceof OpService opService ) {
			addVariablesFromPattern( acc, opService.getSubOp() );
		}
		else if ( op instanceof OpFilter opFilter ) {
			addVariablesFromPattern( acc, opFilter.getSubOp() );
		}
		else if ( op instanceof OpExtend opExtend ) {
			addVariablesFromPattern( acc, opExtend.getSubOp() );
			acc.addAll( opExtend.getVarExprList().getVars() );
		}
		else if ( op instanceof OpTable opTable ) {
			acc.addAll( opTable.getTable().getVars() );
		}
		else if ( op instanceof OpSlice opSlice ) {
			addVariablesFromPattern( acc, opSlice.getSubOp() );
		}
		else if ( op instanceof OpSequence opSequence ) {
			for ( final Op element : opSequence.getElements() )
				addVariablesFromPattern( acc, element );
		}
		else if ( op instanceof OpProject opProject ) {
			addVariablesFromPattern( acc, opProject.getSubOp() );
			acc.addAll( opProject.getVars() );
		}
		else if ( op instanceof OpPath opPath ) {
			final TriplePath path = opPath.getTriplePath();

			VarUtils.addVarsFromTriplePath(acc, path);

			// The following is necessary because the currently
			// implementation of VarUtils.addVarsFromTriplePath(..)
			// does not consider this case.
			if ( path.isTriple() )
				VarUtils.addVar( acc, path.getPredicate() );
		}
		else if ( op instanceof OpDistinct opDistinct ) {
			addVariablesFromPattern( acc, opDistinct.getSubOp() );
		}
		else if ( op instanceof OpUnfold opUnfold ) {
			addVariablesFromPattern( acc, opUnfold.getSubOp() );
			acc.add( opUnfold.getVar1() );

			if ( opUnfold.getVar2() != null )
				acc.add( opUnfold.getVar2() );
		}
		else {
			throw new UnsupportedOperationException("Getting the variables from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
		}
	}

	/**
	 * Adds all variables mentioned in the graph pattern represented by the
	 * given operator to the given set of variables, except for the variables
	 * that occur only in expressions (in FILTER or in BIND).
	 */
	public static void addVariablesFromPattern( final Set<Var> acc, final Op2 op ) {
		addVariablesFromPattern( acc, op.getLeft() );
		addVariablesFromPattern( acc, op.getRight() );
	}

	/**
	 * Returns the number of times any variable is mentioned in the graph
	 * pattern represented by the given operator (if the same variable is
	 * mentioned multiple times, then each of these mentions is counted),
	 * but ignores variable mentions in expressions (in FILTER or in BIND).
	 */
	public static int getNumberOfVarMentions( final Op op ) {
		// TODO: It is better to implement this function using an OpVisitor.

		if ( op instanceof OpBGP opBGP ) {
			int n = 0;
			for ( final Triple tp : opBGP.getPattern().getList() ) {
				n += getNumberOfVarMentionsFromNode( tp.getSubject() )
				   + getNumberOfVarMentionsFromNode( tp.getPredicate() )
				   + getNumberOfVarMentionsFromNode( tp.getObject() );
			}
			return n;
		}
		else if ( op instanceof OpJoin || op instanceof OpLeftJoin || op instanceof OpUnion ) {
			return getNumberOfVarMentions( (Op2) op );
		}
		else if ( op instanceof OpService opService ) {
			return getNumberOfVarMentions( opService.getSubOp() );
		}
		else if ( op instanceof OpFilter opFilter ) {
			return getNumberOfVarMentions( opFilter.getSubOp() );
		}
		else if ( op instanceof OpExtend opExtend ) {
			return getNumberOfVarMentions( opExtend.getSubOp() ) + opExtend.getVarExprList().getVars().size();
		}
		else if ( op instanceof OpSlice opSlice ) {
			return getNumberOfVarMentions( opSlice.getSubOp() );
		}
		else if ( op instanceof OpSequence opSequence ) {
			int n = 0;
			for ( final Op element : opSequence.getElements() ) {
				n += getNumberOfVarMentions( element );
			}
			return n;
		}
		else if ( op instanceof OpProject opProject ) {
			return getNumberOfVarMentions( opProject.getSubOp() ) + opProject.getVars().size();
		}
		else if ( op instanceof OpPath opPath ) {
			final TriplePath path = opPath.getTriplePath();
			int n = 0;

			n += getNumberOfVarMentionsFromNode( path.getSubject() );
			n += getNumberOfVarMentionsFromNode( path.getObject() );

			if ( path.isTriple() )
				n += getNumberOfVarMentionsFromNode( path.getPredicate() );

			return n;
		}
		else if ( op instanceof OpDistinct opDistinct ) {
			return getNumberOfVarMentions( opDistinct.getSubOp() );
		}
		else if ( op instanceof OpUnfold opUnfold ) {
			int n = getNumberOfVarMentions( opUnfold.getSubOp() );
			n++;

			if ( opUnfold.getVar2() != null )
				n++;

			return n;
		}
		else {
			throw new UnsupportedOperationException("Getting the number of elements (variables) from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
		}
	}

	/**
	 * Returns the number of times any variable is mentioned in the graph
	 * pattern represented by the given operator (if the same variable is
	 * mentioned multiple times, then each of these mentions is counted),
	 * but ignores variable mentions in expressions (in FILTER or in BIND).
	 */
	public static int getNumberOfVarMentions( final Op2 op ) {
		final int numLeft = getNumberOfVarMentions( op.getLeft() );
		final int numRight = getNumberOfVarMentions( op.getRight() );

		return numLeft + numRight;
	}

	/**
	 * Returns the number of times any RDF term is mentioned in the graph
	 * pattern represented by the given operator (if the same term is
	 * mentioned multiple times, then each of these mentions is counted),
	 * but ignores terms mentioned in expressions (in FILTER or in BIND).
	 */
	public static int getNumberOfTermMentions( final Op op ) {
		// TODO: It is better to implement this function using an OpVisitor.

		if ( op instanceof OpBGP opBGP ) {
			int n = 0;
			for ( final Triple tp : opBGP.getPattern().getList() ) {
				n += getNumberOfTermMentionsFromNode( tp.getSubject() )
				   + getNumberOfTermMentionsFromNode( tp.getPredicate() )
				   + getNumberOfTermMentionsFromNode( tp.getObject() );
			}
			return n;
		}
		else if ( op instanceof OpJoin || op instanceof OpLeftJoin || op instanceof OpUnion ) {
			return getNumberOfTermMentions( (Op2) op );
		}
		else if ( op instanceof OpService opService ) {
			return getNumberOfTermMentions( opService.getSubOp() );
		}
		else if ( op instanceof OpFilter opFilter ) {
			return getNumberOfTermMentions( opFilter.getSubOp() );
		}
		else if ( op instanceof OpExtend opExtend ) {
			return getNumberOfTermMentions( opExtend.getSubOp() );
		}
		else if ( op instanceof OpSlice opSlice ) {
			return getNumberOfTermMentions( opSlice.getSubOp() );
		}
		else if ( op instanceof OpSequence opSequence ) {
			int n = 0;
			for ( final Op element : opSequence.getElements() ) {
				n += getNumberOfTermMentions( element );
			}
			return n;
		}
		else if ( op instanceof OpProject opProject ) {
			return getNumberOfTermMentions( opProject.getSubOp() );
		}
		else if ( op instanceof OpPath opPath ) {
			final TriplePath path = opPath.getTriplePath();
			int n = 0;

			n += getNumberOfTermMentionsFromNode( path.getSubject() );
			n += getNumberOfTermMentionsFromNode( path.getObject() );

			if ( path.isTriple() ) {
				n += getNumberOfTermMentionsFromNode( path.getPredicate() );
			}
			else {
				n += getNumberOfTermMentionsFromPath( path.getPath() );
			}

			return n;
		}
		else if ( op instanceof OpDistinct opDistinct ) {
			return getNumberOfTermMentions( opDistinct.getSubOp() );
		}
		else if ( op instanceof OpUnfold opUnfold ) {
			return getNumberOfTermMentions( opUnfold.getSubOp() );
		}
		else {
			throw new UnsupportedOperationException("Getting the number of elements (RDF terms) from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
		}
	}

	/**
	 * Returns the number of times any RDF term is mentioned in the graph
	 * pattern represented by the given operator (if the same term is
	 * mentioned multiple times, then each of these mentions is counted),
	 * but ignores terms mentioned in expressions (in FILTER or in BIND).
	 */
	public static int getNumberOfTermMentions( final Op2 op ) {
		final int numLeft = getNumberOfTermMentions( op.getLeft() );
		final int numRight = getNumberOfTermMentions( op.getRight() );

		return numLeft+numRight;
	}

	/**
	 * Returns the number of times any RDF term is mentioned in the given
	 * property path expression (if the same term is mentioned multiple
	 * times, then each of these mentions is counted).
	 */
	protected static int getNumberOfTermMentionsFromPath( final Path path ) {
		if ( path instanceof P_Link pLink ) {
			return pLink.getNode().isConcrete() ? 1 : 0;
		}
		else if ( path instanceof P_ReverseLink pReverseLink ) {
			return pReverseLink.getNode().isConcrete() ? 1 : 0;
		}
		else if ( path instanceof P_Path1 pPath1 ) {
			return getNumberOfTermMentionsFromPath( pPath1.getSubPath() );
		}
		else if ( path instanceof P_Path2 pPath2 ) {
			return getNumberOfTermMentionsFromPath( pPath2.getLeft() )
				+ getNumberOfTermMentionsFromPath( pPath2.getRight() );
		}
		else if ( path instanceof P_NegPropSet pNegPropSet ) {
			int n = 0;

			for ( final P_Path0 path0 : pNegPropSet.getNodes() ) {
				if ( path0.getNode().isConcrete() ) {
					n++;
				}
			}

			return n;
		}

		return 0;
	}

	/**
	 * Returns the number of times any variable is mentioned in the given
	 * RDF node (if the same variable is mentioned multiple times, then each
	 * of these mentions is counted). If the node is a triple term, variables
	 * mentioned in the nested triple are counted recursively.
	 */
	protected static int getNumberOfVarMentionsFromNode( final Node node ) {
		if ( node.isVariable() )
			return 1;

		if ( node.isTripleTerm() ) {
			final Triple triple = node.getTriple();

			return getNumberOfVarMentionsFromNode( triple.getSubject() )
				 + getNumberOfVarMentionsFromNode( triple.getPredicate() )
				 + getNumberOfVarMentionsFromNode( triple.getObject() );
		}

		return 0;
	}

	/**
	 * Returns the number of times any RDF term is mentioned in the given
	 * RDF node (if the same term is mentioned multiple times, then each of
	 * these mentions is counted). If the node is a triple term, terms
	 * mentioned in the nested triple are counted recursively.
	 */
	protected static int getNumberOfTermMentionsFromNode( final Node node ) {
		if ( node.isTripleTerm() ) {
			final Triple triple = node.getTriple();

			return getNumberOfTermMentionsFromNode( triple.getSubject() )
				 + getNumberOfTermMentionsFromNode( triple.getPredicate() )
				 + getNumberOfTermMentionsFromNode( triple.getObject() );
		}

		return node.isConcrete() ? 1 : 0;
	}

	/**
	 * This is doing the actual work for {@link #removeRenamedVariables(Op)}.
	 */
	public static Transform renamedVarsRemover = new TransformCopy(false) {
		@Override
		public Op transform( final OpBGP op ) {
			// Before attempting to rewrite the BGP, check whether that
			// is actually needed (are the renamed variables in the BGP?)
			boolean renamedVarFound = false;
			final Iterator<Var> it = getVariablesInPattern(op).iterator();
			while ( ! renamedVarFound  && it.hasNext() ) {
				if ( Var.isRenamedVar(it.next()) )
					renamedVarFound = true;
			}

			if ( ! renamedVarFound )
				return op;

			// Now, rewrite.
			final BasicPattern newBGP = new BasicPattern();
			for ( final Triple tp : op.getPattern() ) {
				newBGP.add( removeRenamedVars(tp) );
			}

			return new OpBGP(newBGP);
		}

		@Override
		public Op transform( final OpTriple op ) {
			final Triple tp = removeRenamedVars( op.getTriple() );

			if ( tp == op.getTriple() ) return op;

			return new OpTriple(tp);
		}

		@Override
		public Op transform( final OpPath op ) {
			final Node s = op.getTriplePath().getSubject();
			final Node o = op.getTriplePath().getObject();

			final Node s2 = removeRenamedVar(s);
			final Node o2 = removeRenamedVar(o);

			final Path pp = op.getTriplePath().getPath();
			if ( pp != null ) {
				if ( s == s2 && o == o2 )
					return op;

				return new OpPath( new TriplePath(s2,pp,o2) );
			}

			final Node p = op.getTriplePath().getPredicate();
			final Node p2 = removeRenamedVar(p);

			if ( s == s2 && p == p2 && o == o2 )
				return op;

			return new OpPath( new TriplePath(Triple.create(s2,p2,o2)) );
		}

		@Override
		public Op transform( final OpQuad op ) {
			throw new UnsupportedOperationException("We currently don't support OpQuad during removal of Jena-renamed variables.");
		}

		@Override
		public Op transform( final OpQuadPattern op ) {
			throw new UnsupportedOperationException("We currently don't support OpQuadPattern during removal of Jena-renamed variables.");
		}

		@Override
		public Op transform( final OpQuadBlock op ) {
			throw new UnsupportedOperationException("We currently don't support OpQuadBlock during removal of Jena-renamed variables.");
		}
	};

	/**
	 * Returns a version of the given triple pattern in which Jena's
	 * renaming of variables (if any) is undone. The function also
	 * handles nested triple patterns (as are possible with SPARQL
	 * 1.2), with the guarantee that no new Java objects are created
	 * if there are no renamed variables at any step of the recursion.
	 *
	 * This function is mainly a helper for {@link #renamedVarsRemover}.
	 */
	public static Triple removeRenamedVars( final Triple tp ) {
		final Node s = tp.getSubject();
		final Node p = tp.getPredicate();
		final Node o = tp.getObject();

		final Node s2 = removeRenamedVar(s);
		final Node p2 = removeRenamedVar(p);
		final Node o2 = removeRenamedVar(o);

		if ( s == s2 && p == p2 && o == o2 )
			return tp;

		return Triple.create(s2, p2, o2);
	}

	/**
	 * If the given node is a Jena-renamed variable, this function
	 * returns a version of that variable with the renaming undone. If
	 * the node is a triple pattern (as is possible with SPARQL 1.2),
	 * the function recursively removes renamed variables from that
	 * one as well (with the guarantee that no new Java objects are
	 * created if there are no renamed variables in the triple pattern).
	 */
	public static Node removeRenamedVar( final Node x ) {
		if ( x.isTripleTerm() ) {
			final Triple tp = removeRenamedVars( x.getTriple() );
			if ( tp == x.getTriple() ) return x;
			return NodeFactory.createTripleTerm(tp);
		}

		if ( ! Var.isRenamedVar(x) )
			return x;

		final String newVarName = x.getName().substring(1);
		return Var.alloc(newVarName);
	}

}
