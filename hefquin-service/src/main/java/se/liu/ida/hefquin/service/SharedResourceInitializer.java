package se.liu.ida.hefquin.service;

import java.io.IOException;

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.riot.system.stream.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import se.liu.ida.hefquin.engine.HeFQUINEngine;

public class SharedResourceInitializer implements ServletContextListener
{
	private static Logger logger = LoggerFactory.getLogger( SharedResourceInitializer.class );

	@Override
	public void contextInitialized( ServletContextEvent servletContextEvent ) {
		final String configurationFile = System.getProperty( "hefquin.configuration", "DefaultEngineConf.ttl" );
		final String federationFile = System.getProperty( "hefquin.federation", "DefaultFedConf.ttl" );

		logger.info( "--- Initialize engine ---" );
		logger.info( "hefquin.configuration: {}", configurationFile );
		logger.info( "hefquin.federation:    {}", federationFile );

		check( configurationFile );
		check( federationFile );

		final HeFQUINEngine engine = ServletUtils.getEngine( federationFile, configurationFile );
		servletContextEvent.getServletContext().setAttribute( "engine", engine );
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
