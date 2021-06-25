package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface RequestProcessor<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
{
	DataRetrievalResponse performRequest( ReqType req, MemberType fm ) throws IOException;
}
