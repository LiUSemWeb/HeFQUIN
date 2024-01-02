package se.liu.ida.hefquin.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.impl.QueryProcessorImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.planning.QueryPlannerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.jenaext.ModelUtils;
import se.liu.ida.hefquin.vocabulary.ECVocab;

/**
 * Reads an RDF description of a configuration for the HeFQUIN engine and
 * creates a {@link HeFQUINEngine} object based on this configuration.
 */
public class HeFQUINEngineConfigReader
{
	/**
	 * Creates a {@link HeFQUINEngine} that is configured based on
	 * the description in the given RDF file. Assumes that the file
	 * describes only one such configuration.
	 */
	public HeFQUINEngine readFromFile( final String filename, final Context ctx ) {
		final Model m = RDFDataMgr.loadModel(filename);
		return read(m, ctx);
	}

	/**
	 * Creates a {@link HeFQUINEngine} that is configured based on the
	 * configuration identified by the given URI in the given RDF file.
	 */
	public HeFQUINEngine readFromFile( final String filename, final String uriOfConfRsrc, final Context ctx ) {
		final Model m = RDFDataMgr.loadModel(filename);
		final Resource confRsrc = m.createResource(uriOfConfRsrc);

		if ( ! m.contains(confRsrc, null) )
			throw new IllegalArgumentException("There is no description of the given URI (" + uriOfConfRsrc + ") in " + filename);

		return read(confRsrc, ctx);
	}

	/**
	 * Creates a {@link HeFQUINEngine} that is configured based on
	 * the description in the given RDF model. Assumes that the
	 * model describes only one such configuration.
	 */
	public HeFQUINEngine read( final Model m, final Context ctx ) {
		final ResIterator itConfigs = m.listResourcesWithProperty(RDF.type, ECVocab.HeFQUINEngineConfiguration);

		if ( ! itConfigs.hasNext() ) {
			throw new IllegalArgumentException("The given RDF description does not contain a HeFQUINEngineConfiguration.");
		}

		final Resource confRsrc = itConfigs.next();

		if ( itConfigs.hasNext() ) {
			throw new IllegalArgumentException("The given RDF description contains more than one HeFQUINEngineConfiguration.");
		}

		return read(confRsrc, ctx);
	}

	public HeFQUINEngine read( final Resource confRsrc, final Context ctx ) {
		final FederationAccessManager fedAccessMgr = readFederationAccessManager(confRsrc, ctx);

		final ExtendedContext ctxx = new ExtendedContextImpl2(ctx, fedAccessMgr);
		final QueryProcessor qproc = readQueryProcessor(confRsrc, ctxx);

		return new HeFQUINEngineImpl(fedAccessMgr, qproc);
	}

	public interface Context {
		ExecutorService getExecutorServiceForFederationAccess();
		ExecutorService getExecutorServiceForPlanTasks();
		FederationCatalog getFederationCatalog();
		boolean isExperimentRun();
		boolean withPrintingOfSourceAssignment();
		boolean withPrintingOfLogicalPlan();
		boolean withPrintingOfPhysicalPlan();
	}


	// ------------ federation access manager ------------

	public FederationAccessManager readFederationAccessManager( final Resource confRsrc,
	                                                            final Context ctx ) {
		final Resource rsrc = ModelUtils.getSingleMandatoryResourceProperty( confRsrc, ECVocab.fedAccessMgr );

		if ( rsrc.equals(ECVocab.DefaultFederationAccessManager) )
			return createDefaultFederationAccessManager(ctx);

		final ExtendedContext ctxx = new ExtendedContextImpl1(ctx);

		final Object i;
		try {
			i = instantiate(rsrc, ctxx);
		}
		catch ( final Exception e ) {
			throw new IllegalArgumentException("Instantiating the FederationAccessManager caused an exception (type: " + e.getClass().getName() + "): " + e.getMessage() );
		}

		return (FederationAccessManager) i;
	}

