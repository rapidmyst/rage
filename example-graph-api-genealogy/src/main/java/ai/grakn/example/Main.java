package ai.grakn.example;

import ai.grakn.Grakn;
import ai.grakn.GraknGraph;
import ai.grakn.GraknGraphFactory;
import ai.grakn.client.Client;
import ai.grakn.concept.Concept;
import ai.grakn.concept.Entity;
import ai.grakn.concept.EntityType;
import ai.grakn.concept.Instance;
import ai.grakn.concept.RelationType;
import ai.grakn.concept.Relation;
import ai.grakn.concept.Resource;
import ai.grakn.concept.ResourceType;
import ai.grakn.concept.RoleType;
import ai.grakn.graql.QueryBuilder;
import ai.grakn.exception.GraknValidationException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static ai.grakn.graql.Graql.var;
import static ai.grakn.graql.Graql.*;

/**
 * The purpose of this example is to show you how to build the ontology outlined in:
 * https://grakn.ai/pages/documentation/developing-with-java/graph-api.html
 * The example can be used as a basic template for your own projects that create
 * a graph by building an ontology and adding data.
 *
 */

public class Main {
    private static final String SERVER_ADDRESS = "127.0.0.1:4567";
    private static final String keyspace = "genealogy";

    //Roles
    private static RoleType spouse;
    private static RoleType spouse1;
    private static RoleType spouse2;
    private static RoleType parent;
    private static RoleType child;

    //Resource Types
    private static ResourceType<String> gender;
    private static ResourceType<String> date;
    private static ResourceType<String> birthDate;
    private static ResourceType<String> deathDate;
    private static ResourceType<String> name;
    private static ResourceType<String> firstname;
    private static ResourceType<String> middlename;
    private static ResourceType<String> surname;
    private static ResourceType<String> identifier;


    //Relation Types
    private static RelationType marriage;
    private static RelationType parentship;

    //Entity Types
    private static EntityType person;

