package se.liu.ida.hefquin.federation.access.impl.iface;

import se.liu.ida.hefquin.federation.access.TPFInterface;

public class TPFInterfaceUtils
{
	// TODO: these string should not be hard-coded but extract from the
	//       Hydra description returned in each response to a TPF request
	//       see: https://github.com/LiUSemWeb/HeFQUIN/issues/232
	public final static String httpQueryArgumentForSubject   = "subject";
	public final static String httpQueryArgumentForPredicate = "predicate";
	public final static String httpQueryArgumentForObject    = "object";

	/**
	 * Returns a {@link TPFInterface} object that represents the interface of
	 * a TPF server from which a TPF with the given URI can be retrieved.
	 */
	public static TPFInterface createTPFInterface( final String fragmentURI ) {
		return new TPFInterfaceImpl(fragmentURI, httpQueryArgumentForSubject, httpQueryArgumentForPredicate, httpQueryArgumentForObject);
	}
}
