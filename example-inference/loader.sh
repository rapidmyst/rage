#!/bin/bash

if [ -n "$1" ]
then
	echo "Importing World DB"

	$1/bin/migration.sh sql -q "SELECT * FROM country;" -location jdbc:mysql://localhost:3306/world -user root -pass root -t $PWD/./migration/countries/template.gql -k grakn
	$1/bin/migration.sh sql -q "SELECT * FROM city;" -location jdbc:mysql://localhost:3306/world -user root -pass root -t $PWD/./migration/cities/template.gql -k grakn
	echo "Done migrating data"
else
	echo "Usage: ./loader.sh <Grakn-bin-directory>"
fi

	