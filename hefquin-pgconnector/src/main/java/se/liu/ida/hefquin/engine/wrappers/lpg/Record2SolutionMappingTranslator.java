package se.liu.ida.hefquin.engine.wrappers.lpg;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.CypherVar;

import java.util.List;
import java.util.Map;

public interface Record2SolutionMappingTranslator
{
	/**
	 * Translates a single {@link TableRecord} into a SPARQL solution mapping
	 * that is represented as a {@link Binding}.
	 */
	Binding translateRecord( TableRecord record,
	                         LPG2RDFConfiguration conf,
	                         CypherQuery query,
	                         Map<CypherVar, Var> varMap );

	default Binding translateRecord( final TableRecord record,
	                                 final LPG2RDFConfiguration conf,
	                                 final SPARQL2CypherTranslationResult tRes ) {
		return translateRecord( record, conf, tRes.getCypherQuery(), tRes.getVariablesMapping() );
	}

	List<Binding> translateRecords( List<TableRecord> records,
	                                LPG2RDFConfiguration conf,
	                                CypherQuery query,
	                                Map<CypherVar, Var> varMap );

	default List<Binding> translateRecords( final List<TableRecord> records,
	                                        final LPG2RDFConfiguration conf,
	                                        final SPARQL2CypherTranslationResult tRes ) {
		return translateRecords( records, conf, tRes.getCypherQuery(), tRes.getVariablesMapping() );
	}

}
