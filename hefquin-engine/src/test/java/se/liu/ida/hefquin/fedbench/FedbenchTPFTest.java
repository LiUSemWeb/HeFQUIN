package se.liu.ida.hefquin.fedbench;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class FedbenchTPFTest extends FedbenchTestBase
{
	@BeforeClass
	public static void setUp() throws Exception {
		final String fedCat = "fedbench/FedbenchFedConf.ttl";
		init(fedCat);
	}

	@Test
	public void executeQuery1() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/tpf/dbpedia",
		                                      "http://localhost:8080/tpf/nyt" };
		_executeQuery( "fedbench/cross-domain/q1", 1000, values );
	}

	@Test
	public void executeQuery2() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/tpf/dbpedia",
		                                      "http://localhost:8080/tpf/nyt" };
		_executeQuery( "fedbench/cross-domain/q2", 1500, values );
	}

	@Test
	public void executeQuery3() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/tpf/dbpedia",
		                                      "http://localhost:8080/tpf/nyt" };
		_executeQuery( "fedbench/cross-domain/q3", 60_000, values );
	}

	@Test
	public void executeQuery4() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/tpf/lmdb",
		                                      "http://localhost:8080/tpf/nyt" };
		_executeQuery( "fedbench/cross-domain/q4", 400, values );
	}

	@Test
	public void executeQuery5() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/tpf/dbpedia",
		                                      "http://localhost:8080/tpf/lmdb" };
		_executeQuery( "fedbench/cross-domain/q5", 5_000, values );
	}

	@Test
	public void executeQuery6() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/tpf/jamendo",
		                                      "http://localhost:8080/tpf/geonames" };
		_executeQuery( "fedbench/cross-domain/q6", 10_000, values );
	}

	@Test
	public void executeQuery7() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/tpf/nyt",
		                                      "http://localhost:8080/tpf/nyt",
		                                      "http://localhost:8080/tpf/nyt",
		                                      "http://localhost:8080/tpf/geonames" };
		_executeQuery( "fedbench/cross-domain/q7", 1000, values );
	}
}
