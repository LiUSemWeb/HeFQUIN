package se.liu.ida.hefquin.service;

import java.io.IOException;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.system.stream.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineBuilder;

public class SharedResourceInitializer implements ServletContextListener
{
	private static Logger logger = LoggerFactory.getLogger( SharedResourceInitializer.class );

	@Override
	public void contextInitialized( ServletContextEvent servletContextEvent ) {
		final String confDescr = System.getProperty( "hefquin.configuration", "DefaultEngineConf.ttl" );
		final String fedCat = System.getProperty( "hefquin.federation", "DefaultFedConf.ttl" );

		logger.info( "--- Initialize engine ---" );
		logger.info( "hefquin.configuration: {}", confDescr );
		logger.info( "hefquin.federation:    {}", fedCat );

		check( confDescr );
		check( fedCat );

		final HeFQUINEngine engine = new HeFQUINEngineBuilder()
			.withFederationCatalog(fedCat)
			.withEngineConfiguration(confDescr)
			.build();

		servletContextEvent.getServletContext().setAttribute("engine", engine);
	}

	/**
	 * Checks whether the given file or URI is accessible.
	 *
	 * @param filenameOrURI path or URI to check
	 * @throws RuntimeException if the resource is not accessible
	 */
	private void check( final String filenameOrURI ) {
		try ( TypedInputStream in = StreamManager.get().open( filenameOrURI ) ) {
			if ( in == null ) {
				throw new IOException( "Resource not found or cannot be opened: " + filenameOrURI );
			}
		} catch ( IOException e ) {
			throw new RuntimeException( "Failed to access: " + filenameOrURI, e );
		}
	}
}
