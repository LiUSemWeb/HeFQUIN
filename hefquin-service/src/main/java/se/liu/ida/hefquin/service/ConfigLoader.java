package se.liu.ida.hefquin.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

	@SuppressWarnings("resource")
	public static Properties load( final String propertiesFileName ) {
		final Properties properties = new Properties();

		// Try to load from root first
		InputStream resourceStream;
		try {
			resourceStream = new FileInputStream( propertiesFileName );
		}
		catch ( final FileNotFoundException e ) {
			resourceStream = ConfigLoader.class.getClassLoader().getResourceAsStream( propertiesFileName );
		}

		try {
			properties.load( resourceStream );
		}
		catch ( final IOException e ) {
			System.err.println( "Failed to load properties file: " + propertiesFileName );
			e.printStackTrace();
		}

		try {
			resourceStream.close();
		}
		catch ( final IOException e ) {
			System.err.println( "Failed to close properties file: " + propertiesFileName );
			e.printStackTrace();
		}

		return properties;
	}
}