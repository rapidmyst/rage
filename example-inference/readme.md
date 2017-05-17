This example illustrates how to write some very simple inference rules. It is documented in a blog tutorial on [blog.grakn.ai](https://blog.grakn.ai).

The data used comes from a mySQL dataset, which has been [described previously](https://blog.grakn.ai/populating-mindmapsdb-with-the-world-5b2445aee60c). 
There is no need to carry out data migration, as this has been done. You can simply import the ontology and data files directly into a graph.

However if you do want to migrate the data, you need to follow the instructions in this [blog tutorial](https://blog.grakn.ai/populating-mindmapsdb-with-the-world-5b2445aee60c).
Once you have the mySQL server running and world.sql database loaded, you start Grakn engine and load the ontology.  
Then use `./loader.sh <path to GRAKN.AI>` to import the data.


