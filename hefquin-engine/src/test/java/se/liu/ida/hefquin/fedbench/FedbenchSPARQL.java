package se.liu.ida.hefquin.fedbench;

import org.junit.BeforeClass;
import org.junit.Test;

public class FedbenchSPARQL extends FedbenchTestBase
{
	@BeforeClass
	public static void setUp() throws Exception {
		final String fedCat = "fedbench/FedConf.ttl";
		init(fedCat);
	}

	@Test
	public void executeQuery1() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/sparql/dbpedia/",
		                                      "http://localhost:8080/sparql/nyt/" };
		_executeQuery( "fedbench/cross-domain/q1", 250, values );
	}

	@Test
	public void executeQuery2() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/sparql/dbpedia/",
		                                      "http://localhost:8080/sparql/nyt/" };
		_executeQuery( "fedbench/cross-domain/q2", 350, values );
	}

	@Test
	public void executeQuery3() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/sparql/dbpedia/",
		                                      "http://localhost:8080/sparql/nyt/" };
		_executeQuery( "fedbench/cross-domain/q3", 350, values );
	}

	@Test
	public void executeQuery4() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/sparql/lmdb/",
		                                      "http://localhost:8080/sparql/nyt/" };
		_executeQuery( "fedbench/cross-domain/q4", 400, values );
	}

	@Test
	public void executeQuery5() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/sparql/dbpedia/",
		                                      "http://localhost:8080/sparql/lmdb/" };
		_executeQuery( "fedbench/cross-domain/q5", 330, values );
	}

	@Test
	public void executeQuery6() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/sparql/jamendo/",
		                                      "http://localhost:8080/sparql/geonames/" };
		_executeQuery( "fedbench/cross-domain/q6", 2500, values );
	}

	@Test
	public void executeQuery7() throws Exception {
		final String[] values = new String[]{ "http://localhost:8080/sparql/nyt/",
		                                      "http://localhost:8080/sparql/nyt/",
		                                      "http://localhost:8080/sparql/nyt/",
		                                      "http://localhost:8080/sparql/geonames/" };
		_executeQuery( "fedbench/cross-domain/q7", 1000, values );
	}
}
