const defaultQuery = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\nPREFIX dbo: <http://dbpedia.org/ontology/>\n\nSELECT *\nWHERE {\n  SERVICE <http://dbpedia.org/sparql> {\n    <http://dbpedia.org/resource/Berlin> dbo:country ?c .\n    ?c owl:sameAs ?cc\n  }\n  SERVICE <https://query.wikidata.org/sparql> {\n    ?cc rdfs:label ?o\n  }\n}';

async function initQueryEditor(){
    const origin = window.location.origin;
    const yasgui = new Yasgui(document.getElementById('yasgui'), {
        requestConfig: {
            endpoint: `${origin}/query-inspect`,
            method: 'GET'
        },
        yasqe: {
            value: defaultQuery
        },
        endpointCatalogueOptions: {
            getData: () => {
                return [
                    { endpoint: `${origin}/sparql` },
                    { endpoint: `${origin}/query-inspect` }
                ]
            }
        }
    });
    updateDesiredOutputPlugin();

    function updateDesiredOutputPlugin() {
        const tab = yasgui.getTab();
        if(!tab) return;

        const endpoint = tab.getEndpoint();
        let desiredPlugin = 'table';
        if (endpoint.includes('/query-inspect')) {
            desiredPlugin = 'response';
        }
        tab.yasr.config.defaultPlugin = desiredPlugin;
        console.log("desired input for tab is: " + desiredPlugin)
    }

    yasgui.on('tabChange', () => {
        updateDesiredOutputPlugin();
    });

    const endpointInput = document.querySelector('.autocomplete');
    endpointInput.addEventListener('change', () => {
        updateDesiredOutputPlugin();
    });
}