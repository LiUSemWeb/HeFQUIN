package se.liu.ida.hefquin.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class LPG2RDF {
    public static final String uri = "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#";

    protected static final Resource resource(final String local ) {
        return ResourceFactory.createResource( uri + local );
    }

    protected static final Property property(final String local ) {
        return ResourceFactory.createProperty(uri, local);
    }

    public static final Resource LPGtoRDFConfiguration = resource("LPGtoRDFConfiguration");

    public static final Resource NodeMapping = resource("NodeMapping");
    public static final Resource BNodeBasedNodeMapping = resource("BNodeBasedNodeMapping");
    public static final Resource IRIBasedNodeMapping = resource("IRIBasedNodeMapping");

    public static final Resource NodeLabelMapping = resource("NodeLabelMapping");
    public static final Resource IRIBasedNodeLabelMapping = resource("IRIBasedNodeLabelMapping");
    public static final Resource LiteralBasedNodeLabelMapping = resource("LiteralBasedNodeLabelMapping");
    public static final Resource EdgeLabelMapping = resource("EdgeLabelMapping");
    public static final Resource IRIBasedEdgeLabelMapping = resource("IRIBasedEdgeLabelMapping");
    public static final Resource PropertyNameMapping = resource("PropertyNameMapping");
    public static final Resource IRIBasedPropertyNameMapping = resource("IRIBasedPropertyNameMapping");
    public static final Resource RegexBasedEdgeLabelMapping = resource("RegexBasedEdgeLabelMapping");

    public static final Property labelPredicate = property("labelPredicate");
    public static final Property nodeMapping = property("nodeMapping");
    public static final Property nodeLabelMapping = property("nodeLabelMapping");
    public static final Property edgeLabelMapping = property("edgeLabelMapping");
    public static final Property regex = property("regex");
    public static final Property propertyNameMapping = property("propertyNameMapping");
    public static final Property prefixOfIRIs = property("prefixOfIRIs");

}
