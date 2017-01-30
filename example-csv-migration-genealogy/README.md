This is the example code for the project described in the Grakn documentation [here](https://grakn.ai/pages/documentation/examples/CSV-migration.html).

## ontology.gql
       
The general idea of the ontology is that, to build your family tree, you gather documents that will serve as evidence for different occurrences (births, deaths, marriages etc.). Once an event is supported by enough evidence, the connected knowledge can be transferred to the actual family tree. 

To load the ontology, make sure you have downloaded Grakn and started the Grakn engine (see our [setup guide](https://grakn.ai/pages/documentation/get-started/setup-guide.html) if you need more information about how to do this). Then, from the terminal: 

```
../bin/graql.sh -f ./ontology.gql
```


Follow the example documentation to migrate the CSV data into Grakn.