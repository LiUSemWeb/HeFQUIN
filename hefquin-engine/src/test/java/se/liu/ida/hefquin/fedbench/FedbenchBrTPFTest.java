package se.liu.ida.hefquin.fedbench;

import org.junit.BeforeClass;
import org.junit.Test;

public class FedbenchBrTPFTest extends FedbenchTestBase
{
	@BeforeClass
	public static void setUp() throws Exception {
		final String fedCat = "fedbench/FedbenchFedConf.ttl";
		init(fedCat);
		DEFAULT_TOLERANCE = 0.5;
		DEFAULT_SLACK = 100;
	}

	@Test
	public void executeQuery1() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/brtpf/dbpedia",
			"http://localhost:8080/brtpf/nyt",
		    "http://localhost:8080/sparql/nyt/"
		};
		_executeQuery( "fedbench/cross-domain/q1", 400, values );
	}

	@Test
	public void executeQuery2() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/brtpf/dbpedia",
			"http://localhost:8080/brtpf/nyt",
			"http://localhost:8080/brtpf/nyt"
		};
		_executeQuery( "fedbench/cross-domain/q2", 16_000, values );
	}

	// Query takes too long, check generated plan
	// @Test
	public void executeQuery3() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/brtpf/dbpedia",
			"http://localhost:8080/brtpf/dbpedia",
			"http://localhost:8080/brtpf/dbpedia",
			"http://localhost:8080/brtpf/nyt",
			"http://localhost:8080/brtpf/nyt"
		};
		_executeQuery( "fedbench/cross-domain/q3", 0, values );
	}

	// Query takes too long, check generated plan
	// @Test
	public void executeQuery4() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/brtpf/lmdb",
			"http://localhost:8080/brtpf/lmdb",
			"http://localhost:8080/brtpf/lmdb",
		    "http://localhost:8080/brtpf/nyt",
		    "http://localhost:8080/brtpf/nyt"
		};
		_executeQuery( "fedbench/cross-domain/q4", 60_000, values );
	}

	// >460 seconds
	// @Test
	public void executeQuery5() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/brtpf/dbpedia",
			"http://localhost:8080/brtpf/dbpedia",
			"http://localhost:8080/brtpf/lmdb",
			"http://localhost:8080/brtpf/lmdb"
		};
		_executeQuery( "fedbench/cross-domain/q5", 0, values );
	}

	@Test
	public void executeQuery6() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/brtpf/jamendo",
			"http://localhost:8080/brtpf/jamendo",
			"http://localhost:8080/brtpf/geonames",
			"http://localhost:8080/brtpf/geonames"
		};
		_executeQuery( "fedbench/cross-domain/q6", 60_000, values );
	}

	@Test
	public void executeQuery7() throws Exception {
		final String[] values = new String[]{
			// first
			"http://localhost:8080/brtpf/nyt",
			"http://localhost:8080/brtpf/geonames",
			"http://localhost:8080/brtpf/nyt",
			"http://localhost:8080/brtpf/nyt",
			// second
			"http://localhost:8080/brtpf/nyt",
			"http://localhost:8080/brtpf/nyt",
			"http://localhost:8080/brtpf/nyt",
			"http://localhost:8080/brtpf/nyt"
		};
		_executeQuery( "fedbench/cross-domain/q7", 25_000, values );
	}
}
