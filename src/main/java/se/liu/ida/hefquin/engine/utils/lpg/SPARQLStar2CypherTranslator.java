package se.liu.ida.hefquin.engine.utils.lpg;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SPARQLStar2CypherTranslator {

    final static private String MATCH_PATTERN = "MATCH (%s)-[:%s]->(%s)";
    final static private String NESTED_MATCH_PATTERN = "MATCH (%s)-[%s:%s {%s : '%s'}]->(%s)";
    final static private String RETURN_PATTERN = "%s AS %s";
    final static private String RETURN_LITERAL_PATTERN = "%s.%s AS %s";
    final static private String EXISTS_CONDITION = "EXISTS(%s.%s)";

    public static String translate( final BGP bgp ) {
        return translate(bgp, new DefaultConfiguration(), new CypherVarGenerator());
    }

    public static String translate( final BGP bgp, final Configuration configuration) {
        return translate(bgp, configuration, new CypherVarGenerator());
    }

    public static String translate( final BGP bgp, final Configuration configuration,
                                    final CypherVarGenerator gen) {
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        if (triples.size() == 1) {
            return translateTriple((TriplePattern) triples.toArray()[0], configuration, gen);
        }
        return translateBGP(bgp);
    }

    private static String translateBGP( final BGP bgp ) {
        final Set<String> matches = new HashSet<>();
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        final Set<String> returns = new HashSet<>();
        final Set<String> conditions = new HashSet<>();
        return null;
    }

    private static String translateTriple(final TriplePattern pattern, final Configuration configuration,
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

        final protected static String varPropLit    = "MATCH (%s) WHERE %s.%s='%s' " +
                "RETURN nm(%s) AS r1, '' AS r2, '' AS r3 " +
                "UNION MATCH (%s)-[%s]->(%s) WHERE %s.%s='%s' " +
                "RETURN nm(%s) AS r1, elm(%s) AS r2, nm(%s) AS r3";
        final protected static String varLabelClass = "MATCH (%s) WHERE %s:%s RETURN nm(%s) AS %s";
        final protected static String varRelNode    = "MATCH (%s)-[:%s]->(%s) WHERE ID(%s) = %s RETURN nm(%s) AS %s";
        final protected static String nodeLabelVar  = "MATCH (%s) WHERE ID(%s)=%s RETURN labels(%s) AS %s";
        final protected static String nodePropVar   = "MATCH (%s) WHERE ID(%s)=%s AND EXISTS(%s.%s) RETURN %s.%s AS %s";
        final protected static String nodeRelVar    = "MATCH (%s)-[:%s]->(%s) WHERE ID(%s)=%s RETURN nm(%s) AS %s";
        final protected static String nodeVarNode   = "MATCH (%s)-[%s]->(%s) WHERE ID(%s)=%s AND ID(%s)=%s RETURN elm(%s) AS %s";
        final protected static String nodeVarLit    = "MATCH (%s) WHERE ID(%s)=%s RETURN [k in KEYS(%s) WHERE %s[k]='%s' | pm(k)] AS %s";
        final protected static String nodeVarLabel  = "RETURN %s AS %s";
        final protected static String varLabelVar   = "MATCH (%s) RETURN nm(%s) AS %s, labels(%s) AS %s";
        final protected static String varRelVar     = "MATCH (%s)-[:%s]->(%s) RETURN nm(%s) AS %s, nm(%s) AS %s";
        final protected static String varPropVar    = "MATCH (%s) WHERE EXISTS(%s.%s) " +
                "RETURN nm(%s) AS r1, '' AS r2, '' AS r3, %s.%s AS %s UNION " +
                "MATCH (%s)-[%s]->(%s) WHERE EXISTS(%s.%s) " +
                "RETURN nm(%s) AS r1, elm(%s) AS r2, nm(%s) AS r3, %s.%s AS %s";
        final protected static String varVarLabel   = "MATCH (%s) WHERE %s:%s RETURN nm(%s) AS %s, %s AS %s";
        final protected static String varVarNode    = "MATCH (%s)-[%s]->(%s) WHERE ID(%s)=%s RETURN nm(%s) AS %s, elm(%s) AS %s";
        final protected static String varVarLit     = "MATCH (%s)-[%s]->(%s) " +
                "RETURN nm(%s) AS r1, elm(%s) AS r2, nm(%s) AS r3, " +
                "[k IN KEYS(%s) WHERE %s[k]='%s' | pm(k)] AS %s UNION " +
                "MATCH (%s) RETURN nm(%s) AS r1, '' AS r2, '' AS r3, " +
                "[k IN KEYS(%s) WHERE %s[k]='%s' | pm(k)] AS %s";
        final protected static String nodeVarVar    = "MATCH (%s) WHERE ID(%s)=%s " +
                "RETURN %s AS %s, labels(%s) AS %s UNION " +
                "MATCH (%s) WHERE ID(%s)=%s " +
                "RETURN [k IN KEYS(%s) | pm(k)] AS %s, " +
                "[k in KEYS(%s) | %s[k]] AS %s UNION " +
                "MATCH (%s)-[%s]->(%s) WHERE ID(%s)=%s " +
                "RETURN elm(%s) AS %s, nm(%s) AS %s";
        final protected static String varVarVar     = "MATCH (%s)-[%s]->(%s) " +
                "RETURN nm(%s) AS %s, elm(%s) AS %s, nm(%s) AS %s UNION " +
                "MATCH (%s) RETURN nm(%s) AS %s, %s AS %s, labels(%s) AS %s; " +
                "MATCH (%s) RETURN %s AS s UNION MATCH ()-[%s]->() RETURN %s AS s";

        public static String getVarPropertyLiteral(final Node s, final Node p, final Node o,
                                                   final Configuration configuration, final CypherVarGenerator gen) {
            final String property = configuration.unmapProperty(p);
            final String literal = o.getLiteralValue().toString();
            final String svar = gen.getVarFor(s.getName());
            final String xvar = gen.getAnonVar();
            final String evar = gen.getAnonVar();
            final String yvar = gen.getAnonVar();
            return String.format(varPropLit, svar, svar, property, literal, svar,
                    xvar, evar, yvar, evar, property, literal, xvar, evar, yvar);
        }

        public static String getVarLabelClass(final Node s, final Node p, final Node o,
                                              final Configuration configuration, final CypherVarGenerator gen) {
            final String clazz = configuration.unmapLabel(o);
            final String svar = gen.getVarFor(s.getName());
            return String.format(varLabelClass, svar, svar, clazz, svar, s.getName());
        }

        public static String getVarRelationshipNode(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final String relationship = configuration.unmapRelationship(p);
            final String nodeID = configuration.unmapNode(o);
            final String svar = gen.getVarFor(s.getName());
            final String ovar = gen.getAnonVar();
            return String.format(varRelNode, svar, relationship, ovar, ovar, nodeID, svar, s.getName());
        }

        public static String getNodeLabelVar(final Node s, final Node p, final Node o,
                                             final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(o.getName());
            return String.format(nodeLabelVar, svar, svar, configuration.unmapNode(s), svar, o.getName());
        }

        public static String getNodePropertyVar(final Node s, final Node p, final Node o,
                                                final Configuration configuration, final CypherVarGenerator gen) {
            final String property = configuration.unmapProperty(p);
            final String var = gen.getAnonVar();
            return String.format(nodePropVar, var, var, configuration.unmapNode(s), var, property,
                    var, property, o.getName());
        }

        public static String getNodeRelationshipVar(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            final String yvar = gen.getVarFor(o.getName());
            return String.format(nodeRelVar, xvar, configuration.unmapRelationship(p), yvar, xvar,
                    configuration.unmapNode(s), yvar, o.getName());
        }

        public static String getNodeVarNode(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            final String evar = gen.getVarFor(p.getName());
            final String yvar = gen.getAnonVar();
            return String.format(nodeVarNode, xvar, evar, yvar, xvar, configuration.unmapNode(s),
                    yvar, configuration.unmapNode(o), evar, p.getName());
        }

        public static String getNodeVarLiteral(final Node s, final Node p, final Node o,
                                               final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            return String.format(nodeVarLit, xvar, xvar, configuration.unmapNode(s), xvar, xvar,
                    o.getLiteralValue(), p.getName());
        }

        public static String getNodeVarLabel(final Node s, final Node p, final Node o,
                                             final Configuration configuration, final CypherVarGenerator gen) {
            return String.format(nodeVarLabel, configuration.getLabelIRI(), p.getName());
        }

        public static String getVarLabelVar(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            return String.format(varLabelVar, svar, svar, s.getName(), svar, o.getName());
        }

        public static String getVarRelationshipVar(final Node s, final Node p, final Node o,
                                                   final Configuration configuration, final CypherVarGenerator gen) {
            final String relationship = configuration.unmapRelationship(p);
            final String svar = gen.getVarFor(s.getName());
            final String ovar = gen.getVarFor(o.getName());
            return String.format(varRelVar, svar, relationship, ovar, svar, s.getName(), ovar, o.getName());
        }

        public static String getVarPropertyVar(final Node s, final Node p, final Node o,
                                               final Configuration configuration, final CypherVarGenerator gen) {
            final String property = configuration.unmapProperty(p);
            final String svar = gen.getVarFor(s.getName());
            final String evar = gen.getAnonVar();
            final String ovar = gen.getVarFor(o.getName());
            return String.format(varPropVar, svar, svar, property, svar, svar, property, o.getName(),
                    svar, evar, ovar, evar, property, svar, evar, ovar, evar, property, o.getName());
        }

        public static String getVarVarLabel(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            return String.format(varVarLabel, svar, svar, configuration.unmapLabel(o), svar, s.getName(),
                    configuration.getLabelIRI(), p.getName());
        }

        public static String getVarVarNode(final Node s, final Node p, final Node o,
                                           final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            final String pvar = gen.getVarFor(p.getName());
            final String yvar = gen.getAnonVar();
            return String.format(varVarNode, svar, pvar, yvar, yvar, configuration.unmapNode(o),
                    svar, s.getName(), pvar, p.getName());
        }

        public static String getVarVarLiteral(final Node s, final Node p, final Node o,
                                              final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            final String pvar = gen.getVarFor(p.getName());
            final String yvar = gen.getAnonVar();
            return String.format(varVarLit, svar, pvar, yvar, svar, pvar, yvar, pvar, pvar, o.getLiteralValue(),
                    p.getName(), svar, svar, svar, svar, o.getLiteralValue(), p.getName());
        }

        public static String getNodeVarVar(final Node s, final Node p, final Node o,
                                           final Configuration configuration, final CypherVarGenerator gen) {
            final String nodeID = configuration.unmapNode(s);
            final String xvar = gen.getAnonVar();
            final String pvar = gen.getVarFor(p.getName());
            final String ovar = gen.getVarFor(o.getName());
            return String.format(nodeVarVar, xvar, xvar, nodeID, configuration.getLabelIRI(), p.getName(),
                    xvar, o.getName(), xvar, xvar, nodeID, xvar, p.getName(), xvar, xvar, o.getName(),
                    xvar, pvar, ovar, xvar, nodeID, pvar, p.getName(), ovar, o.getName());
        }

        public static String getVarVarVar(final Node s, final Node p, final Node o,
                                          final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            final String pvar = gen.getVarFor(p.getName());
            final String ovar = gen.getVarFor(o.getName());
            return String.format(varVarVar, svar, pvar, ovar, svar, s.getName(), pvar, p.getName(), ovar, o.getName(),
                    svar, svar, s.getName(), configuration.getLabelIRI(), p.getName(), svar, o.getName(),
                    svar, svar, pvar, pvar);
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
