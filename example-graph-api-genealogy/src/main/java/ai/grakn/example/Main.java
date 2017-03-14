package ai.grakn.example;

import ai.grakn.Grakn;
import ai.grakn.GraknGraph;

/**
 * The purpose of this class is to show you how to build the genealogy graph outlined in:
 * https://grakn.ai/pages/documentation/developing-with-java/graph-api.html
 */
public class Main {
    private static final String keyspace = "genealogy";

    public static void main(String [] args){
        System.out.println("=================================================================================================");
        System.out.println("|||||||||||||||||||||||||||||||||   Grakn Graph API  Example   ||||||||||||||||||||||||||||||||||");
        System.out.println("=================================================================================================");

        System.out.println("Creating graph . . .");
        try(GraknGraph graph = Grakn.factory(Grakn.DEFAULT_URI, keyspace).getGraph()){
            System.out.println("Writing ontology . . .");
            writeOntology();
            System.out.println("Writing data . . .");
            writeData();
        }
        System.out.println("Done");
    }

    public static void writeOntology(GraknGraph graph){

    }

    public static void writeData(GraknGraph graph){

    }
}
