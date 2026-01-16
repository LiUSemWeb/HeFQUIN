package se.liu.ida.hefquin.fedbench;

import java.lang.annotation.Native;

import org.apache.jena.Jena;
import org.junit.BeforeClass;
import org.junit.Test;

public class FedbenchSPARQLTest extends FedbenchTestBase
{
	@BeforeClass
	public static void setUp() throws Exception {
		final String fedCat = "fedbench/FedbenchFedConf.ttl";
		init(fedCat);
		DEFAULT_TOLERANCE = 0.5;
		DEFAULT_SLACK = 100;
	}

	// cross-domain queries

	@Test
	public void crossDomainQ1() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/dbpedia/",
		    "http://localhost:8080/sparql/nyt/",
		    "http://localhost:8080/sparql/nyt/"
		};
		_executeQuery( "fedbench/cross-domain/q1", 300, values );
	}

	@Test
	public void crossDomainQ2() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/dbpedia/",
			"http://localhost:8080/sparql/nyt/",
			"http://localhost:8080/sparql/nyt/"
		};
		_executeQuery( "fedbench/cross-domain/q2", 500, values );
	}

	@Test
	public void crossDomainQ3() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/dbpedia/",
			"http://localhost:8080/sparql/dbpedia/",
			"http://localhost:8080/sparql/dbpedia/",
		    "http://localhost:8080/sparql/nyt/",
		    "http://localhost:8080/sparql/nyt/"
		};
		_executeQuery( "fedbench/cross-domain/q3", 400, values );
	}

	@Test
	public void crossDomainQ4() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/lmdb/",
			"http://localhost:8080/sparql/lmdb/",
			"http://localhost:8080/sparql/lmdb/",
		    "http://localhost:8080/sparql/nyt/",
		    "http://localhost:8080/sparql/nyt/"
		};
		_executeQuery( "fedbench/cross-domain/q4", 400, values );
	}

	@Test
	public void crossDomainQ5() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/dbpedia/",
			"http://localhost:8080/sparql/dbpedia/",
		    "http://localhost:8080/sparql/lmdb/",
		    "http://localhost:8080/sparql/lmdb/"
		};
		_executeQuery( "fedbench/cross-domain/q5", 350, values );
	}

	@Test
	public void crossDomainQ6() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/jamendo/",
			"http://localhost:8080/sparql/jamendo/",
		    "http://localhost:8080/sparql/geonames/",
		    "http://localhost:8080/sparql/geonames/"
		};
		_executeQuery( "fedbench/cross-domain/q6", 2700, values );
	}

	@Test
	public void crossDomainQ7() throws Exception {
		final String[] values = new String[]{
			// first
			"http://localhost:8080/sparql/nyt/",
		    "http://localhost:8080/sparql/geonames/",
		    "http://localhost:8080/sparql/nyt/",
		    "http://localhost:8080/sparql/nyt/",
			// second
			"http://localhost:8080/sparql/nyt/",
			"http://localhost:8080/sparql/nyt/",
		    "http://localhost:8080/sparql/nyt/",
		    "http://localhost:8080/sparql/nyt/"
		};
		_executeQuery( "fedbench/cross-domain/q7", 1500, values );
	}

	// lifescience-domain queries

	@Test
	public void lifescienceDomainQ1() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/drugbank/"
		};
		_executeQuery( "fedbench/lifescience-domain/q1", 900, values );
	}

	@Test
	public void lifescienceDomainQ2() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/dbpedia/"
			
		};
		_executeQuery( "fedbench/lifescience-domain/q2", 700, values );
	}

	// @Test
	public void lifescienceDomainQ3() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/dbpedia/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/drugbank/"
		};
		_executeQuery( "fedbench/lifescience-domain/q3", 17_000, values );
	}

	@Test
	public void lifescienceDomainQ4() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/kegg/",
			"http://localhost:8080/sparql/kegg/",
			"http://localhost:8080/sparql/kegg/",
			"http://localhost:8080/sparql/kegg/"
		};
		_executeQuery( "fedbench/lifescience-domain/q4", 700, values );
	}

	@Test
	public void lifescienceDomainQ5() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/kegg/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/chebi/",
			"http://localhost:8080/sparql/chebi/"
		};
		_executeQuery( "fedbench/lifescience-domain/q5", 5_000, values );
	}

	@Test
	public void lifescienceDomainQ6() throws Exception {
		final String[] values = new String[]{
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/kegg/",
			"http://localhost:8080/sparql/kegg/",
			"http://localhost:8080/sparql/kegg/"
		};
		_executeQuery( "fedbench/lifescience-domain/q6", 800, values );
	}

	// java.lang.Exception: Query failed with exceptions:
	// Exception occurred when outputting the result of a SELECT query using the Jena machinery.
	// java.lang.NullPointerException: Cannot invoke "se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.getValue()" because "crd" is null
	// @Test
	public void lifescienceDomainQ7() throws Exception {
		final String[] values = new String[]{
			// first
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/drugbank/",
			"http://localhost:8080/sparql/kegg/",
			"http://localhost:8080/sparql/kegg/",
			"http://localhost:8080/sparql/drugbank/"
		};
		_executeQuery( "fedbench/lifescience-domain/q7", 20000, values );
	}
}
