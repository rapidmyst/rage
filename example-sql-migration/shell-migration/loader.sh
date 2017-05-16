#!/bin/bash

if [ -n "$1" ]
then
	echo "Importing World DB into default keyspace"

	$1/bin/migration.sh sql -q "SELECT * FROM country;" -location jdbc:mysql://localhost:3306/world -user root -pass root -t $PWD/./migration/countries/template.gql -k grakn
	$1/bin/migration.sh sql -q "SELECT * FROM city;" -location jdbc:mysql://localhost:3306/world -user root -pass root -t $PWD/./migration/cities/template.gql -k grakn
	$1/bin/migration.sh sql -q "SELECT code, capital FROM country;" -location jdbc:mysql://localhost:3306/world -user root -pass root -t $PWD/./migration/capitals/template.gql -k grakn
	$1/bin/migration.sh sql -q "SELECT DISTINCT language FROM countrylanguage;" -location jdbc:mysql://localhost:3306/world -user root -pass root -t $PWD/./migration/languages/template.gql -k grakn
	$1/bin/migration.sh sql -q "SELECT * from countrylanguage;" -location jdbc:mysql://localhost:3306/world -user root -pass root -t $PWD/./migration/languagesspoken/template.gql -k grakn
	echo "Done migrating data"
else
	echo "Usage: ./loader.sh <Grakn-bin-directory>"
fi

	