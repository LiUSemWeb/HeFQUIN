package se.liu.ida.hefquin.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.Test;

public class HeFQUINEngineConfigReaderTest
{
	@Test
	public void instantiateImplicitCtor() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass1' ."
		);

		final Object o = new HeFQUINEngineConfigReader().instantiate( r, createEmptyContext() );

		assertTrue( o != null );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyClass1 );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyInterface );
	}

	@Test
	public void instantiateEmptyCtor1() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ."
		);

		final Object o = new HeFQUINEngineConfigReader().instantiate( r, createEmptyContext() );

		assertTrue( o != null );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyClass2 );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyInterface );

		final HeFQUINEngineConfigReaderTest_DummyClass2 d = (HeFQUINEngineConfigReaderTest_DummyClass2) o;
		assertEquals( 42, d.i );
	}

	@Test
	public void instantiateEmptyCtor2() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ;" + System.lineSeparator()
			  + "      ec:constructorArguments () ."
		);

		final Object o = new HeFQUINEngineConfigReader().instantiate( r, createEmptyContext() );

		assertTrue( o != null );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyClass2 );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyInterface );

		final HeFQUINEngineConfigReaderTest_DummyClass2 d = (HeFQUINEngineConfigReaderTest_DummyClass2) o;
		assertEquals( 42, d.i );
	}

	@Test
	public void instantiateCtorArgs1() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ;" + System.lineSeparator()
			  + "      ec:constructorArguments (" + System.lineSeparator()
			  + "           [ rdf:value '2'^^xsd:integer ]" + System.lineSeparator()
			  + "           [ ec:argumentTypeName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyInterface' ;" + System.lineSeparator()
			  + "             ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ]" + System.lineSeparator()
			  + "      ) ."
		);

		final Object o = new HeFQUINEngineConfigReader().instantiate( r, createEmptyContext() );

		assertTrue( o != null );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyClass2 );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyInterface );

		final HeFQUINEngineConfigReaderTest_DummyClass2 d = (HeFQUINEngineConfigReaderTest_DummyClass2) o;
		assertEquals( 2, d.i );
		assertEquals( 42, d.sub.getInt() );
	}

	@Test
	public void instantiateCtorArgs2() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ;" + System.lineSeparator()
			  + "      ec:constructorArguments (" + System.lineSeparator()
			  //            the first constructor argument is given directly by its value
			  + "           '2'^^xsd:integer" + System.lineSeparator()
			  + "           [ ec:argumentTypeName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyInterface' ;" + System.lineSeparator()
			  + "             ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ]" + System.lineSeparator()
			  + "      ) ."
		);

		final Object o = new HeFQUINEngineConfigReader().instantiate( r, createEmptyContext() );

		assertTrue( o != null );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyClass2 );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyInterface );

		final HeFQUINEngineConfigReaderTest_DummyClass2 d = (HeFQUINEngineConfigReaderTest_DummyClass2) o;
		assertEquals( 2, d.i );
		assertEquals( 42, d.sub.getInt() );
	}

	@Test(expected = NoSuchMethodException.class)
	public void instantiateNoSuchConstructor1() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ;" + System.lineSeparator()
			  + "      ec:constructorArguments (" + System.lineSeparator()
			  //            type of the first constructor argument is wrong
			  + "           [ rdf:value '2'^^xsd:string ]" + System.lineSeparator()
			  + "           [ ec:argumentTypeName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyInterface' ;" + System.lineSeparator()
			  + "             ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ]" + System.lineSeparator()
			  + "      ) ."
		);

		new HeFQUINEngineConfigReader().instantiate( r, createEmptyContext() );
	}

	@Test(expected = NoSuchMethodException.class)
	public void instantiateNoSuchConstructor2() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ;" + System.lineSeparator()
			  + "      ec:constructorArguments (" + System.lineSeparator()
			  + "           [ rdf:value '2'^^xsd:integer ]" + System.lineSeparator()
			  //            second constructor argument missing here 
			  + "      ) ."
		);

		new HeFQUINEngineConfigReader().instantiate( r, createEmptyContext() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void instantiateWithIllegalConstructorArgs() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ;" + System.lineSeparator()
			  + "      ec:constructorArguments (" + System.lineSeparator()
			  + "           [ rdf:value '2'^^xsd:integer ]" + System.lineSeparator()
			  //            issue in the following argument: the class cannot be used for the expected type
			  + "           [ ec:argumentTypeName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyInterface' ;" + System.lineSeparator()
			  + "             ec:javaClassName 'java.lang.String' ]" + System.lineSeparator()
			  + "      ) ."
		);

		new HeFQUINEngineConfigReader().instantiate( r, createEmptyContext() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void instantiateWithUnknownDesignatedArgValue() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ;" + System.lineSeparator()
			  + "      ec:constructorArguments (" + System.lineSeparator()
			  //            There is no designated argument value with the following URI.
			  + "           [ rdf:value ec:Unknown ]" + System.lineSeparator()
			  + "      ) ."
		);

		new HeFQUINEngineConfigReader().instantiate( r, createEmptyContext() );
	}

	@Test
	public void instantiateWithDesignatedArgValue1() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ;" + System.lineSeparator()
			  + "      ec:constructorArguments (" + System.lineSeparator()
			  + "           [ rdf:value ec:value:ExecServiceForFedAccess ]" + System.lineSeparator()
			  + "      ) ."
		);

		final HeFQUINEngineConfigReader.ExtendedContext ctx = createNonEmptyContext();
		final Object o = new HeFQUINEngineConfigReader().instantiate(r, ctx);

		assertTrue( o != null );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyClass2 );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyInterface );

		final HeFQUINEngineConfigReaderTest_DummyClass2 d = (HeFQUINEngineConfigReaderTest_DummyClass2) o;
		assertEquals( 4321, d.i );
		assertEquals( ctx.getExecutorServiceForFederationAccess(), d.execService );
	}

	@Test
	public void instantiateWithDesignatedArgValue2() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
		final Resource r = parseAndCreateResource(
			  "PREFIX ec:  <http://w3id.org/hefquin/engineconf#>" + System.lineSeparator()
			  + "PREFIX ex:  <http://example.org/>" + System.lineSeparator()
			  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + System.lineSeparator()
			  + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + System.lineSeparator()
			  + System.lineSeparator()
			  + "ex:a  ec:javaClassName 'se.liu.ida.hefquin.engine.HeFQUINEngineConfigReaderTest_DummyClass2' ;" + System.lineSeparator()
			  + "      ec:constructorArguments (" + System.lineSeparator()
			  //            the constructor argument is given directly
			  + "           ec:value:ExecServiceForFedAccess" + System.lineSeparator()
			  + "      ) ."
		);

		final HeFQUINEngineConfigReader.ExtendedContext ctx = createNonEmptyContext();
		final Object o = new HeFQUINEngineConfigReader().instantiate(r, ctx);

		assertTrue( o != null );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyClass2 );
		assertTrue( o instanceof HeFQUINEngineConfigReaderTest_DummyInterface );

		final HeFQUINEngineConfigReaderTest_DummyClass2 d = (HeFQUINEngineConfigReaderTest_DummyClass2) o;
		assertEquals( 4321, d.i );
		assertEquals( ctx.getExecutorServiceForFederationAccess(), d.execService );
	}


	// ---- helper functions -----

	protected Resource parseAndCreateResource( final String turtle ) {
		final Model m = ModelFactory.createDefaultModel();

		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
		b.parse(m);

		return m.createResource("http://example.org/a");
	}

	protected HeFQUINEngineConfigReader.ExtendedContext createEmptyContext() {
		final HeFQUINEngineConfigReader.Context ctx = new HeFQUINEngineConfigReader.Context() {
			@Override
			public ExecutorService getExecutorServiceForFederationAccess() { throw new UnsupportedOperationException(); }
		};

		return new HeFQUINEngineConfigReader.ExtendedContext(ctx);
	}

	protected HeFQUINEngineConfigReader.ExtendedContext createNonEmptyContext() {
		final ExecutorService execService1 = Executors.newSingleThreadExecutor();

		final HeFQUINEngineConfigReader.Context ctx = new HeFQUINEngineConfigReader.Context() {
			@Override
			public ExecutorService getExecutorServiceForFederationAccess() { return execService1; }
		};

		return new HeFQUINEngineConfigReader.ExtendedContext(ctx);
	}

}
