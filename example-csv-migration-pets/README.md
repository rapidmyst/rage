CSV Migration Example
---

We will be loading this example into the `pets` graph. It contains 9 pets, 4 people and a variety of `owns` relationships.

The first step is to make sure that you have `Grakn.sh` running in the background. Then:

### Load the ontology

```
./graql.sh -f ontology.gql -k pets
```

**Make sure you are using the correct paths from the script to the files.**

### Load the pets

```
./migration.sh csv -t pets.gql -i pets.csv -k pets
```

You will see the response:

```
Graph ontology contains:
	 7 entity types
	 6 relation types
	 12 role types
	 5 resource types
	 2 rule types

Graph data contains:
	 9 entities
	 27 relations
	 21 resources
	 0 rules
```

### Load the people

This is a TSV file so we must specify the separator when calling the script.

```
./migration.sh csv -t person.gql -i person.tsv -s \t -k pets
```

You will see the response:

```
Graph ontology contains:
	 7 entity types
	 6 relation types
	 12 role types
	 5 resource types
	 2 rule types

Graph data contains:
	 13 entities
	 35 relations
	 29 resources
	 0 rules
```

### Load the relations

```
./migration.sh csv -t owns.gql -i owns.csv -k pets
```

You will see the response:

```
Graph ontology contains:
	 7 entity types
	 6 relation types
	 12 role types
	 5 resource types
	 2 rule types

Graph data contains:
	 13 entities
	 44 relations
	 29 resources
	 0 rules
```

### Ask the questions

You can now open Graql and take a look at the data you have migrated.

```
2016-11-10 15:19:14.972:INFO::main: Logging initialized @343ms

GraknDB  Copyright (C) 2016  Grakn Research Ltd
This is free software, and you are welcome to redistribute it
under certain conditions; type 'license' for details.
>>> match $x isa pet;
$x id "ENTITY-snake-7e38d3ca-9b29-4a39-a94e-86646b0add70" isa snake;
$x id "ENTITY-dog-35dc3c02-790f-451e-857a-f57eeeeb7b03" isa dog;
$x id "ENTITY-dog-71797877-e9f4-4df8-a6b7-ab2f40184512" isa dog;
$x id "ENTITY-dog-7b343f24-8b86-4b7a-abe8-5eaecc557c65" isa dog;
$x id "ENTITY-bird-cb8e4179-65d9-4775-abca-10bf060f8064" isa bird;
$x id "ENTITY-bird-06449374-1bfa-4f75-b89d-5634902d5497" isa bird;
$x id "ENTITY-cat-b19f4703-dcc9-40c2-a531-6b5841963b3f" isa cat;
$x id "ENTITY-cat-2dfbfdd9-7662-4adf-ba51-c6f9820824b9" isa cat;
$x id "ENTITY-hamster-e8bc4733-de30-490f-acfc-2dfe99b80824" isa hamster;
>>> match $x isa cat;
$x id "ENTITY-cat-b19f4703-dcc9-40c2-a531-6b5841963b3f" isa cat;
$x id "ENTITY-cat-2dfbfdd9-7662-4adf-ba51-c6f9820824b9" isa cat;
>>> match ($x, $y) isa owns; $x isa cat; $y isa person has name $z; select $z;
$z value "Harold" isa name;
$z value "Gwen" isa name;
>>>
```