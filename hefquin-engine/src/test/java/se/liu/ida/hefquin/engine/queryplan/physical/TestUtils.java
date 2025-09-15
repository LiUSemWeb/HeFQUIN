package se.liu.ida.hefquin.engine.queryplan.physical;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.BRTPFInterface;
import se.liu.ida.hefquin.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.federation.access.TPFInterface;
import se.liu.ida.hefquin.federation.access.impl.iface.BRTPFInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.iface.TPFInterfaceImpl;

public class TestUtils {
		// ---- helper functions -----
	
	public LogicalOpGPAdd getLogicalOpGPAdd( final SPARQLGraphPattern pattern ){
		final FederationMember fm = new SPARQLEndpointForTest();
		return new LogicalOpGPAdd(fm, pattern);
	}

	protected static ExpectedVariables getExpectedVariables( final List<String> certainVars,
	                                                         final List<String> possibleVars )
	{
		return new ExpectedVariables() {

			@Override
			public Set<Var> getCertainVariables() {
				return certainVars.stream().map(Var::alloc).collect(Collectors.toSet());
			}
			
			@Override
			public Set<Var> getPossibleVariables() {
				return possibleVars.stream().map(Var::alloc).collect(Collectors.toSet());
			}
			
		};
	}

	protected static VocabularyMapping getVocabularyMappingForTest() {
		return new VocabularyMapping() {

			@Override
			public SPARQLGraphPattern translateTriplePattern( TriplePattern tp ) {
				throw new UnsupportedOperationException("Unimplemented method 'translateTriplePattern'");
			}

			@Override
			public Set<SolutionMapping> translateSolutionMapping( SolutionMapping sm ) {
				throw new UnsupportedOperationException("Unimplemented method 'translateSolutionMapping'");
			}

			@Override
			public Set<SolutionMapping> translateSolutionMappingFromGlobal( SolutionMapping sm ) {
				throw new UnsupportedOperationException("Unimplemented method 'translateSolutionMappingFromGlobal'");
			}

			@Override
			public boolean isEquivalenceOnly() {
				throw new UnsupportedOperationException("Unimplemented method 'isEquivalenceOnly'");
			}
		};
	}

	protected static class SPARQLEndpointForTest implements SPARQLEndpoint
	{
		final SPARQLEndpointInterface iface;

		public SPARQLEndpointForTest() { this("http://example.org/sparql"); }

		public SPARQLEndpointForTest( final String ifaceURL ) {
			iface = new SPARQLEndpointInterfaceImpl(ifaceURL);
		}

		@Override
		public SPARQLEndpointInterface getInterface() { return iface; }

		@Override
		public VocabularyMapping getVocabularyMapping() { return null; }

	}

	protected static class TPFServerForTest implements TPFServer
	{
		protected final TPFInterface iface = new TPFInterfaceImpl("http://example.org/", "subject", "predicate", "object");

		public TPFServerForTest() { }

		@Override
		public TPFInterface getInterface() { return iface; }

		@Override
		public VocabularyMapping getVocabularyMapping() { return null; }
	}

	protected static class BRTPFServerForTest implements BRTPFServer
	{
		final BRTPFInterface iface = new BRTPFInterfaceImpl("http://example.org/", "subject", "predicate", "object", "values");

		public BRTPFServerForTest() { }

		@Override
		public BRTPFInterface getInterface() { return iface; }

		@Override
		public VocabularyMapping getVocabularyMapping() { return null; }
	}

}
