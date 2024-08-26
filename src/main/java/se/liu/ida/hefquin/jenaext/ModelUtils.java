package se.liu.ida.hefquin.jenaext;

import java.net.URI;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.XSD;

public class ModelUtils
{
	/**
	 * Based on the assumption that the given resource may have a single value
	 * for the given property, this function returns either this value or null,
	 * depending on whether the resource has such a value.
	 *
	 * If the resource has multiple values for the property,
	 * an {@link IllegalArgumentException} is thrown.
	 */
	public static RDFNode getSingleOptionalProperty( final Resource r, final Property p ) {
		return getSingleProperty(r, p, false);
	}

	/**
	 * Assumes that the given resource may have a single value for the given
	 * property and, if so, this value is a resource (not a literal). Based
	 * on this assumption, this function returns either the literal or null,
	 * depending on whether the resource has such a value.
	 *
	 * If the value is not a resource or the resource has multiple values for
	 * the property, an {@link IllegalArgumentException} is thrown.
	 */
	public static Resource getSingleOptionalResourceProperty( final Resource r, final Property p ) {
		return getSingleResourceProperty(r, p, false);
	}

	/**
	 * Assumes that the given resource may have a single value for the given
	 * property and, if so, this value is a literal. Based on this assumption,
	 * this function returns either the literal or null, depending on whether
	 * the resource has such a value.
	 *
	 * If the value is not a literal or the resource has multiple values for
	 * the property, an {@link IllegalArgumentException} is thrown.
	 */
	public static Literal getSingleOptionalLiteralProperty( final Resource r, final Property p ) {
		return getSingleLiteralProperty(r, p, false);
	}

	/**
	 * Assumes that the given resource may have a single value for the given
	 * property and, if so, this value is an xsd:string literal. Based on this
	 * assumption, this function returns either the string of this literal or
	 * null, depending on whether the resource has such a value.
	 *
	 * If the value is not an xsd:string literal or the resource has multiple
	 * values for the property, an {@link IllegalArgumentException} is thrown.
	 */
	public static String getSingleOptionalProperty_XSDString( final Resource r, final Property p ) {
		return getSingleProperty_XSDString(r, p, false);
	}

	/**
	 * Assumes that the given resource may have a single value for the given
	 * property and, if so, this value is an xsd:anyURI literal. Based on this
	 * assumption, this function returns either the URI of this literal or
	 * null, depending on whether the resource has such a value.
	 *
	 * If the value is not an xsd:anyURI literal, the lexical form of the
	 * literal cannot be converted into a URI, or the resource has multiple
	 * values for the property, an {@link IllegalArgumentException} is thrown.
	 */
	public static URI getSingleOptionalProperty_XSDURI( final Resource r, final Property p ) {
		return getSingleProperty_XSDURI(r, p, false);
	}

	/**
	 * Based on the assumption that the given resource has a single value
	 * for the given property, this function returns this.
	 *
	 * An {@link IllegalArgumentException} is thrown if the resource does
	 * not have any value for the property or it has multiple values for
	 * the property.
	 */
	public static RDFNode getSingleMandatoryProperty( final Resource r, final Property p ) {
		return getSingleProperty(r, p, true);
	}

	/**
	 * Assumes that the given resource has a single value for the given
	 * property and this value is a resource (not a literal). Based on
	 * this assumption, this function returns this literal.
	 *
	 * An {@link IllegalArgumentException} is thrown if the value is not a
	 * resource, the resource does not have any value for the property, or
	 * it has multiple values for the property. 
	 */
	public static Resource getSingleMandatoryResourceProperty( final Resource r, final Property p ) {
		return getSingleResourceProperty(r, p, true);
	}

	/**
	 * Assumes that the given resource has a single value for the given
	 * property and this value is a literal. Based on this assumption,
	 * this function returns this literal.
	 *
	 * An {@link IllegalArgumentException} is thrown if the value is not a
	 * literal, the resource does not have any value for the property, or
	 * it has multiple values for the property. 
	 */
	public static Literal getSingleMandatoryLiteralProperty( final Resource r, final Property p ) {
		return getSingleLiteralProperty(r, p, true);
	}

	/**
	 * Assumes that the given resource has a single value for the given
	 * property and this value is an xsd:string literal. Based on this
	 * assumption, this function returns the string of this literal.
	 *
	 * An {@link IllegalArgumentException} is thrown if the value is not
	 * an xsd:string literal, the resource does not have any value for
	 * the property, or it has multiple values for the property. 
	 */
	public static String getSingleMandatoryProperty_XSDString( final Resource r, final Property p ) {
		return getSingleProperty_XSDString(r, p, true);
	}

	/**
	 * Assumes that the given resource has a single value for the given
	 * property and this value is an xsd:anyURI literal. Based on this
	 * assumption, this function returns the URI of this literal.
	 *
	 * An {@link IllegalArgumentException} is thrown if the value is not
	 * an xsd:anyURI literal, the lexical form of the literal cannot be
	 * converted into a URI, the resource does not have any value for
	 * the property, or it has multiple values for the property. 
	 */
	public static URI getSingleMandatoryProperty_XSDURI( final Resource r, final Property p ) {
		return getSingleProperty_XSDURI(r, p, true);
	}


	// ----- helper functions ----

	protected static RDFNode getSingleProperty( final Resource r, final Property p, final boolean mandatory ) {
		final StmtIterator it = r.listProperties(p);

		if( ! it.hasNext() ) {
			if ( mandatory )
				throw new IllegalArgumentException( p.getLocalName() + " property missing for " + r.toString() );
			else
				return null;
		}

		final RDFNode o = it.next().getObject();

		if ( it.hasNext() )
			throw new IllegalArgumentException( "More than one " + p.getLocalName() + " property given for " + r.toString() );

		return o;
	}

	protected static Resource getSingleResourceProperty( final Resource r, final Property p, final boolean mandatory ) {
		final RDFNode v = getSingleProperty(r, p, mandatory);

		if ( v == null )
			return null;

		if ( ! v.isResource() )
			throw new IllegalArgumentException( p.getLocalName() + " property of " + r.toString() + " is not a resource (but a literal)" );

		return v.asResource();
	}

	protected static Literal getSingleLiteralProperty( final Resource r, final Property p, final boolean mandatory ) {
		final RDFNode v = getSingleProperty(r, p, mandatory);

		if ( v == null )
			return null;

		if ( ! v.isLiteral() )
			throw new IllegalArgumentException( p.getLocalName() + " property of " + r.toString() + " is not a literal" );

		return v.asLiteral();
	}

	protected static String getSingleProperty_XSDString( final Resource r, final Property p, final boolean mandatory ) {
		final Literal l = getSingleLiteralProperty(r, p, mandatory);

		if ( l == null )
			return null;

		if ( ! l.getDatatypeURI().equals(XSD.xstring.getURI()) )
			throw new IllegalArgumentException( p.getLocalName() + " property of " + r.toString() + " is not of type xsd:string" );

		return l.getString();
	}

	protected static URI getSingleProperty_XSDURI( final Resource r, final Property p, final boolean mandatory ) {
		final Literal l = getSingleLiteralProperty(r, p, mandatory);

		if ( l == null )
			return null;

		if ( ! l.getDatatypeURI().equals(XSD.anyURI.getURI()) )
			throw new IllegalArgumentException( p.getLocalName() + " property of " + r.toString() + " is not of type xsd:anyURI" );

		final String s = l.getString();
		try{
			return URI.create(s);
		}
		catch ( final Exception e ){
			throw new IllegalArgumentException( p.getLocalName() + " property of " + r.toString() + " is not a valid URI", e );
		}
	}

}
