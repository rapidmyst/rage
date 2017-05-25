## example-SQL-migration

This is an example of [SQL migration using the Java API](https://grakn.ai/pages/documentation/examples/SQL-migration.html). 

To run the shell migration you can use the following commands from within the `shell-migration` folder: 

```
// load the ontology 
graql.sh -f ontology.gql

// migrate as specified in sql-migrators.yaml 
migration.sh sql -k grakn -location jdbc:mysql://localhost:3306/world -user root -pass root -c sql-migrators.yaml 
```

Please see the [Grakn documentation portal](https://grakn.ai/pages/documentation/migration/SQL-migration.html) for a worked example of SQL migration using the Graql migration script.