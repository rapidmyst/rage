## Analytics Example

See the [example documentation](https://grakn.ai/pages/documentation/examples/analytics.html) for further information about this example.  Start Grakn and you should simply be able to load in the ontology and data into a keyspace to get up and running.

```
<relative-path-to-Grakn>/bin/grakn.sh clean
<relative-path-to-Grakn>/bin/grakn.sh start
<relative-path-to-Grakn>/bin/graql.sh -f ./ontology.gql
<relative-path-to-Grakn>/bin/graql.sh -b ./data.gql
```


If you prefer to migrate the CSV dataset into Grakn, you need to use the migrator shell script as follows, once Grakn is running:


```
<relative-path-to-grakn-install>/bin/graql.sh -f ./ontology.gql
<relative-path-to-grakn-install>/bin/migration.sh csv -i ./raw-data/mtcars.csv -t ./raw-data/makers.gql -k grakn
<relative-path-to-grakn-install>/bin/migration.sh csv -i ./raw-data/mtcars.csv -t ./raw-data/cars.gql -k grakn
```

**NOTE**    

The data will be loaded into a Grakn graph using the default keyspace, so you should clean it first, or use a different keyspace after the -k flag if you prefer
