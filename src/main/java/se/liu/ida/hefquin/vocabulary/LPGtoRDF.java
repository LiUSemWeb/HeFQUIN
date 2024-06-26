package se.liu.ida.hefquin.vocabulary;

/* CVS $Id: $ */
 
import org.apache.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from LPGtoRDFConfiguration.ttl 
 * @author Auto-generated by schemagen on 03 Dec 2023 17:23 
 */
public class LPGtoRDF {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static final Model M_MODEL = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     * @return namespace as String
     * @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = M_MODEL.createResource( NS );
    
    /** <p>Specifies the list of mappings that a lr:CompositeNodeLabelMapping, a lr:CompositeEdgeLabelMapping, 
     *  or a lr:CompositePropertyNameMapping consists of. Hence, the domain of this 
     *  property is the union of these three classes (lr:CompositeNodeLabelMapping, 
     *  lr:CompositeEdgeLabelMapping, and lr:CompositePropertyNameMapping), and each 
     *  such mapping has exactly one such property. More specifically, for every lr:CompositeNodeLabelMapping, 
     *  the list provided by this property must contain only other lr:NodeLabelMapping 
     *  instances. Similarly, for every lr:CompositeEdgeLabelMapping, the list provided 
     *  by this property must contain only other lr:EdgeLabelMapping instances, and 
     *  for every lr:CompositePropertyNameMapping, the list must contain only other 
     *  lr:PropertyNameMapping instances.</p>
     */
    public static final Property componentMappings = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#componentMappings" );
    
    /** <p>Specifies the edge label mapping that is part of an LPG-to-RDF configuration. 
     *  Every such configuration has only one edge label mapping.</p>
     */
    public static final Property edgeLabelMapping = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#edgeLabelMapping" );
    
    /** <p>Specifies the IRI that a lr:SingletonIRINodeLabelMapping, a lr:SingletonIRIEdgeLabelMapping, 
     *  or a lr:SingletonIRIPropertyNameMapping maps its matching label or property 
     *  name to. Hence, the domain of this property is the union of these three classes 
     *  (lr:SingletonIRINodeLabelMapping, lr:SingletonIRIEdgeLabelMapping, and lr:SingletonIRIPropertyNameMapping), 
     *  and each such mapping has exactly one such property</p>
     */
    public static final Property iri = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#iri" );
    
    /** <p>Specifies the particular node or edge label that a lr:SingletonLiteralNodeLabelMapping, 
     *  a lr:SingletonIRINodeLabelMapping, or a lr:SingletonIRIEdgeLabelMapping considers. 
     *  Hence, the domain of this property is the union of these three classes (lr:SingletonLiteralNodeLabelMapping, 
     *  lr:SingletonIRINodeLabelMapping, and lr:SingletonIRIEdgeLabelMapping), and 
     *  each such mapping has exactly one such property.</p>
     */
    public static final Property label = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#label" );
    
    /** <p>Specifies the label predicate that is part of an LPG-to-RDF configuration. 
     *  Every such configuration has only one label predicate.</p>
     */
    public static final Property labelPredicate = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#labelPredicate" );
    
    /** <p>Specifies the literal that a lr:SingletonLiteralNodeLabelMapping maps its 
     *  matching node label to. Each such mapping has exactly one such property</p>
     */
    public static final Property literal = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#literal" );
    
    /** <p>Specifies the node label mapping that is part of an LPG-to-RDF configuration. 
     *  Every such configuration has only one node label mapping.</p>
     */
    public static final Property nodeLabelMapping = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#nodeLabelMapping" );
    
    /** <p>Specifies the node mapping that is part of an LPG-to-RDF configuration. Every 
     *  such configuration has only one node mapping.</p>
     */
    public static final Property nodeMapping = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#nodeMapping" );
    
    /** <p>Specifies the IRI prefix used by IRI-based node mappings, by IRI-based node 
     *  label mappings, by IRI-based edge label mappings, and by IRI-based property 
     *  name mappings. Hence, the domain of this property is the union of the following 
     *  four classes: lr:IRIPrefixBasedNodeMapping, lr:IRIPrefixBasedNodeLabelMapping, 
     *  lr:IRIPrefixBasedEdgeLabelMapping, and lr:IRIPrefixBasedPropertyNameMapping. 
     *  Every such mapping has only one such property.</p>
     */
    public static final Property prefixOfIRIs = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#prefixOfIRIs" );
    
    /** <p>Specifies the particular property name that a lr:SingletonIRIPropertyNameMapping 
     *  considers. Each such mapping has exactly one such property.</p>
     */
    public static final Property propertyName = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#propertyName" );
    