    public static void main(String [] args){
        //Disable internal logging
        ((Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.OFF);

        if(!Client.serverIsRunning(SERVER_ADDRESS)) {
            System.out.println("Please start Grakn Engine");
            System.out.println("You can get more information on how to do so using our setup guide: https://grakn.ai/pages/documentation/get-started/setup-guide.html");
            return;
        }
    
        System.out.println("=================================================================================================");
        System.out.println("|||||||||||||||||||||||||||||||||   Grakn Graph API  Example   ||||||||||||||||||||||||||||||||||");
        System.out.println("=================================================================================================");

        System.out.println("Creating graph . . .");
        GraknGraphFactory factory = Grakn.factory(Grakn.DEFAULT_URI, keyspace);

        //Simple example of working with a graph in a single thread.
        System.out.println("\n-------------------- Running Simple Example --------------------");
        try(GraknGraph graph = factory.getGraph()){
            System.out.println("Writing ontology . . .");
            writeOntology(graph);
            System.out.println("Writing sample marriage . . .");
            writeSampleRelation_Marriage();
            System.out.println("Writing sample parentship . . .");
            writeSampleRelation_Parentship();
            System.out.println("Running sample queries . . .");
            runSampleQueries(graph);
            graph.commit();
        } catch (GraknValidationException e) {
            e.printStackTrace();
        }

        /*
        System.out.println("\n-------------------- Running Multithreaded Example --------------------");
        //This section demonstrates simple how to work with multiple transactions
        //Lets say we want to add 100 people each with their own name
        transactionHandlingSample_WritingManyPeople(factory);

        try(GraknGraph graph = factory.getGraph()){
            runSampleQuery_People(graph);
        }
*/

        System.out.println("Done");
    }

    /**
     * Writes the genealogy ontology in a simple manner
     */
    private static void writeOntology(GraknGraph graph){
        //Resource Types
        identifier = graph.putResourceType("identifier", ResourceType.DataType.STRING);
        name = graph.putResourceType("name", ResourceType.DataType.STRING);
        firstname = graph.putResourceType("firstname", ResourceType.DataType.STRING).superType(name);
        surname = graph.putResourceType("surname", ResourceType.DataType.STRING).superType(name);
        middlename = graph.putResourceType("middlename", ResourceType.DataType.STRING).superType(name);
        date = graph.putResourceType("date", ResourceType.DataType.STRING);
        birthDate = graph.putResourceType("birth-date", ResourceType.DataType.STRING).superType(date);
        deathDate = graph.putResourceType("death-date", ResourceType.DataType.STRING).superType(date);
        gender = graph.putResourceType("gender", ResourceType.DataType.STRING);

        //Roles
        spouse = graph.putRoleType("spouse").setAbstract(true);
        spouse1 = graph.putRoleType("spouse1").superType(spouse);
        spouse2 = graph.putRoleType("spouse2").superType(spouse);
        parent = graph.putRoleType("parent");
        child = graph.putRoleType("child");

        //Relation Types
        marriage = graph.putRelationType("marriage");
        marriage.hasRole(spouse1).hasRole(spouse2);
        marriage.hasResource(date);
        parentship = graph.putRelationType("parentship");
        parentship.hasRole(parent).hasRole(child);

        //Entity Types
        person = graph.putEntityType("person");
        person.playsRole(spouse1).playsRole(spouse2).playsRole(parent).playsRole(child);
        person.hasResource(gender);
        person.hasResource(birthDate);
        person.hasResource(deathDate);
        person.hasResource(identifier);
        person.hasResource(firstname);
        person.hasResource(middlename);
        person.hasResource(surname);
    }

    /**
     * Writes an example of a marriage relationship including all the entities and resources needed
     */
    private static void writeSampleRelation_Marriage(){
        //Adding a sample marriage
        //Lets create a husband
        //But first we need to define some resource which describe the husband
        Resource<String> homerFirstName = firstname.putResource("Homer");
        Resource<String> jMiddleName = middlename.putResource("J");
        Resource<String> simpsonSurname = surname.putResource("Simpson");
        Resource<String> male = gender.putResource("Male");

        //Now we can create the actual husband entity
        Entity homer = person.addEntity();
        homer.hasResource(homerFirstName);
        homer.hasResource(jMiddleName);
        homer.hasResource(simpsonSurname);
        homer.hasResource(male);

        //Now lets create the wife
        //Again we need to define a few extra resources exclusive to the wife
        Resource<String> margeFirstName = firstname.putResource("Marge");
        Resource<String> female = gender.putResource("Female");

        //Turns out we can use some of our existing resources to create the wife
        Entity marge = person.addEntity();
        marge.hasResource(margeFirstName);
        marge.hasResource(jMiddleName);
        marge.hasResource(simpsonSurname);
        marge.hasResource(female);

        //I now pronounce you husband and wife:
        Relation homerAndMargeMarriage = marriage.addRelation().putRolePlayer(spouse1, homer).putRolePlayer(spouse2, marge);

        Resource marriageDate = date.putResource("12/08/1980");
        homerAndMargeMarriage.hasResource(marriageDate);
    }

    /**
     * Writes an example of a parentship relationship including all the entities and resources needed
     */
    private static void writeSampleRelation_Parentship(){
        //Now lets say our couple had a child.
        //Lets first create that child: Bart
        Resource<String> bartFirstName = firstname.putResource("Bart");
        Resource<String> jMiddleName = middlename.putResource("J"); //This resource already exists so we just getting it back
        Resource<String> simpsonSurname = surname.putResource("Simpson"); //Same for this one
        Resource<String> male = gender.putResource("Male"); //and this one

        //Let's create Bart
        Entity bart = person.addEntity();
        bart.hasResource(bartFirstName);
        bart.hasResource(jMiddleName);
        bart.hasResource(simpsonSurname);
        bart.hasResource(male);

        //Let's get the parents back
        //We know they have unique first name so we will use those to get them
        Instance homer = firstname.putResource("Homer").owner();
        Instance marge = firstname.putResource("Marge").owner();

        //Congratulations! You have a son
        parentship.addRelation().putRolePlayer(parent, homer).putRolePlayer(child, bart);
        parentship.addRelation().putRolePlayer(parent, marge).putRolePlayer(child, bart);
    }

    /**
     * This execute some sample queries and lookups using the Graph API and Graql queries using QueryBuilder
     */
    private static void runSampleQueries(GraknGraph graph){
        System.out.println("What are the instances of the type person?");
        System.out.println("    Using Graph API: ");
        graph.getEntityType("person").instances().forEach(p-> System.out.println("    " + p));

        System.out.println("    Using Graql QueryBuilder: ");
        QueryBuilder qb = graph.graql();
        qb.match(var("x").isa("person")).execute().stream().
                map(Map::entrySet).forEach(p-> System.out.println("    " + p));
        System.out.println();

        System.out.println("Who is married to Homer?");
        //This query is too complex to be solved via a simple lookup. In this case we must query with Graql.
        System.out.println("    Using Graql QueryBuilder: ");

        List<Map<String, Concept>> results = qb.match(
                var("x").has("firstname", "Homer").isa("person"),
                var("y").has("firstname", var("y_name")).isa("person"),
                var().isa("marriage").
                        rel("spouse1", "x").
                        rel("spouse2", "y")).execute();
        for (Map<String, Concept> result : results) {
            System.out.println("    " + result.get("y_name"));
        }

        // Some more queries using QueryBuilder.
        // All the Simpsons.
        System.out.println("The Simpsons");
        qb.match(var("x").has("surname", contains("Simp"))).execute().stream().
                map(Map::entrySet).forEach(p-> System.out.println("    " + p));
        System.out.println();

    }

    /**
     *
     * @param factory The factory bound to a specific keyspace
     */
    private static void transactionHandlingSample_WritingManyPeople(GraknGraphFactory factory){
        ExecutorService pool = Executors.newCachedThreadPool();
        HashSet<Future> futures = new HashSet<>();

        for(int i = 0; i < 10; i ++) {
            int finalI = i;
            futures.add(pool.submit(() -> writeRandomPerson(factory, finalI)));
        }

        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
    private static void writeRandomPerson(GraknGraphFactory factory, int personNumber){
        try(GraknGraph graph = factory.getGraph()) {//Each thread gets it's own thread bound transaction
            Entity randomPerson = graph.getEntityType("person").addEntity();
            Resource<Object> randomPersonName = graph.getResourceType("firstname").putResource("Name " + personNumber);
            randomPerson.hasResource(randomPersonName);
            graph.commit();
        } catch (GraknValidationException e) {
            e.printStackTrace();
        }
    }

    private static void runSampleQuery_People(GraknGraph graph){
        System.out.println("Which people do we have now ?");
        graph.getEntityType("person").instances().forEach(p-> System.out.println("    " + p));
    }
}
