# HeFQUIN Examples
This directory contains a number of simple example queries that demonstrate some of the features of HeFQUIN. To run each of these queries, [set up the HeFQUIN command-line programs using the latest release](https://liusemweb.github.io/HeFQUIN/doc/programs.html#use-release) and, from the root folder of the extracted release, execute the following command.
```bash
bin/hefquin --federationDescription examples/ExampleFederation.ttl --query examples/ExampleQuery1.rq
```
While the command above is for the first example query, you can simply change the file name of the query file to run any other of the example queries. The given federation description (`examples/ExampleFederation.ttl`) defines all federation members relevant for the examples.

To get a quick overview of additional arguments that can be passed to the program, execute the following command.
```bash
bin/hefquin --help
```
For a more detailed description of all the arguments refer to the [corresponding page of the HeFQUIN documentation](https://liusemweb.github.io/HeFQUIN/doc/cli.html).

