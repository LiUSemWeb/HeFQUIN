package se.liu.ida.hefquin.federation.access;

public interface BRTPFRequest extends BindingsRestrictedTriplePatternRequest
{
	/**
	 * Since responses of brTPF servers are paginated,
	 * every request must include a page number.
	 */
	int getPageNumber();
}
