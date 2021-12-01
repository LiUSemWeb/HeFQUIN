package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;

import java.util.List;

public interface Record2SolutionMappingTranslator {

    /**
     *
     */
    List<SolutionMapping> translateRecord(final TableRecord record, final LPG2RDFConfiguration conf);

}
