package se.liu.ida.hefquin.jenaext.sparql.algebra;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.algebra.AlgebraGenerator;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.util.Context;

import se.liu.ida.hefquin.jenaext.sparql.algebra.op.OpServiceWithParams;
import se.liu.ida.hefquin.jenaext.sparql.syntax.ElementServiceWithParams;

public class AlgebraGeneratorForHeFQUIN extends AlgebraGenerator
{
	protected final Context context;
	protected final int subQueryDepth;

	public AlgebraGeneratorForHeFQUIN() {
		this( ARQ.getContext().copy(), 0 );
	}

	protected AlgebraGeneratorForHeFQUIN( final Context cxt, final int depth ) {
		super(cxt, depth);
		
		this.context = cxt;
		this.subQueryDepth = depth;
	}

	@Override
	protected Op compileElementService( final ElementService e ) {
		if ( e instanceof ElementServiceWithParams ewp ) {
			return new OpServiceWithParams( ewp.getServiceNode(),
			                                compileElement( ewp.getElement() ),
			                                ewp.getSilent(),
			                                ewp.getParamVars() );
		}
		else
			return super.compileElementService(e);
	}

	@Override
	protected Op compileElementSubquery( final ElementSubQuery e ) {
		final AlgebraGenerator gen = new AlgebraGeneratorForHeFQUIN(context, subQueryDepth + 1);
		return gen.compile( e.getQuery() );
	}

}
