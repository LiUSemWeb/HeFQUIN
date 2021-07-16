package se.liu.ida.hefquin.engine.utils.lpg;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.MatchCypherQuery;
import se.liu.ida.hefquin.engine.query.impl.UnionCypherQuery;

import java.net.IDN;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SPARQLStar2CypherTranslator {

    public static CypherQuery translate(final BGP bgp ) {
        return translate(bgp, new DefaultConfiguration(), new CypherVarGenerator());
    }

    public static CypherQuery translate( final BGP bgp, final Configuration configuration) {
        return translate(bgp, configuration, new CypherVarGenerator());
    }

    public static CypherQuery translate(final BGP bgp, final Configuration configuration,
                                        final CypherVarGenerator gen) {
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        if (triples.size() == 1) {
            return translateTriple((TriplePattern) triples.toArray()[0], configuration, gen);
        }
        return translateBGP(bgp);
    }

    private static CypherQuery translateBGP( final BGP bgp ) {
        final Set<String> matches = new HashSet<>();
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        final Set<String> returns = new HashSet<>();
        final Set<String> conditions = new HashSet<>();
        return null;
    }

    private static CypherQuery translateTriple(final TriplePattern pattern, final Configuration configuration,
                                          final CypherVarGenerator gen) {
        final Triple b = pattern.asJenaTriple();
        final Node s = b.getSubject();
        final Node p = b.getPredicate();
        final Node o = b.getObject();
        if (pattern.numberOfVars() == 1){
            if (s.isVariable()) {
                if (configuration.mapsToProperty(p) && o.isLiteral()) {
                    return Translations.getVarPropertyLiteral(s, p, o, configuration, gen);
                } else if (configuration.isLabelIRI(p) && configuration.mapsToLabel(o)) {
                    return Translations.getVarLabelClass(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p) && configuration.mapsToNode(o)){
                    return Translations.getVarRelationshipNode(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Illegal values for predicate and object");
                }
            }
            else if (o.isVariable()) {
                if (configuration.isLabelIRI(p) && configuration.mapsToNode(s)) {
                    return Translations.getNodeLabelVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToProperty(p) && configuration.mapsToNode(s)) {
                    return Translations.getNodePropertyVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p) && configuration.mapsToNode(s)) {
                    return Translations.getNodeRelationshipVar(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Illegal values for subject and predicate");
                }
            }
            else if (p.isVariable()) {
                if (configuration.mapsToNode(s) && configuration.mapsToNode(o)) {
                    return Translations.getNodeVarNode(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(s) && o.isLiteral()) {
                    return Translations.getNodeVarLiteral(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(s) && configuration.mapsToLabel(o)) {
                    return Translations.getNodeVarLabel(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Illegal values for subject and object");
                }
            }
        } else if (pattern.numberOfVars() == 2) {
            if (s.isVariable() && o.isVariable()) {
                if (configuration.isLabelIRI(p)) {
                    return Translations.getVarLabelVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p)) {
                    return Translations.getVarRelationshipVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToProperty(p)) {
                    return Translations.getVarPropertyVar(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Predicate must be a mapping of a property or a relationship " +
                            "or the label URI");
                }
            } else if (s.isVariable() && p.isVariable()) {
                if (configuration.mapsToLabel(o)) {
                    return Translations.getVarVarLabel(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(o)) {
                    return Translations.getVarVarNode(s, p, o, configuration, gen);
                } else if (o.isLiteral()) {
                    return Translations.getVarVarLiteral(s, p, o, configuration, gen);
                }
            } else if(p.isVariable() && o.isVariable()) {
                if (configuration.mapsToNode(s)) {
                    return Translations.getNodeVarVar(s, p, o, configuration, gen);
                }
            }
        } else {
            return Translations.getVarVarVar(s, p, o, configuration, gen);
        }
        return null;
    }

    private static class Translations {

        final static private String MATCH_EDGE              = "MATCH (%s)-[:%s]->(%s)";
        final static private String MATCH_EDGE_VAR          = "MATCH (%s)-[%s]->(%s)";
        final static private String MATCH_NODE              = "MATCH (%s)";
        final static private String CONDITION_EXISTS        = "EXISTS(%s.%s)";
        final static private String CONDITION_VALUE         = "%s.%s='%s'";
        final static private String CONDITION_CLASS         = "%s:%s" ;
        final static private String CONDITION_ID            = "ID(%s)=%s";
        final static private String RETURN_PATTERN          = "%s AS %s";
        final static private String RETURN_LITERAL_PATTERN  = "%s.%s AS %s";
        final static private String RETURN_NODE_MAPPING     = "nm(%s) AS %s";
        final static private String RETURN_EDGE_MAPPING     = "elm(%s) AS %s";
        final static private String RETURN_LABELS           = "labels(%s) AS %s";
        final private static String RETURN_KEYS             = "[k in KEYS(%s) | pm(k)] AS %s";
        final private static String RETURN_KEYS_VALUES      = "[k in KEYS(%s) | %s[k]] AS %s";
        final private static String RETURN_KEYS_WITH_VALUE  = "[k in KEYS(%s) WHERE %s[k]='%s' | pm(k)] AS %s";

        public static CypherQuery getVarPropertyLiteral(final Node s, final Node p, final Node o,
                                                   final Configuration configuration, final CypherVarGenerator gen) {
            final String property = configuration.unmapProperty(p);
            final String literal = o.getLiteralValue().toString();
            final String svar = gen.getVarFor(s.getName());
            final String xvar = gen.getAnonVar();
            final String evar = gen.getAnonVar();
            final String yvar = gen.getAnonVar();
            return new UnionCypherQuery(
                    new MatchCypherQuery(new String[]{String.format(MATCH_NODE, svar)},
                            new String[]{String.format(CONDITION_VALUE, svar, property, literal)} ,
                            new String[]{
                                String.format(RETURN_NODE_MAPPING, svar, "r1"), "'' AS r2", "'' AS r3"} ),
                    new MatchCypherQuery(new String[]{String.format(MATCH_EDGE_VAR, xvar, evar, yvar)},
                            new String[]{String.format(CONDITION_VALUE, evar, property, literal)},
                            new String[]{
                                String.format(RETURN_NODE_MAPPING, xvar, "r1"),
                                String.format(RETURN_EDGE_MAPPING, evar, "r2"),
                                String.format(RETURN_NODE_MAPPING, yvar, "r3")}));
        }

        public static CypherQuery getVarLabelClass(final Node s, final Node p, final Node o,
                                              final Configuration configuration, final CypherVarGenerator gen) {
            final String clazz = configuration.unmapLabel(o);
            final String svar = gen.getVarFor(s.getName());
            return new MatchCypherQuery(new String[]{String.format(MATCH_NODE, svar)},
                    new String[]{String.format(CONDITION_CLASS, svar, clazz)},
                    new String[]{String.format(RETURN_NODE_MAPPING, svar, s.getName())});
        }

        public static CypherQuery getVarRelationshipNode(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final String relationship = configuration.unmapRelationship(p);
            final String nodeID = configuration.unmapNode(o);
            final String svar = gen.getVarFor(s.getName());
            final String ovar = gen.getAnonVar();
            return new MatchCypherQuery(new String[]{String.format(MATCH_EDGE, svar, relationship, ovar)},
                    new String[]{String.format(CONDITION_ID, ovar, nodeID)},
                    new String[]{String.format(RETURN_NODE_MAPPING, svar, s.getName())});
        }

        public static CypherQuery getNodeLabelVar(final Node s, final Node p, final Node o,
                                             final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(o.getName());
            return new MatchCypherQuery(new String[]{String.format(MATCH_NODE, svar)},
                    new String[]{String.format(CONDITION_ID, svar, configuration.unmapNode(s))},
                    new String[]{String.format(RETURN_LABELS, svar, o.getName())});
        }

        public static CypherQuery getNodePropertyVar(final Node s, final Node p, final Node o,
                                                final Configuration configuration, final CypherVarGenerator gen) {
            final String property = configuration.unmapProperty(p);
            final String var = gen.getAnonVar();
            return new MatchCypherQuery(new String[]{String.format(MATCH_NODE, var)},
                    new String[]{String.format(CONDITION_ID, var, configuration.unmapNode(s)),
                        String.format(CONDITION_EXISTS, var, property)},
                    new String[]{String.format(RETURN_LITERAL_PATTERN, var, property, o.getName())});
        }

        public static CypherQuery getNodeRelationshipVar(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            final String yvar = gen.getVarFor(o.getName());
            return new MatchCypherQuery(
                    new String[]{String.format(MATCH_EDGE, xvar, configuration.unmapRelationship(p), yvar)},
                    new String[]{String.format(CONDITION_ID, xvar, configuration.unmapNode(s))},
                    new String[]{String.format(RETURN_NODE_MAPPING, yvar, o.getName())});
        }

        public static CypherQuery getNodeVarNode(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            final String evar = gen.getVarFor(p.getName());
            final String yvar = gen.getAnonVar();
            return new MatchCypherQuery(new String[]{String.format(MATCH_EDGE_VAR, xvar, evar, yvar)},
                    new String[]{String.format(CONDITION_ID, xvar, configuration.unmapNode(s)),
                            String.format(CONDITION_ID, yvar, configuration.unmapNode(o))},
                    new String[]{String.format(RETURN_EDGE_MAPPING, evar, p.getName())});
        }

        public static CypherQuery getNodeVarLiteral(final Node s, final Node p, final Node o,
                                               final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            return new MatchCypherQuery(new String[]{String.format(MATCH_NODE, xvar)},
                    new String[]{String.format(CONDITION_ID, xvar, configuration.unmapNode(s))},
                    new String[]{String.format(RETURN_KEYS_WITH_VALUE, xvar, xvar, o.getLiteralValue(), p.getName())});
        }

        public static CypherQuery getNodeVarLabel(final Node s, final Node p, final Node o,
                                             final Configuration configuration, final CypherVarGenerator gen) {
            return new MatchCypherQuery(new String[]{}, new String[]{},
                    new String[]{String.format(RETURN_PATTERN, configuration.getLabelIRI(), p.getName())});
        }

        public static CypherQuery getVarLabelVar(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            return new MatchCypherQuery(new String[]{String.format(MATCH_NODE, svar)},
                    new String[]{},
                    new String[]{String.format(RETURN_NODE_MAPPING, svar, s.getName()),
                        String.format(RETURN_LABELS, svar, o.getName())});
        }

        public static CypherQuery getVarRelationshipVar(final Node s, final Node p, final Node o,
                                                   final Configuration configuration, final CypherVarGenerator gen) {
            final String relationship = configuration.unmapRelationship(p);
            final String svar = gen.getVarFor(s.getName());
            final String ovar = gen.getVarFor(o.getName());
            return new MatchCypherQuery(new String[]{String.format(MATCH_EDGE, svar, relationship, ovar)},
                    new String[]{},
                    new String[]{String.format(RETURN_NODE_MAPPING, svar, s.getName()),
                            String.format(RETURN_NODE_MAPPING, ovar, o.getName())});
        }

        public static CypherQuery getVarPropertyVar(final Node s, final Node p, final Node o,
                                               final Configuration configuration, final CypherVarGenerator gen) {
            final String property = configuration.unmapProperty(p);
            final String svar = gen.getVarFor(s.getName());
            final String evar = gen.getAnonVar();
            final String ovar = gen.getVarFor(o.getName());
            return new UnionCypherQuery(
                    new MatchCypherQuery(new String[]{String.format(MATCH_EDGE_VAR, svar, evar, ovar)},
                            new String[]{String.format(CONDITION_EXISTS, evar, property)},
                            new String[]{String.format(RETURN_NODE_MAPPING, svar, "r1"),
                                    String.format(RETURN_EDGE_MAPPING, evar, "r2"),
                                    String.format(RETURN_NODE_MAPPING, ovar, "r3"),
                                    String.format(RETURN_LITERAL_PATTERN, evar, property, o.getName())}),
                    new MatchCypherQuery(new String[]{String.format(MATCH_NODE, svar)},
                            new String[]{String.format(CONDITION_EXISTS, svar, property)},
                            new String[]{String.format(RETURN_NODE_MAPPING, svar, "r1"),
                                    String.format(RETURN_PATTERN, "''", "r2"),
                                    String.format(RETURN_PATTERN, "'", "r3"),
                                    String.format(RETURN_LITERAL_PATTERN, svar, property, o.getName())})
            );
        }

        public static CypherQuery getVarVarLabel(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            return new MatchCypherQuery(new String[]{String.format(MATCH_NODE, svar)},
                    new String[]{String.format(CONDITION_CLASS, svar, configuration.unmapLabel(o))},
                    new String[]{String.format(RETURN_NODE_MAPPING, svar, s.getName()),
                            String.format(RETURN_PATTERN, configuration.getLabelIRI(), p.getName())});
        }

        public static CypherQuery getVarVarNode(final Node s, final Node p, final Node o,
                                           final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            final String pvar = gen.getVarFor(p.getName());
            final String yvar = gen.getAnonVar();
            return new MatchCypherQuery(new String[]{String.format(MATCH_EDGE_VAR, svar, pvar, yvar)},
                    new String[]{String.format(CONDITION_ID, yvar, configuration.unmapNode(o))},
                    new String[]{String.format(RETURN_NODE_MAPPING, svar, s.getName()),
                            String.format(RETURN_EDGE_MAPPING, pvar, p.getName())});
        }

        public static CypherQuery getVarVarLiteral(final Node s, final Node p, final Node o,
                                              final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            final String pvar = gen.getVarFor(p.getName());
            final String yvar = gen.getAnonVar();
            return new UnionCypherQuery(
                    new MatchCypherQuery(new String[]{String.format(MATCH_EDGE_VAR, svar, pvar, yvar)},
                            new String[]{},
                            new String[]{String.format(RETURN_NODE_MAPPING, svar, "r1"),
                                String.format(RETURN_EDGE_MAPPING, pvar, "r2"),
                                String.format(RETURN_NODE_MAPPING, yvar, "r3"),
                                String.format(RETURN_KEYS_WITH_VALUE, pvar, pvar, o.getLiteralValue(), p.getName())}),
                    new MatchCypherQuery(new String[]{String.format(MATCH_NODE, svar)},
                            new String[]{},
                            new String[]{String.format(RETURN_NODE_MAPPING, svar, "r1"),
                                String.format(RETURN_PATTERN, "''", "r2"),
                                String.format(RETURN_PATTERN, "''", "r3"),
                                String.format(RETURN_KEYS_WITH_VALUE, svar, svar, o.getLiteralValue(), p.getName())})
            );
        }

        public static CypherQuery getNodeVarVar(final Node s, final Node p, final Node o,
                                           final Configuration configuration, final CypherVarGenerator gen) {
            final String nodeID = configuration.unmapNode(s);
            final String xvar = gen.getAnonVar();
            final String pvar = gen.getVarFor(p.getName());
            final String ovar = gen.getVarFor(o.getName());
            return new UnionCypherQuery(
                    new MatchCypherQuery(new String[]{String.format(MATCH_NODE, xvar)},
                            new String[]{String.format(CONDITION_ID, xvar, nodeID)},
                            new String[]{String.format(RETURN_PATTERN, configuration.getLabelIRI(), p.getName()),
                                String.format(RETURN_LABELS, xvar, o.getName())}),
                    new MatchCypherQuery(new String[]{String.format(MATCH_NODE, xvar)},
                            new String[]{String.format(CONDITION_ID, xvar, nodeID)},
                            new String[]{String.format(RETURN_KEYS, xvar, p.getName()),
                                String.format(RETURN_KEYS_VALUES, xvar, xvar, o.getName())}),
                    new MatchCypherQuery(new String[]{String.format(MATCH_EDGE, xvar, pvar, ovar)},
                            new String[]{String.format(CONDITION_ID, xvar, nodeID)},
                            new String[]{String.format(RETURN_EDGE_MAPPING, pvar, p.getName()),
                                String.format(RETURN_NODE_MAPPING, ovar, o.getName())})
            );
        }

        public static CypherQuery getVarVarVar(final Node s, final Node p, final Node o,
                                          final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            final String pvar = gen.getVarFor(p.getName());
            final String ovar = gen.getVarFor(o.getName());
            return new UnionCypherQuery(
                    new MatchCypherQuery(new String[]{String.format(MATCH_EDGE, svar, pvar, ovar)},
                            new String[]{},
                            new String[]{String.format(RETURN_PATTERN, "''", "r1"),
                                String.format(RETURN_PATTERN, "''", "r2"),
                                String.format(RETURN_PATTERN, "''", "r3"),
                                String.format(RETURN_NODE_MAPPING, svar, s.getName()),
                                String.format(RETURN_EDGE_MAPPING, pvar, p.getName()),
                                String.format(RETURN_NODE_MAPPING, ovar, o.getName())}),
                    new MatchCypherQuery(new String[]{String.format(MATCH_NODE, svar)},
                            new String[]{},
                            new String[]{String.format(RETURN_PATTERN, "''", "r1"),
                                String.format(RETURN_PATTERN, "''", "r2"),
                                String.format(RETURN_PATTERN, "''", "r3"),
                                String.format(RETURN_NODE_MAPPING, svar, s.getName()),
                                String.format(RETURN_PATTERN, configuration.getLabelIRI(), p.getName()),
                                String.format(RETURN_LABELS, svar, o.getName())}),
                    new MatchCypherQuery(new String[]{String.format(MATCH_EDGE, svar, pvar, ovar)},
                            new String[]{},
                            new String[]{String.format(RETURN_NODE_MAPPING, svar, "r1"),
                                String.format(RETURN_EDGE_MAPPING, pvar, "r2"),
                                String.format(RETURN_NODE_MAPPING, ovar, "r3"),
                                String.format(RETURN_KEYS, pvar, p.getName()),
                                String.format(RETURN_KEYS_VALUES, pvar, pvar, o.getName())}),
                    new MatchCypherQuery(new String[]{String.format(MATCH_NODE, svar)},
                            new String[]{},
                            new String[]{String.format(RETURN_NODE_MAPPING, svar, "r1"),
                                String.format(RETURN_PATTERN, "''", "r2"),
                                String.format(RETURN_PATTERN, "''", "r3"),
                                String.format(RETURN_KEYS, svar, p.getName()),
                                String.format(RETURN_KEYS_VALUES, svar, svar, o.getName())})
            );
        }

    }

    protected static class CypherVarGenerator {

        protected final Map<String, String> varmap = new HashMap<>();
        protected final String VAR_PATTERN = "cpvar%d";
        protected int current = 1;

        public String getVarFor( final String var ) {
            if (varmap.containsKey(var))
                return varmap.get(var);
            varmap.put(var, String.format(VAR_PATTERN, current));
            current++;
            return varmap.get(var);
        }

        public String getAnonVar() {
            String var = String.format(VAR_PATTERN, current);
            current++;
            return var;
        }
    }
}
