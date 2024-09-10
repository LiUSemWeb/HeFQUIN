#!/usr/bin/env python3
import os
import re
from jinja2 import Environment, FileSystemLoader, Template
from rdflib import DC, RDF, RDFS, Graph, URIRef
from rdflib.namespace import NamespaceManager
import logging

prefixes = {}

# Configure root logger
logger = logging.getLogger()
logger.setLevel(logging.INFO)
logging.basicConfig(format='%(levelname)-8s [%(filename)s:%(lineno)d] %(message)s', level=logging.DEBUG)


def copy_vocabularies(vocabs):
    """Copy ontologies to docs/vocab."""
    logger.info("Copy vocabularies")
  
    for name, version in vocabs.items():
        logger.info(f"Copying {name}")
        versioned_target = f"docs/vocab/{name}/{version}/"
        latest_target = f"docs/vocab/{name}/latest/"
        os.makedirs(versioned_target, exist_ok=True)
        os.makedirs(latest_target, exist_ok=True)
        
        g = Graph()
        g.parse(f"hefquin-vocabs/{name}.ttl")
        # Versioned
        g.serialize(destination=f"{versioned_target}{name}.ttl", format="turtle")
        g.serialize(destination=f"{versioned_target}{name}.rdf", format="xml")
        g.serialize(destination=f"{versioned_target}{name}.rdf", format="xml")
        g.serialize(destination=f"{versioned_target}{name}.jsonld", format="json-ld")
        # Latest
        g.serialize(destination=f"{latest_target}{name}.ttl", format="turtle")
        g.serialize(destination=f"{latest_target}{name}.rdf", format="xml")
        g.serialize(destination=f"{latest_target}{name}.rdf", format="xml")
        g.serialize(destination=f"{latest_target}{name}.jsonld", format="json-ld")
    

def create_documentation(vocabs):
    """Generate LODE documentation."""
    logger.info(f"Generating vocabulary documentation")
    
    for name, version in vocabs.items():
        logger.info(f"Generating documentation for {name}")
        versioned_target = f"docs/vocab/{name}/{version}/index.html"
        latest_target = f"docs/vocab/{name}/latest/index.html"
        
        # Load RDF data into a graph
        g = Graph(bind_namespaces="none")
        g.parse(file=open(f"hefquin-vocabs/{name}.ttl"), format="turtle")
        
        # Set up Jinja2 environment and load the template
        env = Environment(loader=FileSystemLoader('./docs-scripts/'))
        env.filters['prefix'] = prefix
        env.filters['prefix_list'] = prefix_list
        env.filters['fragment'] = fragment
        html_template = env.get_template('template.j2')
        
        # Collect prefixes
        for p, namespace in g.namespaces():
            prefixes[p] = namespace
        
        # Get the identifier of the vocabulary
        for s, p, _ in g.triples((None, DC.identifier, None)):
            vocab_uri = s

        # Extract general information about the vocabulary
        data = {
            "creator": g.value(vocab_uri, DC.creator),
            "date": g.value(vocab_uri,  DC.date),
            "format": g.value(vocab_uri,  DC.format),
            "identifier": g.value(vocab_uri,  DC.identifier),
            "title": g.value(vocab_uri, DC.title),
            "label": g.value(vocab_uri, RDFS.label),
            "description": g.value(vocab_uri, DC.description),
            "comment": g.value(vocab_uri, RDFS.comment),
            "prefixes": prefixes,
            "classes": [],
            "properties": [],
            "instances": []
        }

        # Extract classes
        for s in g.subjects(RDF.type, RDFS.Class):
            data["classes"].append({
                "uri": s,
                "label": g.value(s, RDFS.label, default="N/A"),
                "comment": g.value(s, RDFS.comment, default="N/A"),
                "subclasses": [o for o in g.objects(s, RDFS.subClassOf)]
            })

        # Extract properties
        for s in g.subjects(RDF.type, RDF.Property):
            data["properties"].append({
                "uri": s,
                "label": g.value(s, RDFS.label, default="N/A"),
                "comment": g.value(s, RDFS.comment, default="N/A"),
                "domain": g.value(s, RDFS.domain, default="N/A"),
                "range": g.value(s, RDFS.range, default="N/A")
            })

        # Extract instances
        for s in g.subjects(RDF.type, None):
            if s not in [cls['uri'] for cls in data['classes']] and s not in [prop['uri'] for prop in data['properties']]:
                data["instances"].append({
                    "uri": s,
                    "type": [o for o in g.objects(s, RDF.type)]
                })

        
        ## Save the HTML to a file
        with open(versioned_target, "w") as file:
            data["location"] = f"{vocab_uri}/{version}/{name}"
            html_content = html_template.render(data)
            file.write(html_content)
        
        with open(latest_target, "w") as file:
            data["location"] = f"{vocab_uri}/latest/{name}"
            html_content = html_template.render(data)
            file.write(html_content)


def prefix_list(l):
    return [prefix(i) for i in l]


def prefix(resource):
    for prefix, uri in prefixes.items():
        if resource.startswith(uri):
            return f"{prefix}:{resource.replace(uri, '')}"
    return resource


def fragment(resource):
    return re.split(r"[/#]", resource)[-1]


def main():
    vocabs = { "engineconf" : "0.0.1",
               "lpg2rdfconf" : "0.0.1" }
    copy_vocabularies(vocabs)
    create_documentation(vocabs)

if __name__ == "__main__":
    main()
