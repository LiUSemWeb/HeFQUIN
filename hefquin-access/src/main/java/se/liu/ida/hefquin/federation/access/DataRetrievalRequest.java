package se.liu.ida.hefquin.federation.access;

import se.liu.ida.hefquin.base.query.ExpectedVariables;

public interface DataRetrievalRequest
{
	/**
	 * Returns the query variables for which this data retrieval request aims
	 * to fetch data (if any), where some of these variables may be 'certain
	 * variables' whereas others are 'possible variables'.
	 *
	 * For the distinction between these two types of variables, refer to
	 * {@link ExpectedVariables#getCertainVariables()} and to
	 * {@link ExpectedVariables#getPossibleVariables())}.
	 *
	 * For instance, if this request is about fetching triples that match a
	 * given triple pattern, then the variables of that triple pattern would
	 * be returned here as certain variables.
	 *
	 * Note that the method may also return null (which would be the case for
	 * types of data retrieval requests for which this method is not applicable
	 * in any reasonable way).
	 */
	ExpectedVariables getExpectedVariables();

	String toString();

}