	public FederationAccessManager createDefaultFederationAccessManager( final Context ctx ) {
		return HeFQUINEngineDefaultComponents.createDefaultFederationAccessManager( ctx.getExecutorServiceForFederationAccess() );
	}


	// ------------ query processor ------------

	public QueryProcessor readQueryProcessor( final Resource confRsrc,
	                                          final ExtendedContext ctx ) {
		final Resource rsrc = ModelUtils.getSingleMandatoryResourceProperty( confRsrc, ECVocab.queryProcessor );

		final CostModel cm = readCostModel(rsrc, ctx);
		ctx.complete(cm);

		final QueryPlanner planner = readQueryPlanner(rsrc, ctx);
		final QueryPlanCompiler compiler = readQueryPlanCompiler(rsrc, ctx);
		final ExecutionEngine exec = readExecutionEngine(rsrc, ctx);

		return new QueryProcessorImpl( planner, compiler, exec, ctx.getQueryProcContext() );
	}

	public CostModel readCostModel( final Resource qprocRsrc, final ExtendedContext ctx ) {
		final Resource rsrc = ModelUtils.getSingleOptionalResourceProperty( qprocRsrc, ECVocab.costModel );

		if ( rsrc == null )
			return null;

		if ( rsrc.equals(ECVocab.DefaultCostModel) )
			return createDefaultCostModel(ctx);

		final Object i;
		try {
			i = instantiate(rsrc, ctx);
		}
		catch ( final Exception e ) {
			throw new IllegalArgumentException("Instantiating the CostModel caused an exception (type: " + e.getClass().getName() + "): " + e.getMessage() );
		}

		return (CostModel) i;
	}

	public QueryPlanner readQueryPlanner( final Resource queryProcRsrc,
	                                      final ExtendedContext ctx ) {
		final Resource rsrc = ModelUtils.getSingleMandatoryResourceProperty( queryProcRsrc, ECVocab.queryPlanner );

		final SourcePlanner spl = readSourcePlanner(rsrc, ctx);
		final LogicalOptimizer lopt = readLogicalOptimizer(rsrc, ctx);
		final PhysicalOptimizer popt = readPhysicalOptimizer(rsrc, ctx);

		return new QueryPlannerImpl( spl, lopt, popt,
		                             ctx.withPrintingOfSourceAssignment(),
		                             ctx.withPrintingOfLogicalPlan(),
		                             ctx.withPrintingOfPhysicalPlan() );
	}

	public SourcePlanner readSourcePlanner( final Resource qplRsrc, final ExtendedContext ctx ) {
		final Resource rsrc = ModelUtils.getSingleMandatoryResourceProperty( qplRsrc, ECVocab.sourcePlanner );

		if ( rsrc.equals(ECVocab.DefaultSourcePlanner) )
			return createDefaultSourcePlanner(ctx);

		final Object i;
		try {
			i = instantiate(rsrc, ctx);
		}
		catch ( final Exception e ) {
			throw new IllegalArgumentException("Instantiating the SourcePlanner caused an exception (type: " + e.getClass().getName() + "): " + e.getMessage() );
		}

		return (SourcePlanner) i;
	}

	public LogicalOptimizer readLogicalOptimizer( final Resource qplRsrc, final ExtendedContext ctx ) {
		final Resource rsrc = ModelUtils.getSingleMandatoryResourceProperty( qplRsrc, ECVocab.logicalOptimizer );

		if ( rsrc.equals(ECVocab.DefaultLogicalOptimizer) )
			return createDefaultLogicalOptimizer(ctx);

		final Object i;
		try {
			i = instantiate(rsrc, ctx);
		}
		catch ( final Exception e ) {
			throw new IllegalArgumentException("Instantiating the LogicalOptimizer caused an exception (type: " + e.getClass().getName() + "): " + e.getMessage() );
		}

		return (LogicalOptimizer) i;
	}

