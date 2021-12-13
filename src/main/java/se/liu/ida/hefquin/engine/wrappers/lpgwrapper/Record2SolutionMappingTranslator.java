package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherVarGenerator;

import java.util.List;
import java.util.Map;

public interface Record2SolutionMappingTranslator {

    /**
     *
     */
    List<SolutionMapping> translateRecord(final TableRecord record, final LPG2RDFConfiguration conf,
                                          final CypherQuery query, final Map<CypherVar, Node> varMap);

    List<SolutionMapping> translateRecords(final List<TableRecord> records, final LPG2RDFConfiguration conf,
                                           final CypherQuery query, final Map<CypherVar, Node> varMap);
}
