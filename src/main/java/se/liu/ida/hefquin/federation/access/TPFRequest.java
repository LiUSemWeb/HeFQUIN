package se.liu.ida.hefquin.federation.access;

public interface TPFRequest extends TriplePatternRequest
{
	/**
	 * Since responses of TPF servers are paginated,
	 * every request must include a page number.
	 */
	int getPageNumber();
}
