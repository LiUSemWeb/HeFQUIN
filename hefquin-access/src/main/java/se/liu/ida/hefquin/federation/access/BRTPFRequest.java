package se.liu.ida.hefquin.federation.access;

public interface BRTPFRequest extends BindingsRestrictedTriplePatternRequest
{
	/**
	 * If this request is for a specific page of the requested TPF,
	 * then this method returns the URL from which that page can be
	 * retrieved. Otherwise, this method returns <code>null</code>,
	 * which means that the request is for the first page.
	 * 
	 * The URL for a specific page (as may be returned by this method)
	 * is typically mentioned in the metadata of the previous page
	 * (see {@link TPFResponse#getNextPageURL()}).
	 */
	String getPageURL();
}