	public PhysicalOptimizer readPhysicalOptimizer( final Resource qplRsrc, final ExtendedContext ctx ) {
		final Resource rsrc = ModelUtils.getSingleMandatoryResourceProperty( qplRsrc, ECVocab.physicalOptimizer );

		if ( rsrc.equals(ECVocab.DefaultPhysicalOptimizer) )
			return HeFQUINEngineDefaultComponents.createDefaultPhysicalOptimizer();

		final Object i;
		try {
			i = instantiate(rsrc, ctx);
		}
		catch ( final Exception e ) {
			throw new IllegalArgumentException("Instantiating the PhysicalOptimizer caused an exception (type: " + e.getClass().getName() + "): " + e.getMessage() );
		}

		return (PhysicalOptimizer) i;
	}

	public QueryPlanCompiler readQueryPlanCompiler( final Resource qprocRsrc, final ExtendedContext ctx ) {
		final Resource rsrc = ModelUtils.getSingleMandatoryResourceProperty( qprocRsrc, ECVocab.planCompiler );

		if ( rsrc.equals(ECVocab.DefaultPlanCompiler) )
			return createDefaultPlanCompiler(ctx);

		final Object i;
		try {
			i = instantiate(rsrc, ctx);
		}
		catch ( final Exception e ) {
			throw new IllegalArgumentException("Instantiating the PlanCompiler caused an exception (type: " + e.getClass().getName() + "): " + e.getMessage() );
		}

		return (QueryPlanCompiler) i;
	}

	public ExecutionEngine readExecutionEngine( final Resource qprocRsrc, final ExtendedContext ctx ) {
		final Resource rsrc = ModelUtils.getSingleMandatoryResourceProperty( qprocRsrc, ECVocab.executionEngine );

		if ( rsrc.equals(ECVocab.DefaultExecutionEngine) )
			return HeFQUINEngineDefaultComponents.createDefaultExecutionEngine();

		final Object i;
		try {
			i = instantiate(rsrc, ctx);
		}
		catch ( final Exception e ) {
			throw new IllegalArgumentException("Instantiating the ExecutionEngine caused an exception (type: " + e.getClass().getName() + "): " + e.getMessage() );
		}

		return (ExecutionEngine) i;
	}

	public CostModel createDefaultCostModel( final ExtendedContext ctx ) {
		return HeFQUINEngineDefaultComponents.createDefaultCostModel( ctx.getQueryProcContext() );
	}

	public SourcePlanner createDefaultSourcePlanner( final ExtendedContext ctx ) {
		return HeFQUINEngineDefaultComponents.createDefaultSourcePlanner( ctx.getQueryProcContext() );
	}

	public LogicalOptimizer createDefaultLogicalOptimizer( final ExtendedContext ctx ) {
		return HeFQUINEngineDefaultComponents.createDefaultLogicalOptimizer( ctx.getQueryProcContext() );
	}

	public QueryPlanCompiler createDefaultPlanCompiler( final ExtendedContext ctx ) {
		return HeFQUINEngineDefaultComponents.createDefaultPlanCompiler( ctx.getQueryProcContext() );
	}


	// ------------ helper functions ------------

	protected Object instantiate( final Resource r, final ExtendedContext ctx ) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		// Obtain the Java class to be instantiated.
		final String className = ModelUtils.getSingleMandatoryProperty_XSDString( r, ECVocab.javaClassName );
		final Class<?> c = Class.forName(className);

		// Obtain the list of constructor arguments (if any).
		final RDFNode list = ModelUtils.getSingleOptionalProperty( r, ECVocab.constructorArguments );

		// If there is no list of constructor arguments, create an instance
		// of the class by using the constructor without arguments.
		if ( list == null ) {
			final Constructor<?> ctor = c.getConstructor();
			return ctor.newInstance();
		}

		// Check that the list of constructor arguments is indeed a list.
		if ( ! list.canAs(RDFList.class) )
			throw new IllegalArgumentException( ECVocab.constructorArguments.getLocalName() + " property of " + r.toString() + " should be a list" );

		final Iterator<RDFNode> it = list.as( RDFList.class ).iterator();

