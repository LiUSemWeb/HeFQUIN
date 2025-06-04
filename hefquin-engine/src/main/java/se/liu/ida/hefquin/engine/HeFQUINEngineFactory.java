package se.liu.ida.hefquin.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import se.liu.ida.hefquin.engine.HeFQUINEngineConfigReader.Context;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.federation.catalog.FederationDescriptionReader;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;

/**
 * Factory class that can be used to create a fully-wired instance of
 * {@link HeFQUINEngine} and its execution {@link Context}. The returned wrapper
 * is {@code AutoCloseable} so that the caller can rely on *try-with-resources*
 * to guarantee clean-up of thread pools and other resources.
 */
public class HeFQUINEngineFactory
{
	/**
	 * Creates an enginebased on a federation catalogue file and the default engine
	 * configuration.
	 *
	 * @param fedDescFile path to a federation catalogue Turtle file
	 * @return a wrapper for the initialized engine and context
	 */
	public static HeFQUINEngineAndContext create( final String fedDescFile ) {
		final Model confDesc = loadDefaultEngineConf();
		return create(fedDescFile, confDesc);
	}

	/**
	 * Creates an engine based on a federation catalogue file and an engine
	 * configuration model.
	 *
	 * @param fedCatFile path to a federation catalogue Turtle file
	 * @param engineConf model describing the engine configuration
	 * @return a wrapper for the initialized engine and context
	 */
	public static HeFQUINEngineAndContext create( final String fedCatFile,
	                                              final Model engineConf ) {
		final FederationCatalog fedCat = loadFedDesc(fedCatFile);
		return create(fedCat, engineConf);
	}

	/**
	 * Creates an engine based on a federation catalogue model and an engine
	 * configuration model.
	 *
	 * @param fedCatModel model describing a federation catalogue
	 * @param engineConf  model describing an engine configuration
	 * @return a wrapper for the initialized engine and context
	 */
	public static HeFQUINEngineAndContext create( final Model fedCatModel,
	                                              final Model engineConf ) {
		final FederationCatalog cat = loadFedDesc(fedCatModel);
		return create(cat, engineConf);
	}

	/**
	 * Creates an engine based on a federation catalogue and an engine configuration
	 * model.
	 *
	 * @param cat        federation catalogue
	 * @param engineConf model describing an engine configuration
	 * @return a wrapper for the initialized engine and context
	 */
	public static HeFQUINEngineAndContext create( final FederationCatalog fedCat,
	                                              final Model engineConf ) {
		final ExecutorService execFed = HeFQUINEngineDefaultComponents
				.createExecutorServiceForFedAccess();
		final ExecutorService execPlan = HeFQUINEngineDefaultComponents
				.createExecutorServiceForPlanTasks();

		// create context
		final Context ctx = new HeFQUINEngineConfigReader.Context() {
			public ExecutorService getExecutorServiceForFederationAccess() { return execFed; }
			public ExecutorService getExecutorServiceForPlanTasks() { return execPlan; }
			public FederationCatalog getFederationCatalog() { return fedCat; }
			public boolean isExperimentRun() { return false; }
			public boolean skipExecution() { return false; }
			public LogicalPlanPrinter getSourceAssignmentPrinter() { return null; }
			public LogicalPlanPrinter getLogicalPlanPrinter() { return null; }
			public PhysicalPlanPrinter getPhysicalPlanPrinter() { return null; }
		};

		// init engine
		final HeFQUINEngine engine = new HeFQUINEngineConfigReader().read(engineConf, ctx);
		ARQ.init();
		engine.integrateIntoJena();

		return new HeFQUINEngineAndContext(engine, ctx);
	}

	// ───────────── helper methods ─────────────

	/**
	 * Loads the default configuration description and return it as a Jena model.
	 * 
	 * @return model describing the default engine configuration
	 */
	private static Model loadDefaultEngineConf() {
        String ttl = HeFQUINEngineDefaultComponents.getDefaultConfigurationDescription();
        return RDFParser.fromString(ttl).lang(Lang.TURTLE).toModel();
    }

	/**
     * Reads a federation description from the specified Turtle file.
     *
	 * @param fedCatFile path to a federation catalogue Turtle file
	 * @return a federation catalogue
	 */
	private static FederationCatalog loadFedDesc( final String fedCatFile ) {
        return FederationDescriptionReader.readFromFile(fedCatFile);
    }

	/**
     * Reads a federation description from the specified model.
     *
	 * @param fedCatModel model describing a federation catalogue
	 * @return a federation catalogue
	 */
	private static FederationCatalog loadFedDesc( final Model fedCatModel ) {
        return FederationDescriptionReader.readFromModel(fedCatModel);
    }

	// ─────────────── engine wrapper ───────────────

	/**
	 * Immutable wrapper that provides a {@link HeFQUINEngine} instance and the
	 * {@link Context} it was configured with. Implements
	 * {@link java.lang.AutoCloseable}.
	 */
	public record HeFQUINEngineAndContext( HeFQUINEngine engine, Context ctx )
			implements AutoCloseable {

		/**
		 * Closes both executor services in the context. The method first attempts to
		 * performs an orderly shutdown. If the tasks fail to finish within 500 ms
		 * {@link ExecutorService#shutdownNow()} is called.
		 */
		@Override
		public void close() {
			shutdown( ctx.getExecutorServiceForFederationAccess() );
			shutdown( ctx.getExecutorServiceForPlanTasks() );
		}

		/**
		 * Helper method to shut down a single executor service within a bounded wait
		 * time of 500ms.
		 *
		 * @param executorService the executor service to terminate
		 */
		private static void shutdown( final ExecutorService executorService ) {
			executorService.shutdown();
			try {
				if ( ! executorService.awaitTermination( 500L, TimeUnit.MILLISECONDS ) ) {
					executorService.shutdownNow();
				}
			}
			catch ( InterruptedException ex ) {
				Thread.currentThread().interrupt();
				executorService.shutdownNow();
			}
		}
	}
}