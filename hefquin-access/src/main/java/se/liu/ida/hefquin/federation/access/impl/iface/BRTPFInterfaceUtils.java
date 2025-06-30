package se.liu.ida.hefquin.federation.access.impl.iface;

import se.liu.ida.hefquin.federation.access.BRTPFInterface;

public class BRTPFInterfaceUtils
{
	// TODO: these strings should not be hard-coded but extracted from the
	//       Hydra description returned in each response to a brTPF request
	//       see: https://github.com/LiUSemWeb/HeFQUIN/issues/233
	public final static String httpQueryArgumentForSubject   = TPFInterfaceUtils.httpQueryArgumentForSubject;
	public final static String httpQueryArgumentForPredicate = TPFInterfaceUtils.httpQueryArgumentForPredicate;
	public final static String httpQueryArgumentForObject    = TPFInterfaceUtils.httpQueryArgumentForObject;
	public final static String httpQueryArgumentForBindings  = "values";

	/**
	 * Returns a {@link BRTPFInterface} object that represents the interface of
	 * a TPF server from which a TPF with the given URI can be retrieved.
	 */
	public static BRTPFInterface createBRTPFInterface( final String fragmentURI ) {
		return new BRTPFInterfaceImpl(fragmentURI, httpQueryArgumentForSubject, httpQueryArgumentForPredicate, httpQueryArgumentForObject, httpQueryArgumentForBindings);
	}

}
