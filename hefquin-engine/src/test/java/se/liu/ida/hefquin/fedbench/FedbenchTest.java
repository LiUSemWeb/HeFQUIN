package se.liu.ida.hefquin.fedbench;

import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Evaluation based on FedBench.
 *
 * The evaluation is based on two federation configurations, Fed-I and Fed-II,
 * as defined by Heling and Acosta. The setup includes 9 datasets exposed through
 * heterogeneous interfaces (SPARQL, TPF, and brTPF). A total of 25 queries are
 * used from cross domain, life science, and linked data.
 * 
 * Source assignments were manually derived using the FedX source selection approach.
 * For each triple pattern in a query, FedX identifies the federation members
 * that can answer the pattern, thereby reducing the number of endpoints
 * involved in query evaluation and limiting unnecessary intermediate results.
 *
 * |------------|----------|---------|--------|---------|----------|--------|-------|-------|-------|
 * | Federation | GeoNames | DBpedia | ChEBI  | Jamendo | Drugbank | SWDF   | LMDB  | Kegg  | NYT   |
 * |------------|----------|---------|--------|---------|----------|--------|-------|-------|-------|
 * | Fed-I      | SPARQL   | SPARQL  | SPARQL | TPF     | TPF      | TPF    | brTPF | brTPF | brTPF |
 * | Fed-II     | TPF      | TPF     | TPF    | SPARQL  | SPARQL   | SPARQL | brTPF | brTPF | brTPF |
 * |------------|----------|---------|--------|---------|----------|--------|-------|-------|-------|
 * 
 * TODO: Include SP2B in the testing. 
 */

public class FedbenchTest extends FedbenchTestBase
{
	@BeforeClass
	public static void setUp() throws Exception {
		final String fedCat = "fedbench/FedbenchFedConf.ttl";
		init(fedCat);
	}

	// FED-I

	// Expected runtime: ~400 ms
	@Test
	public void fed1_cd_q1() throws Exception {
		_executeQuery( "fedbench/queries/fed1-cd-1.rq", "fedbench/results/cd-1.json" );
	}

	// Expected runtime: ~18000 ms
	@Test
	public void fed1_cd_q2() throws Exception {
		_executeQuery( "fedbench/queries/fed1-cd-2.rq", "fedbench/results/cd-2.json" );
	}

	// Expected runtime: ~24000 ms
	@Test
	public void fed1_cd_q3() throws Exception {
		_executeQuery( "fedbench/queries/fed1-cd-3.rq", "fedbench/results/cd-3.json" );
	}

	// Expected runtime: ~75000 ms
	@Test
	public void fed1_cd_q4() throws Exception {
		_executeQuery( "fedbench/queries/fed1-cd-4.rq", "fedbench/results/cd-4.json" );
	}

	// Expected runtime: ~85000 ms
	@Test
	public void fed1_cd_q5() throws Exception {
		_executeQuery( "fedbench/queries/fed1-cd-5.rq", "fedbench/results/cd-5.json" );
	}

	// Expected runtime: ~8700 ms
	@Test
	public void fed1_cd_q6() throws Exception {
		_executeQuery( "fedbench/queries/fed1-cd-6.rq", "fedbench/results/cd-6.json" );
	}

	// Expected runtime: ~26000 ms
	@Test
	public void fed1_cd_q7() throws Exception {
		_executeQuery( "fedbench/queries/fed1-cd-7.rq", "fedbench/results/cd-7.json" );
	}

	// Expected runtime: ~1300 ms
	@Test
	public void fed1_ls_q1() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ls-1.rq", "fedbench/results/ls-1.json" );
	}

	// Expected runtime: ~700 ms
	@Test
	public void fed1_ls_q2() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ls-2.rq", "fedbench/results/ls-2.json" );
	}

	// Expected runtime: ~130000 ms
	@Test
	public void fed1_ls_q3() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ls-3.rq", "fedbench/results/ls-3.json" );
	}

	// Expected runtime: ~60000 ms
	@Test
	public void fed1_ls_q4() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ls-4.rq", "fedbench/results/ls-4.json" );
	}

	// Expected runtime: ~205000 ms
	@Test
	public void fed1_ls_q5() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ls-5.rq", "fedbench/results/ls-5.json" );
	}

	// Expected runtime: ~94000 ms
	@Test
	public void fed1_ls_q6() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ls-6.rq", "fedbench/results/ls-6.json" );
	}

	// java.lang.Exception: Query failed with exceptions:
	// Exception occurred when outputting the result of a SELECT query using the
	// Jena machinery.
	// java.lang.NullPointerException: Cannot invoke
	// "se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.getValue()"
	// because "crd" is null
	// Expected runtime: ~? ms
	// @Test
	public void fed1_ls_q7() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ls-7.rq", "fedbench/results/ls-7.json" );
	}

	// Expected runtime: ~3300 ms
	@Test
	public void fed1_ld_q1() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-1.rq", "fedbench/results/ld-1.json" );
	}

	// Expected runtime: ~1000 ms
	@Test
	public void fed1_ld_q2() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-2.rq", "fedbench/results/ld-2.json" );
	}

	// Expected runtime: ~3000 ms
	@Test
	public void fed1_ld_q3() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-3.rq", "fedbench/results/ld-3.json" );
	}

	// Expected runtime: ~2800 ms
	@Test
	public void fed1_ld_q4() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-4.rq", "fedbench/results/ld-4.json" );
	}

	// Expected runtime: ~500 ms
	@Test
	public void fed1_ld_q5() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-5.rq", "fedbench/results/ld-5.json" );
	}

	// Expected runtime: ~88000 ms
	@Test
	public void fed1_ld_q6() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-6.rq", "fedbench/results/ld-6.json" );
	}

	// Expected runtime: ~500 ms
	@Test
	public void fed1_ld_q7() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-7.rq", "fedbench/results/ld-7.json" );
	}

	// Expected runtime: ~1700 ms
	@Test
	public void fed1_ld_q8() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-8.rq", "fedbench/results/ld-8.json" );
	}

	// Expected runtime: ~300 ms
	@Test
	public void fed1_ld_q9() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-9.rq", "fedbench/results/ld-9.json" );
	}

	// Expected runtime: ~7000 ms
	@Test
	public void fed1_ld_q10() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-10.rq", "fedbench/results/ld-10.json" );
	}

	// Expected runtime: ~ ms
	@Test
	public void fed1_ld_q11() throws Exception {
		_executeQuery( "fedbench/queries/fed1-ld-11.rq", "fedbench/results/ld-11.json" );
	}
}
