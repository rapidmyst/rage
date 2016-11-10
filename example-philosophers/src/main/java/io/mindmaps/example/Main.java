package io.mindmaps.example;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.mindmaps.Mindmaps;
import io.mindmaps.MindmapsGraph;
import io.mindmaps.concept.Entity;
import io.mindmaps.concept.EntityType;
import io.mindmaps.concept.Instance;
import io.mindmaps.concept.Relation;
import io.mindmaps.concept.RelationType;
import io.mindmaps.concept.Resource;
import io.mindmaps.concept.ResourceType;
import io.mindmaps.concept.RoleType;
import io.mindmaps.concept.Rule;
import io.mindmaps.concept.Type;
import io.mindmaps.exception.MindmapsValidationException;

import java.util.Arrays;

public class Main {
    private static MindmapsGraph mindmapsGraph;
    private static final String keyspace = "ExamplePhilosophers";


    public static void main(String [] args){
        disableInternalLogs();

        System.out.println("=================================================================================================");
        System.out.println("|||||||||||||||||||||||||||||||   Mindmaps Philosopher's Example   ||||||||||||||||||||||||||||||");
        System.out.println("=================================================================================================");

        //Initialise a new mindmaps graph
        mindmapsGraph = Mindmaps.factory(Mindmaps.IN_MEMORY, keyspace).getGraph();

        createSomeConceptTypesAndInstances();

        createSomeRelations();

        createSomeResources();

        createSomeRelationsInsideRelations();

        try{
            mindmapsGraph.commit();
            System.out.println("Results committed.");
        } catch (MindmapsValidationException e){
            System.out.println("Validation errors have occurred during committing:" + e.getMessage());
        } finally {
            try {
                mindmapsGraph.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    private static void createSomeConceptTypesAndInstances(){
        //Create a name resource type to attach information to instances
        ResourceType<String> name = mindmapsGraph.putResourceType("name", ResourceType.DataType.STRING);
        //Create a concept type to represent people
        EntityType person = mindmapsGraph.putEntityType("person");
        person.hasResource(name);
        //Create a bunch of ancient greeks
        addInstances(person, name, "Socrates", "Plato", "Aristotle", "Alexander");
        //Print that bunch of ancient greeks
        printEntitiesOf(person, name);
        //Create a concept type to represent schools of philosophy
        EntityType school = mindmapsGraph.putEntityType("school");
        school.hasResource(name);
        //Create some schools
        addInstances(school, name, "Peripateticism", "Platonism", "Idealism", "Cynicism");
        //Print those schools
        printEntitiesOf(school, name);
    }

    private static void createSomeRelations(){
        ResourceType<String> name = mindmapsGraph.getResourceType("name");

        //Start by defining the type of the roles of the relation ship
        RoleType philosopher = mindmapsGraph.putRoleType("philosopher");
        RoleType philosophy = mindmapsGraph.putRoleType("philosophy");

        //Now define the actual relation type
        RelationType practice = mindmapsGraph.putRelationType("practice").hasRole(philosopher).hasRole(philosophy);

        //Find the instances we need:
        Instance socrates = mindmapsGraph.getResource("Socrates", name).owner();
        Instance plato = mindmapsGraph.getResource("Plato", name).owner();
        Instance aristotle = mindmapsGraph.getResource("Aristotle", name).owner();
        Instance platonisim = mindmapsGraph.getResource("Platonism", name).owner();

        Instance idealism = mindmapsGraph.getResource("Idealism", name).owner();
        Instance peripateticism = mindmapsGraph.getResource("Peripateticism", name).owner();

        //Oh wait we need to allow these guys to be philosophers. Luckily they are all of the type people.
        Type person = socrates.type();
        person.playsRole(philosopher);
        //We also need to allow schools to play the role of philosophy
        Type school = mindmapsGraph.getEntityType("school");
        school.playsRole(philosophy);

        //Create the actual relationship instances
        mindmapsGraph.addRelation(practice).putRolePlayer(philosopher, socrates).putRolePlayer(philosophy, platonisim);
        mindmapsGraph.addRelation(practice).putRolePlayer(philosopher, plato).putRolePlayer(philosophy, idealism);
        mindmapsGraph.addRelation(practice).putRolePlayer(philosopher, plato).putRolePlayer(philosophy, platonisim);
        mindmapsGraph.addRelation(practice).putRolePlayer(philosopher, aristotle).putRolePlayer(philosophy, peripateticism);

        //Who studies platonism?
        System.out.println("Who practices platonism?");
        platonisim.relations(philosophy).forEach(relation -> {
            relation.rolePlayers().values().forEach(rolePlayer -> {
                if (!rolePlayer.equals(platonisim))
                    System.out.println("    -> " + resourceOf(rolePlayer, name));
            });
        }); //Pssssstttt Graql is much better at querying relationships!!

        //Lets define another type of relationship. First some lovely roles:
        RoleType teacher = mindmapsGraph.putRoleType("teacher");
        RoleType student = mindmapsGraph.putRoleType("student");
        RelationType education = mindmapsGraph.putRelationType("education").hasRole(teacher).hasRole(student);

        //WAIT !! People aren't allowed to be teachers and students yet:
        person.playsRole(teacher).playsRole(student);

        //Create the actual relationship instances of the new Relation Type
        mindmapsGraph.addRelation(education).putRolePlayer(teacher, socrates).putRolePlayer(student, plato);
        mindmapsGraph.addRelation(education).putRolePlayer(teacher, plato).putRolePlayer(student, aristotle);
        mindmapsGraph.addRelation(education).putRolePlayer(teacher, aristotle).putRolePlayer(student, mindmapsGraph.getEntity("Alexander"));

        //Who did Plato teach?
        System.out.println("Who did plato teach ?");
        plato.relations(teacher).forEach(relation -> {
            relation.rolePlayers().values().forEach(rolePlayer -> {
                if (!rolePlayer.equals(plato))
                    System.out.println("    -> " + resourceOf(rolePlayer, name));
            });
        }); //Pssssstttt Graql is much better at querying relationships!!
    }

    private static void createSomeResources(){
        //Lets create some resources first. Lets start with their type
        ResourceType<String> name = mindmapsGraph.putResourceType("name", ResourceType.DataType.STRING);
        ResourceType<String> title = mindmapsGraph.putResourceType("title", ResourceType.DataType.STRING);
        ResourceType<String> epithet = mindmapsGraph.putResourceType("epithet", ResourceType.DataType.STRING);

        //Let a person have these resources
        EntityType person = mindmapsGraph.getEntityType("person");
        person.hasResource(title);
        person.hasResource(epithet);

        //Now lets create the actual resource instances:
        Resource<String> theGreat = mindmapsGraph.putResource("The Great", epithet);
        Resource<String> hegemon = mindmapsGraph.putResource("Hegemon", title);
        Resource<String> kingOfMacedon = mindmapsGraph.putResource("King of Macedon", title);
        Resource<String> shahOfPersia = mindmapsGraph.putResource("Shah of Persia", title);
        Resource<String> pharaohOfEgypt = mindmapsGraph.putResource("Pharaoh of Egypt", title);
        Resource<String> lordOfAsia = mindmapsGraph.putResource("Lord of Asia", title);

        //Lets Create the relationship instances involving resources.
        Instance alexander = mindmapsGraph.getResource("Alexander", name).owner();

        //Now lets create the actual relations
        alexander.hasResource(theGreat);
        alexander.hasResource(hegemon);
        alexander.hasResource(kingOfMacedon);
        alexander.hasResource(shahOfPersia);
        alexander.hasResource(pharaohOfEgypt);
        alexander.hasResource(lordOfAsia);

        //Who was the Pharaoh again?
        System.out.println("Who was the Pharaoh again ?");
        pharaohOfEgypt.ownerInstances().forEach(instance -> {
            System.out.println("    ->" +   resourceOf(instance, name));
        }); //Pssssstttt Graql is much better at querying relationships!!
    }

    private static void createSomeRelationsInsideRelations(){
        //Another relation type is needed
        ResourceType<String> name = mindmapsGraph.getResourceType("name");
        RoleType thinker = mindmapsGraph.putRoleType("thinker");
        RoleType thought = mindmapsGraph.putRoleType("thought");
        RelationType knowledge = mindmapsGraph.putRelationType("knowledge").hasRole(thinker).hasRole(thought);

        //Lets create a new concept type and at the same time give it permission to play the role
        EntityType fact = mindmapsGraph.putEntityType("fact").playsRole(thought);

        //Create a new resource type to hold information about the facts
        ResourceType<String> factIsAbout = mindmapsGraph.putResourceType("factIsAbout", ResourceType.DataType.STRING);
        fact.hasResource(factIsAbout);

        //We can't forget out thinker role:
        Type person = mindmapsGraph.getEntityType("person").playsRole(thinker); // Hey people can think now !

        //Let's get some facts for people to learn
        Entity sunFact = mindmapsGraph.addEntity(fact);
        sunFact.hasResource(mindmapsGraph.putResource("sun-fact", factIsAbout));
        Entity caveFact = mindmapsGraph.addEntity(fact);
        sunFact.hasResource(mindmapsGraph.putResource("cave-fact", factIsAbout));
        Entity nothing = mindmapsGraph.addEntity(fact);
        sunFact.hasResource(mindmapsGraph.putResource("nothing", factIsAbout));

        //You must have thoughts in order to think so lets give our Philosophers some thoughts:
        Entity socrates = mindmapsGraph.getResource("Socrates", name).owner().asEntity();
        Entity plato = mindmapsGraph.getResource("Plato", name).owner().asEntity();
        Entity aristotle = mindmapsGraph.getResource("Aristotle", name).owner().asEntity();

        mindmapsGraph.addRelation(knowledge).putRolePlayer(thinker, aristotle).putRolePlayer(thought, sunFact);
        mindmapsGraph.addRelation(knowledge).putRolePlayer(thinker, plato).putRolePlayer(thought, caveFact);
        Relation socratesKnowsNothing = mindmapsGraph.addRelation(knowledge).putRolePlayer(thinker, socrates).putRolePlayer(thought, nothing);

        //So what's Aristotle thinking ?
        System.out.println("What is Aristotle thinking ?");
        aristotle.relations(thinker).forEach(relation -> {
            relation.rolePlayers().values().forEach(instance -> {
                if(!instance.equals(aristotle))
                    System.out.println("    -> " + resourceOf(instance, factIsAbout) + ": " + instance);
            });
        }); // Seriously, graql is so much better at querying our graph.

        //So according to this ,Socrates knows nothing but he also knew he knew nothing. Lets model that . . .
        //First knowledge itself must be allowed to be a thought:
        knowledge.playsRole(thought);

        //Now lets actually make Socrates know he knew nothing. . .
        mindmapsGraph.addRelation(knowledge).putRolePlayer(thinker, socrates).putRolePlayer(thought, socratesKnowsNothing);

        //So what does Socrates know ?
        System.out.println("What is Socrates thinking ?");
        socrates.relations(thinker).forEach(relation -> {
            relation.rolePlayers().values().forEach(instance -> {
                if(!instance.equals(socrates))
                    System.out.println("    -> " + instance.getId() + ": " + instance);
            });
        }); // Graql + querying = joy.
    }

    /**
     * Disables extra lo    gs which come from cassandra when using the TitanFactory
     */
    private static void disableInternalLogs(){
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.OFF);
    }

    /**
     * Prints the instances of a concept type
     * @param type The concept type whose instances we are retrieving
     * @param resourceToPrint Resource containing information about the instance
     */
    private static void printEntitiesOf(EntityType type, ResourceType resourceToPrint){
        System.out.println("Instances of Concept Type [" + type.getId() + "]:");
        type.instances().forEach(instance -> System.out.println("    Instance: " + resourceOf(instance, resourceToPrint)));
    }

    private static Object resourceOf(Instance instance, ResourceType resourceToPrint){
        if(instance instanceof Entity) {
            return instance.asEntity().resources(resourceToPrint).iterator().next().getValue();
        } else if(instance instanceof Relation){
            return instance.asRelation().resources(resourceToPrint).iterator().next().getValue();
        } else if(instance instanceof Rule){
            return instance.asRule().resources(resourceToPrint).iterator().next().getValue();
        }

        return "";
    }

    /**
     * Creates instances of a specific concept type
     * @param instanceType The concept type which will get new instances
     * @param resourceType The resource type which will be attached to new instances
     * @param instances A list of string ids which will be the ids of the new instances
     */
    private static void addInstances(EntityType instanceType, ResourceType<String> resourceType, String ... instances){
        Arrays.asList(instances).forEach(instanceId -> {
            Resource<String> resource = mindmapsGraph.putResource(instanceId, resourceType);
            mindmapsGraph.addEntity(instanceType).hasResource(resource);
        });
    }

}
