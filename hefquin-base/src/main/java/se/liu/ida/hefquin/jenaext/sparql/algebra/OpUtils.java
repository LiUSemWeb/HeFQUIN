package se.liu.ida.hefquin.jenaext.sparql.algebra;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.Vars;

import se.liu.ida.hefquin.jenaext.graph.TripleUtils;

/**
 * This class provides useful functionality related to Jena's
 * {@link Op} interface and the classes that implement it.
 */
public class OpUtils
{
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
			for ( final Triple tp : opBGP.getPattern().getList() ) {
				Vars.addVarsFromTriple(acc, tp);
			}
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
		}
		else if ( op instanceof OpPath opPath ) {
			final TriplePath path = opPath.getTriplePath();

			if ( path.getSubject().isVariable() )
				acc.add( Var.alloc(path.getSubject()) );
			if ( path.getObject().isVariable() )
				acc.add( Var.alloc(path.getObject()) );
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
				n += TripleUtils.getNumberOfVarMentions(tp);
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
			return getNumberOfVarMentions( opProject.getSubOp() );
		}
		else if ( op instanceof OpPath opPath ) {
			final TriplePath path = opPath.getTriplePath();
			int n = 0;

			if ( path.getSubject().isVariable() )
				n++;
			if ( path.getObject().isVariable() )
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
				n += TripleUtils.getNumberOfTermMentions(tp);
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

			if ( path.getSubject().isConcrete() ) {
				n++;
			}
			if ( path.getObject().isConcrete() ) {
				n++;
			}
			if ( path.isTriple() && path.asTriple().getPredicate().isConcrete() ) {
				n++;
			}

			return n;
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

}