    /** <p>Specifies the property name mapping that is part of an LPG-to-RDF configuration. 
     *  Every such configuration has only one property mapping.</p>
     */
    public static final Property propertyNameMapping = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#propertyNameMapping" );
    
    /** <p>Specifies the regular expression used by regex-based node label mappings, 
     *  by regex-based edge label mappings, and by regex-based property name mappings. 
     *  Hence, the domain of this property is the union of the following three classes: 
     *  lr:RegexBasedNodeLabelMapping, lr:RegexBasedNodeLabelMapping, and lr:RegexBasedPropertyNameMapping. 
     *  Every such mapping has only one such property.</p>
     */
    public static final Property regex = M_MODEL.createProperty( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#regex" );
    
    /** <p>Class of node mappings that map every given LPG node to a blank node.</p> */
    public static final Resource BNodeBasedNodeMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#BNodeBasedNodeMapping" );
    
    /** <p>Class of edge label mappings that are based on other edge label mappings and, 
     *  for every given edge label, consider these other mappings one after another 
     *  until one of them is found that has the given edge label in its domain; this 
     *  other edge label mapping is then invoked. The list of the other edge label 
     *  mappings is specified by the lr:componentMappings property. Hence, every instance 
     *  of this class must have exactly one such property.</p>
     */
    public static final Resource CompositeEdgeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#CompositeEdgeLabelMapping" );
    
    /** <p>Class of node label mappings that are based on other node label mappings and, 
     *  for every given node label, consider these other mappings one after another 
     *  until one of them is found that has the given node label in its domain; this 
     *  other node label mapping is then invoked. The list of the other node label 
     *  mappings is specified by the lr:componentMappings property. Hence, every instance 
     *  of this class must have exactly one such property.</p>
     */
    public static final Resource CompositeNodeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#CompositeNodeLabelMapping" );
    
    /** <p>Class of property name mappings that are based on other property name mappings 
     *  and, for every given property name, consider these other mappings one after 
     *  another until one of them is found that has the given property name in its 
     *  domain; this other property name mapping is then invoked. The list of the 
     *  other property name mappings is specified by the lr:componentMappings property. 
     *  Hence, every instance of this class must have exactly one such property.</p>
     */
    public static final Resource CompositePropertyNameMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#CompositePropertyNameMapping" );
    
    /** <p>An edge label mapping is an injective function that maps labels of edges in 
     *  LPGs to IRIs, and it is one of the five components of an LPG-to-RDF configuration. 
     *  This class is a superclass of any kind of edge label mapping. Concrete types 
     *  of edge label mappings are captured by the subclasses of this class.</p>
     */
    public static final Resource EdgeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#EdgeLabelMapping" );
    
    /** <p>Class of edge label mappings that map every given edge label to an IRI by 
     *  attaching the label to a common IRI prefix. This prefix is specified by the 
     *  lr:prefixOfIRIs property. Hence, every instance of this class must have exactly 
     *  one such property.</p>
     */
    public static final Resource IRIPrefixBasedEdgeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#IRIPrefixBasedEdgeLabelMapping" );
    
    /** <p>Class of node label mappings that map every given node label to an IRI by 
     *  attaching the label to a common IRI prefix. This prefix is specified by the 
     *  lr:prefixOfIRIs property. Hence, every instance of this class must have exactly 
     *  one such property.</p>
     */
    public static final Resource IRIPrefixBasedNodeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#IRIPrefixBasedNodeLabelMapping" );
    
    /** <p>Class of node mappings that map every given LPG node to an IRI by attaching 
     *  the ID of the node to a common IRI prefix. This prefix is specified by the 
     *  lr:prefixOfIRIs property. Hence, every instance of this class must have exactly 
     *  one such property.</p>
     */
    public static final Resource IRIPrefixBasedNodeMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#IRIPrefixBasedNodeMapping" );
    
    /** <p>Class of property name mappings that map every given property name to an IRI 
     *  by attaching the property name to a common IRI prefix. This prefix is specified 
     *  by the lr:prefixOfIRIs property. Hence, every instance of this class must 
     *  have exactly one such property.</p>
     */
    public static final Resource IRIPrefixBasedPropertyNameMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#IRIPrefixBasedPropertyNameMapping" );
    
    /** <p>Class of LPG-to-RDF configurations. Instances of this class must have exactly 
     *  one lr:nodeMapping property, one lr:nodeLabelMapping property, one lr:edgeLabelMapping 
     *  property, one lr:propertyNameMapping property, and one lr:labelPredicate property.</p>
     */
    public static final Resource LPGtoRDFConfiguration = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#LPGtoRDFConfiguration" );
    
    /** <p>Class of node label mappings that map every given node label to a string literal 
     *  by using the label as the value of the literal.</p>
     */
    public static final Resource LiteralBasedNodeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#LiteralBasedNodeLabelMapping" );
    
    /** <p>A node label mapping is an injective function that maps labels of LPG nodes 
     *  to IRIs or literals, and it is one of the five components of an LPG-to-RDF 
     *  configuration. This class is a superclass of any kind of node label mapping. 
     *  Concrete types of node label mappings are captured by the subclasses of this 
     *  class.</p>
     */
    public static final Resource NodeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#NodeLabelMapping" );
    
    /** <p>A node mapping is an injective function that maps every given LPG node to 
     *  either an IRI or a blank node, and it is one of the five components of an 
     *  LPG-to-RDF configuration. This class is a superclass of any kind of node mapping. 
     *  Concrete types of node mappings are captured by the subclasses of this class.</p>
     */
    public static final Resource NodeMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#NodeMapping" );
    
    /** <p>A property name mapping is an injective function that maps names of properties 
     *  of LPG nodes and edges to IRIs, and it is one of the five components of an 
     *  LPG-to-RDF configuration. This class is a superclass of any kind of property 
     *  name mapping. Concrete types of property name mappings are captured by the 
     *  subclasses of this class.</p>
     */
    public static final Resource PropertyNameMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#PropertyNameMapping" );
    
    /** <p>Class of edge label mappings that focus only on edge labels that match a given 
     *  regular expression. This regular expression is specified by the lr:regex property. 
     *  Hence, every instance of this class must have exactly one such property.</p>
     */
    public static final Resource RegexBasedEdgeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#RegexBasedEdgeLabelMapping" );
    
    /** <p>Class of node label mappings that focus only on node labels that match a given 
     *  regular expression. This regular expression is specified by the lr:regex property. 
     *  Hence, every instance of this class must have exactly one such property. The 
     *  way the matching node labels are mapped is then determined depending on whether 
     *  the instance of this class is an instance of lr:LiteralBasedNodeLabelMapping 
     *  or of lr:IRIPrefixBasedNodeLabelMapping. Hence, every instance of this class 
     *  should also be an instance of one of these other two classes.</p>
     */
    public static final Resource RegexBasedNodeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#RegexBasedNodeLabelMapping" );
    
    /** <p>Class of property name mappings that focus only on property names that match 
     *  a given regular expression. This regular expression is specified by the lr:regex 
     *  property. Hence, every instance of this class must have exactly one such property.</p>
     */
    public static final Resource RegexBasedPropertyNameMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#RegexBasedPropertyNameMapping" );
    
    /** <p>Class of edge label mappings that focus only on a single edge label and map 
     *  it to a given IRI. This IRI is specified by the lr:iri property and the specific 
     *  edge label considered by this mapping is specified by the lr:label property. 
     *  Hence, every instance of this class must have exactly one of each of these 
     *  two properties.</p>
     */
    public static final Resource SingletonIRIEdgeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#SingletonIRIEdgeLabelMapping" );
    
    /** <p>Class of node label mappings that focus only on a single node label and map 
     *  it to a given IRI. This IRI is specified by the lr:iri property and the specific 
     *  node label considered by this mapping is specified by the lr:label property. 
     *  Hence, every instance of this class must have exactly one of each of these 
     *  two properties.</p>
     */
    public static final Resource SingletonIRINodeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#SingletonIRINodeLabelMapping" );
    
    /** <p>Class of property name mappings that focus only on a single property name 
     *  and map it to a given IRI. This IRI is specified by the lr:iri property and 
     *  the specific property name considered by this mapping is specified by the 
     *  lr:propertyName property. Hence, every instance of this class must have exactly 
     *  one of each of these two properties.</p>
     */
    public static final Resource SingletonIRIPropertyNameMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#SingletonIRIPropertyNameMapping" );
    
    /** <p>Class of node mappings that focus only on a single node label and map it to 
     *  a given literal. This literal is node label by the lr:literal property and 
     *  the specific node label considered by this mapping is specified by the lr:label 
     *  property. Hence, every instance of this class must have exactly one of each 
     *  of these two properties.</p>
     */
    public static final Resource SingletonLiteralNodeLabelMapping = M_MODEL.createResource( "http://www.example.org/se/liu/ida/hefquin/lpg2rdf#SingletonLiteralNodeLabelMapping" );
    
}