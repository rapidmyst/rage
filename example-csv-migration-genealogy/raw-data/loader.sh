#!/bin/bash

if [ -n "$1" ]
then
	echo "Importing Ontology into default keyspace"
	$1/graql.sh -f $PWD/../ontology.gql
	echo "Migrating people into default keyspace"
	$1/migration.sh csv -i $PWD/people.csv -t $PWD/migrators/people-migrator.gql -k grakn
	echo "Migrating births into default keyspace"
	$1/migration.sh csv -i $PWD/births.csv -t $PWD/migrators/births-migrator.gql -k grakn
	echo "Migrating weddings into default keyspace"
	$1/migration.sh csv -i $PWD/weddings.csv -t $PWD/migrators/weddings-migrator.gql -k grakn
	echo "Done migrating data"

else
	echo "Usage: ./loader.sh <Grakn-bin-directory>"
fi
