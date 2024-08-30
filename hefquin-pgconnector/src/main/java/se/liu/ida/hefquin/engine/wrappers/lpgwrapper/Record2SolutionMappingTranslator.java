package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.List;
import java.util.Map;

public interface Record2SolutionMappingTranslator {

    /**
     * Translates a single {@link TableRecord} object into a collection of {@link SolutionMapping}
     */
    SolutionMapping translateRecord(final TableRecord record, final LPG2RDFConfiguration conf,
                                    final CypherQuery query, final Map<CypherVar, Var> varMap);

    List<SolutionMapping> translateRecords(final List<TableRecord> records, final LPG2RDFConfiguration conf,
                                           final CypherQuery query, final Map<CypherVar, Var> varMap);
}
