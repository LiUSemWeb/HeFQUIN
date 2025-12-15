package se.liu.ida.hefquin.fedbench;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class FedbenchTPFTest extends FedbenchTestBase
{
	@BeforeClass
	public static void setUp() throws Exception {
		final String fedCat = "fedbench/TPFFedConf.ttl";
		init(fedCat);
	}

	@Test
	public void executeQuery1() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/ldf/dbpedia",
		                                      "http://localhost:8080/ldf/nyt" };
		_executeQuery( "fedbench/cross-domain/q1", 250, values );
	}

	@Test
	public void executeQuery2() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/ldf/dbpedia",
		                                      "http://localhost:8080/ldf/nyt" };
		_executeQuery( "fedbench/cross-domain/q2", 350, values );
	}

	@Test
	public void executeQuery3() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/ldf/dbpedia",
		                                      "http://localhost:8080/ldf/nyt" };
		_executeQuery( "fedbench/cross-domain/q3", 20_000, values );
	}

	@Test
	public void executeQuery4() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/ldf/lmdb",
		                                      "http://localhost:8080/ldf/nyt" };
		_executeQuery( "fedbench/cross-domain/q4", 400, values );
	}

	@Test
	public void executeQuery5() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/ldf/dbpedia",
		                                      "http://localhost:8080/ldf/lmdb" };
		_executeQuery( "fedbench/cross-domain/q5", 5_000, values );
	}

	@Test
	public void executeQuery6() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/ldf/jamendo",
		                                      "http://localhost:8080/ldf/geonames" };
		_executeQuery( "fedbench/cross-domain/q6", 10_000, values );
	}

	@Test
	public void executeQuery7() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/ldf/nyt",
		                                      "http://localhost:8080/ldf/nyt",
		                                      "http://localhost:8080/ldf/nyt",
		                                      "http://localhost:8080/ldf/geonames" };
		_executeQuery( "fedbench/cross-domain/q7", 1000, values );
	}
}