		// If the list of constructor arguments is the empty list, create an
		// instance of the class by using the constructor without arguments.
		if ( ! it.hasNext() ) {
			final Constructor<?> ctor = c.getConstructor();
			return ctor.newInstance();
		}

		// At this point we know that the list of constructor arguments
		// is not empty. Iterate over the list to extract the types of
		// the arguments and argument values.
		final List<Class<?>> argTypes = new ArrayList<>();
		final List<Object> argValues = new ArrayList<>();
		while ( it.hasNext()  ) {
			final RDFNode argDescr = it.next();

			// The current constructor argument may be given directly by its
			// value or via a resource that has an rdf:value property or as a
			// resource that describes a Java object to be instantiated.
			// We first check whether it is given directly by its value.
			final RDFNode argValueTerm;
			if (    argDescr.isLiteral()
			     || argDescr.equals(ECVocab.ExecServiceForFedAccess)
			     || argDescr.equals(ECVocab.QueryProcContext)
			     || argDescr.equals(ECVocab.CostModel_INSTANCE) ) {
				argValueTerm = argDescr;
			}
			else {
				final Resource argRsrc = argDescr.asResource();
				argValueTerm = ModelUtils.getSingleOptionalProperty( argRsrc, RDF.value );
			}

			// Now we extract the type and the value of the current argument.
			final Class<?> argType;
			final Object argValue;
			if ( argValueTerm == null )
			{
				final Resource argRsrc = argDescr.asResource();
				final String argTypeName = ModelUtils.getSingleMandatoryProperty_XSDString( argRsrc, ECVocab.argumentTypeName );
				argType = Class.forName(argTypeName);
				argValue = instantiate(argRsrc, ctx);
			}
			else if ( argValueTerm.isLiteral() )
			{
				final Literal argLitValue = argValueTerm.asLiteral();
				final RDFDatatype dt = argLitValue.getDatatype();

				if ( dt.equals(XSDDatatype.XSDstring) ) {
					argType = String.class;
					argValue = argLitValue.getString();
				}
				else if ( dt.equals(XSDDatatype.XSDboolean) ) {
					argType = boolean.class;
					argValue = argLitValue.getBoolean();
				}
				else if ( dt.equals(XSDDatatype.XSDinteger) ) {
					argType = int.class;
					argValue = argLitValue.getInt();
				}
				else if ( dt.equals(XSDDatatype.XSDdouble) ) {
					argType = double.class;
					argValue = argLitValue.getDouble();
				}
				else
					throw new IllegalArgumentException("Literal with unsupported datatype as argument value (datatype URI: " + dt.getURI() + ")");
			}
			else if ( ! argValueTerm.isLiteral() )
			{
				if ( argValueTerm.equals(ECVocab.ExecServiceForFedAccess) ) {
					argType = ExecutorService.class;
					argValue = ctx.getExecutorServiceForFederationAccess();
				}
				else if ( argValueTerm.equals(ECVocab.QueryProcContext) ) {
					argType = QueryProcContext.class;
					argValue = ctx.getQueryProcContext();
				}
				else if ( argValueTerm.equals(ECVocab.CostModel_INSTANCE) ) {
					argType = CostModel.class;
					argValue = ctx.getCostModel();
				}
				else
					throw new IllegalArgumentException("Unsupported designated argument value (" + argValueTerm.toString() + ")");
			}
			else {
				throw new IllegalArgumentException("Shouldn't end up here (" + argValueTerm + ")");
			}

			argTypes.add(argType);
			argValues.add(argValue);
		}

