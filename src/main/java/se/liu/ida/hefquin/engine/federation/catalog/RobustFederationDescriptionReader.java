package se.liu.ida.hefquin.engine.federation.catalog;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.catalog.impl.FederationCatalogImpl;
import se.liu.ida.hefquin.vocabulary.FD;

import java.util.HashMap;
import java.util.Map;

public class RobustFederationDescriptionReader extends FederationDescriptionReader {

    public static RobustFederationDescriptionReader instance = new RobustFederationDescriptionReader();

    public static FederationCatalog readFromModel(final Model fd ) {
        return instance.parseFedDescr(fd);
    }

    public FederationCatalog parseFedDescr( final Model fd ) {
        final Map<String, FederationMember> membersByURI = new HashMap<>();

        // Iterate over all federation members mentioned in the description
        final ResIterator fedMembers = fd.listResourcesWithProperty(RDF.type, FD.FederationMember);
        while ( fedMembers.hasNext() ) {
            final Resource fedMember = fedMembers.next();
            final VocabularyMapping vocabMap = parseVocabMapping(fedMember, fd);


            final Resource iface = fedMember.getProperty(FD.interface_).getResource();
            final RDFNode ifaceType = fd.getRequiredProperty(iface, RDF.type).getObject();

            // Check the type of interface
            if ( ifaceType.equals(FD.SPARQLEndpointInterface) ) {
                //NEW CODE
                int numberOfEndpointAddresses = iface.listProperties(FD.endpointAddress).toList().size();
                if (numberOfEndpointAddresses == 0){
                    throw new IllegalArgumentException("EndpointAddress is required!");
                }
                if (numberOfEndpointAddresses > 1){
                    throw new IllegalArgumentException("More Than One EndpointAddress!");
                }
                //End of NEW CODE

                final RDFNode addr = fd.getRequiredProperty(iface, FD.endpointAddress).getObject();

                final String addrStr;
                if ( addr.isLiteral() ) {
                    addrStr = addr.asLiteral().getLexicalForm();
                }
                else if ( addr.isURIResource() ) {
                    addrStr = addr.asResource().getURI();
                }
                else {
                    throw new IllegalArgumentException();
                }

                final FederationMember fm = createSPARQLEndpoint(addrStr, vocabMap);
                membersByURI.put(addrStr, fm);
            }
            else if ( ifaceType.equals(FD.TPFInterface) ) {
                //NEW CODE
                int numberOfExampleFragmentAddresses = iface.listProperties(FD.exampleFragmentAddress).toList().size();
                if (numberOfExampleFragmentAddresses == 0){
                    throw new IllegalArgumentException("ExampleFragmentAddress is required!");
                }
                if (numberOfExampleFragmentAddresses > 1){
                    throw new IllegalArgumentException("More Than One ExampleFragmentAddress!");
                }
                //End of NEW CODE

                final RDFNode addr = fd.getRequiredProperty(iface, FD.exampleFragmentAddress).getObject();

                final String addrStr;
                if ( addr.isLiteral() ) {
                    addrStr = addr.asLiteral().getLexicalForm();
                }
                else if ( addr.isURIResource() ) {
                    addrStr = addr.asResource().getURI();
                }
                else {
                    throw new IllegalArgumentException();
                }

                final FederationMember fm = createTPFServer(addrStr, vocabMap);
                membersByURI.put(addrStr, fm);
            }
            else if ( ifaceType.equals(FD.brTPFInterface) ) {
                //NEW CODE
                int numberOfExampleFragmentAddresses = iface.listProperties(FD.exampleFragmentAddress).toList().size();
                if (numberOfExampleFragmentAddresses == 0){
                    throw new IllegalArgumentException("ExampleFragmentAddress is required!");
                }
                if (numberOfExampleFragmentAddresses > 1){
                    throw new IllegalArgumentException("More Than One ExampleFragmentAddress!");
                }
                //End of NEW CODE

                final RDFNode addr = fd.getRequiredProperty(iface, FD.exampleFragmentAddress).getObject();

                final String addrStr;
                if ( addr.isLiteral() ) {
                    addrStr = addr.asLiteral().getLexicalForm();
                }
                else if ( addr.isURIResource() ) {
                    addrStr = addr.asResource().getURI();
                }
                else {
                    throw new IllegalArgumentException();
                }

                final FederationMember fm = createBRTPFServer(addrStr, vocabMap);
                membersByURI.put(addrStr, fm);
            }
            else if ( ifaceType.equals(FD.BoltInterface) ) {
                //NEW CODE
                int numberOfEndpointAddresses = iface.listProperties(FD.endpointAddress).toList().size();
                if (numberOfEndpointAddresses == 0){
                    throw new IllegalArgumentException("EndpointAddress is required!");
                }
                if (numberOfEndpointAddresses > 1){
                    throw new IllegalArgumentException("More Than One EndpointAddress!");
                }
                //End of NEW CODE
                final RDFNode addr = fd.getRequiredProperty(iface, FD.endpointAddress).getObject();

                final String addrStr;
                if ( addr.isLiteral() ) {
                    addrStr = addr.asLiteral().getLexicalForm();
                }
                else if ( addr.isURIResource() ) {
                    addrStr = addr.asResource().getURI();
                }
                else {
                    throw new IllegalArgumentException();
                }

                final FederationMember fm = createNeo4jServer(addrStr, vocabMap);
                membersByURI.put(addrStr, fm);
            }
            else if ( ifaceType.equals(FD.GraphQLEndpointInterface) ) {
                //NEW CODE
                int numberOfEndpointAddresses = iface.listProperties(FD.endpointAddress).toList().size();
                if (numberOfEndpointAddresses == 0){
                    throw new IllegalArgumentException("EndpointAddress is required!");
                }
                if (numberOfEndpointAddresses > 1){
                    throw new IllegalArgumentException("More Than One EndpointAddress!");
                }
                //End of NEW CODE

                final RDFNode addr = fd.getRequiredProperty(iface, FD.endpointAddress).getObject();

                final String addrStr;
                if ( addr.isLiteral() ) {
                    addrStr = addr.asLiteral().getLexicalForm();
                }
                else if ( addr.isURIResource() ) {
                    addrStr = addr.asResource().getURI();
                }
                else {
                    throw new IllegalArgumentException();
                }

                final FederationMember fm = createGraphQLServer(addrStr, vocabMap);
                membersByURI.put(addrStr, fm);
            }
            else {
                throw new IllegalArgumentException( ifaceType.toString() );
            }

        }

        return new FederationCatalogImpl(membersByURI);
    }
}
