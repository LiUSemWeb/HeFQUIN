package se.liu.ida.hefquin.service;

import java.io.IOException;
import java.util.List;

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
		final String confDescr = System.getProperty("hefquin.configuration", "config/DefaultConfDescr.ttl");
		final String fedCount = System.getProperty("hefquin.federation.count");
		final List<String> fedCatList = new java.util.ArrayList<>();
		
		logger.info( "--- Initialize engine ---" );
		logger.info( "hefquin.configuration:    {}", confDescr );
		
		if ( fedCount == null ) {
			final String fedCat = System.getProperty("hefquin.federation", "config/DefaultFedConf.ttl");
			fedCatList.add(fedCat);
			logger.info( "hefquin.federation:       {}", fedCat );
		}
		else {
			for ( int i = 0; i < Integer.parseInt(fedCount); i++ ) {
				fedCatList.add( System.getProperty("hefquin.federation." + (i + 1)) );
			}
			logger.info( "hefquin.federation.count: {}", fedCount );
			logger.info( "hefquin.federation.list:  {}", fedCatList );
		}

		check( confDescr );
		for ( final String fed : fedCatList ) {
			check(fed);
		}

		final HeFQUINEngine engine = new HeFQUINEngineBuilder()
			.withFederationCatalogInFiles(fedCatList)
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