		final Class<?>[] argTypesArray = argTypes.toArray( new Class<?>[argTypes.size()] );
		final Object[] argValuesArray = argValues.toArray( new Object[argValues.size()] );
		final Constructor<?> ctor = c.getConstructor(argTypesArray);
		final Object result = ctor.newInstance(argValuesArray);
		return result;
	}

	// ----- the types of contexts used by this implementation -----

	protected interface ExtendedContext extends Context {
		void complete( CostModel cm );
		QueryProcContext getQueryProcContext();
		CostModel getCostModel();
	}

	protected class ExtendedContextImpl1 implements ExtendedContext {
		protected final Context ctx;

		public ExtendedContextImpl1( final Context ctx ) { this.ctx = ctx; }

		@Override
		public void complete( final CostModel cm ) { throw new UnsupportedOperationException(); }

		@Override
		public QueryProcContext getQueryProcContext() { throw new UnsupportedOperationException(); }

		@Override
		public CostModel getCostModel() { throw new UnsupportedOperationException(); }

		@Override
		public ExecutorService getExecutorServiceForFederationAccess() { return ctx.getExecutorServiceForFederationAccess(); }

		@Override
		public ExecutorService getExecutorServiceForPlanTasks() { return ctx.getExecutorServiceForPlanTasks(); }

		@Override
		public FederationCatalog getFederationCatalog() { return ctx.getFederationCatalog(); }

		@Override
		public boolean isExperimentRun() { return ctx.isExperimentRun(); }

		@Override
		public boolean withPrintingOfSourceAssignment() { return ctx.withPrintingOfSourceAssignment(); }

		@Override
		public boolean withPrintingOfLogicalPlan() { return ctx.withPrintingOfLogicalPlan(); }

		@Override
		public boolean withPrintingOfPhysicalPlan()  { return ctx.withPrintingOfPhysicalPlan(); }
	}

	protected class ExtendedContextImpl2 implements ExtendedContext {
		protected final QueryProcContext qprocCtx;
		protected final ExecutorService execService4FedAccess;
		protected final boolean printSourceAssignment;
		protected final boolean printLogicalPlan;
		protected final boolean printPhysicalPlan;

		protected CostModel costModel = null;
		protected boolean completed = false;

		public ExtendedContextImpl2( final Context ctx, final FederationAccessManager fedAccessMgr ) {
			qprocCtx = createQueryProcContext(ctx, fedAccessMgr);
			execService4FedAccess = ctx.getExecutorServiceForFederationAccess();
			printSourceAssignment = ctx.withPrintingOfSourceAssignment();
			printLogicalPlan      = ctx.withPrintingOfLogicalPlan();
			printPhysicalPlan     = ctx.withPrintingOfPhysicalPlan();
		}

		@Override
		public void complete( final CostModel cm ) {
			costModel = cm;
		}

		@Override
		public QueryProcContext getQueryProcContext() {
			return qprocCtx;
		}

		@Override
		public CostModel getCostModel() {
			if ( ! completed )
				throw new UnsupportedOperationException();

			return costModel;
		}

		@Override
		public ExecutorService getExecutorServiceForFederationAccess() {
			return execService4FedAccess;
		}

		@Override
		public ExecutorService getExecutorServiceForPlanTasks() {
			return qprocCtx.getExecutorServiceForPlanTasks();
		}

		@Override
		public FederationCatalog getFederationCatalog() {
			return qprocCtx.getFederationCatalog();
		}

		@Override
		public boolean isExperimentRun() {
			return qprocCtx.isExperimentRun();
		}

		@Override
		public boolean withPrintingOfSourceAssignment() { return printSourceAssignment; }

		@Override
		public boolean withPrintingOfLogicalPlan() { return printLogicalPlan; }

		@Override
		public boolean withPrintingOfPhysicalPlan()  { return printPhysicalPlan; }
	}

	protected QueryProcContext createQueryProcContext( final Context ctx,
	                                                   final FederationAccessManager fedAccessMgr ) {
		return new QueryProcContext() {
			@Override
			public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }

			@Override
			public FederationCatalog getFederationCatalog() { return ctx.getFederationCatalog(); }

			@Override
			public ExecutorService getExecutorServiceForPlanTasks() { return ctx.getExecutorServiceForPlanTasks(); }

			@Override
			public boolean isExperimentRun() { return ctx.isExperimentRun(); }
		};
	}

}
