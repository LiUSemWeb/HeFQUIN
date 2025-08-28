package se.liu.ida.hefquin.base.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides build-time metadata (name, version, url) and a constructed
 * User-Agent string. Values are loaded once from project-info.properties
 * on the classpath.
 */
public final class BuildInfo
{

	private static final String DEFAULT_NAME    = "HeFQUIN";
	private static final String DEFAULT_VERSION = "unknown-version";
	private static final String DEFAULT_URL     = "https://liusemweb.github.io/HeFQUIN/";

	private static final Properties props = new Properties();

	static {
		try ( final InputStream in = BuildInfo.class.getResourceAsStream("/project-info.properties") ) {
			if ( in != null ) {
				props.load( in );
			}
		} catch ( IOException e ) {
			// optionally log
		}
	}

	public static String getName() {
		return props.getProperty("name", DEFAULT_NAME);
	}

	public static String getVersion() {
		return props.getProperty("version", DEFAULT_VERSION);
	}

	public static String getUrl() {
		return props.getProperty("url", DEFAULT_URL);
	}

	public static String getUserAgent() {
		return getName() + "/" + getVersion() + " (" + getUrl() + ")";
	}

	public static void main( String[] args ) {
		System.out.println( BuildInfo.props);
		System.out.println( "Name: " + getName() );
		System.out.println( "Version: " + getVersion() );
		System.out.println( "URL: " + getUrl() );
		System.out.println( "User-Agent: " + getUserAgent() );
	}
}