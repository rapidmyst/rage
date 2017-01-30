#!/bin/bash

if [ -n "$1" ]
then
	echo "Importing Ontology"
	$1/graql.sh -f $PWD/../ontology.gql
	echo "Migrating people"
	$1/migration.sh csv -i $PWD/people.csv -t $PWD/migrators/people-migrator.gql
	echo "Migrating births"
	$1/migration.sh csv -i $PWD/births.csv -t $PWD/migrators/births-migrator.gql
	echo "Migrating weddings"
	$1/migration.sh csv -i $PWD/weddings.csv -t $PWD/migrators/weddings-migrator.gql
	echo "Done migrating data"
	echo "Importing Rules"
	$1/graql.sh -f $PWD/../rules.gql

else
	echo "Usage: ./loader.sh <Grakn-bin-directory>"
fi
