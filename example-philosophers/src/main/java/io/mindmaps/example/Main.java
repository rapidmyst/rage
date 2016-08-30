package io.mindmaps.example;

import io.mindmaps.MindmapsTransaction;
import io.mindmaps.core.Data;
import io.mindmaps.core.MindmapsGraph;
import io.mindmaps.core.implementation.exception.MindmapsValidationException;
import io.mindmaps.core.model.*;
import io.mindmaps.factory.MindmapsClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import java.util.Arrays;

public class Main {
    private static MindmapsGraph mindmapsGraph;
    private static MindmapsTransaction transaction;
    private static final String keyspace = "ExamplePhilosophers";


    public static void main(String [] args){
        disableInternalLogs();

        System.out.println("=================================================================================================");
        System.out.println("|||||||||||||||||||||||||||||||   Mindmaps Philosopher's Example   ||||||||||||||||||||||||||||||");
        System.out.println("=================================================================================================");

        //Initialise a new mindmaps graph
        mindmapsGraph = MindmapsClient.getGraph(keyspace);

        //Get a new transaction
        transaction = mindmapsGraph.getTransaction();

        createSomeConceptTypesAndInstances();

        createSomeRelations();

        createSomeResources();

        createSomeRelationsInsideRelations();

        try{
            transaction.commit();
            System.out.println("Results committed. Why not use graql to make some queries ??");
        } catch (MindmapsValidationException e){
            System.out.println("Validation errors have occurred during committing:" + e.getMessage());
        } finally {
            try {
                transaction.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    private static void createSomeConceptTypesAndInstances(){
        //Create a concept type to represent people
        EntityType person = transaction.putEntityType("person");
        //Create a bunch of ancient greeks
        addInstances(person, "Socrates", "Plato", "Aristotle", "Alexander");
        //Print that bunch of ancient greeks
        printInstancesOf(person);
        //Create a concept type to represent schools of philosophy
        EntityType school = transaction.putEntityType("school");
        //Create some schools
        addInstances(school, "Peripateticism", "Platonism", "Idealism", "Cynicism");
        //Print those schools
        printInstancesOf(school);
    }

    private static void createSomeRelations(){
        //Start by defining the type of the roles of the relation ship
        RoleType philosopher = transaction.putRoleType("philosopher");
        RoleType philosophy = transaction.putRoleType("philosophy");

        //Now define the actual relation type
        RelationType practice = transaction.putRelationType("practice").hasRole(philosopher).hasRole(philosophy);

        //Find the instances we need:
        Instance socrates = transaction.getEntity("Socrates");
        Instance plato = transaction.getEntity("Plato");
        Instance aristotle = transaction.getEntity("Aristotle");
        Instance platonisim = transaction.getEntity("Platonism");

        //Oh wait we need to allow these guys to be philosophers. Luckily they are all of the type people.
        Type person = socrates.type();
        person.playsRole(philosopher);
        //We also need to allow schools to play the role of philosophy
        Type school = transaction.getEntityType("school");
        school.playsRole(philosophy);

        //Create the actual relationship instances
        transaction.addRelation(practice).putRolePlayer(philosopher, socrates).putRolePlayer(philosophy, platonisim);
        transaction.addRelation(practice).putRolePlayer(philosopher, plato).putRolePlayer(philosophy, transaction.getEntity("Idealism"));
        transaction.addRelation(practice).putRolePlayer(philosopher, plato).putRolePlayer(philosophy, platonisim);
        transaction.addRelation(practice).putRolePlayer(philosopher, aristotle).putRolePlayer(philosophy, transaction.getEntity("Peripateticism"));

        //Who studies platonism?
        System.out.println("Who practices platonism?");
        platonisim.relations(philosophy).forEach(relation -> {
            relation.rolePlayers().values().forEach(rolePlayer -> {
                if (!rolePlayer.equals(platonisim))
                    System.out.println("    -> " + rolePlayer.getId());
            });
        }); //Pssssstttt Graql is much better at querying relationships!!

        //Lets define another type of relationship. First some lovely roles:
        RoleType teacher = transaction.putRoleType("teacher");
        RoleType student = transaction.putRoleType("student");
        RelationType education = transaction.putRelationType("education").hasRole(teacher).hasRole(student);

        //WAIT !! People aren't allowed to be teachers and students yet:
        person.playsRole(teacher).playsRole(student);

        //Create the actual relationship instances of the new Relation Type
        transaction.addRelation(education).putRolePlayer(teacher, socrates).putRolePlayer(student, plato);
        transaction.addRelation(education).putRolePlayer(teacher, plato).putRolePlayer(student, aristotle);
        transaction.addRelation(education).putRolePlayer(teacher, aristotle).putRolePlayer(student, transaction.getEntity("Alexander"));

        //Who did Plato teach?
        System.out.println("Who did plato teach ?");
        plato.relations(teacher).forEach(relation -> {
            relation.rolePlayers().values().forEach(rolePlayer -> {
                if (!rolePlayer.equals(plato))
                    System.out.println("    -> " + rolePlayer.getId());
            });
        }); //Pssssstttt Graql is much better at querying relationships!!
    }

    private static void createSomeResources(){
        //Lets create some resources first. Lets start with their type
        ResourceType<String> title = transaction.putResourceType("title", Data.STRING);
        ResourceType<String> epithet = transaction.putResourceType("epithet", Data.STRING);

        //Now lets create the actual resource instances:
        Resource<String> theGreat = transaction.putResource("The Great", epithet);
        Resource<String> hegemon = transaction.putResource("Hegemon", title);
        Resource<String> kingOfMacedon = transaction.putResource("King of Macedon", title);
        Resource<String> shahOfPersia = transaction.putResource("Shah of Persia", title);
        Resource<String> pharaohOfEgypt = transaction.putResource("Pharaoh of Egypt", title);
        Resource<String> lordOfAsia = transaction.putResource("Lord of Asia", title);

        //Lets Define a relation type
        RoleType hasResourceTarget = transaction.putRoleType("has-resource-target");
        RoleType hasResourceValue = transaction.putRoleType("has-resource-value");
        RelationType hasResource = transaction.putRelationType("has-resource").hasRole(hasResourceTarget).hasRole(hasResourceValue);

        //Lets Create the relationship instances involving resources.
        Instance alexander = transaction.getEntity("Alexander");

        //REMEMBER: We have to give the types of the instances permission to play these roles !
        alexander.type().playsRole(hasResourceTarget);
        title.playsRole(hasResourceValue);
        epithet.playsRole(hasResourceValue);

        //Now lets create the actual relations
        transaction.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, theGreat);
        transaction.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, hegemon);
        transaction.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, kingOfMacedon);
        transaction.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, shahOfPersia);
        transaction.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, pharaohOfEgypt);
        transaction.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, lordOfAsia);

        //Who was the Pharaoh again?
        System.out.println("Who was the Pharaoh again ?");
        pharaohOfEgypt.ownerInstances().forEach(instance -> {
            System.out.println("    ->" +   instance.getId());
        }); //Pssssstttt Graql is much better at querying relationships!!
    }

    private static void createSomeRelationsInsideRelations(){
        //Another relation type is needed
        RoleType thinker = transaction.putRoleType("thinker");
        RoleType thought = transaction.putRoleType("thought");
        RelationType knowledge = transaction.putRelationType("knowledge").hasRole(thinker).hasRole(thought);

        //Lets create a new concept type and at the same time give it permission to play the role
        EntityType fact = transaction.putEntityType("fact").playsRole(thought);

        //We can't forget out thinker role:
        Type person = transaction.getEntityType("person").playsRole(thinker); // Hey people can think now !

        //Let's get some facts for people to learn
        Entity sunFact = transaction.putEntity("sun-fact", fact).setValue("The Sun is bigger than the Earth");
        Entity caveFact = transaction.putEntity("cave-fact", fact).setValue("Caves are mostly pretty dark");
        Entity nothing = transaction.putEntity("nothing", fact);

        //You must have thoughts in order to think so lets give our Philosophers some thoughts:
        Entity socrates = transaction.getEntity("Socrates");
        Entity plato = transaction.getEntity("Plato");
        Entity aristotle = transaction.getEntity("Aristotle");

        transaction.addRelation(knowledge).putRolePlayer(thinker, aristotle).putRolePlayer(thought, sunFact);
        transaction.addRelation(knowledge).putRolePlayer(thinker, plato).putRolePlayer(thought, caveFact);
        Relation socratesKnowsNothing = transaction.addRelation(knowledge).putRolePlayer(thinker, socrates).putRolePlayer(thought, nothing).setValue("Socrates knows nothing");

        //So what's Aristotle thinking ?
        System.out.println("What is Aristotle thinking ?");
        aristotle.relations(thinker).forEach(relation -> {
            relation.rolePlayers().values().forEach(instance -> {
                if(!instance.equals(aristotle))
                    System.out.println("    -> " + instance.getId() + ": " + instance.getValue());
            });
        }); // Seriously, graql is so much better at querying our graph.

        //So according to this ,Socrates knows nothing but he also knew he knew nothing. Lets model that . . .
        //First knowledge itself must be allowed to be a thought:
        knowledge.playsRole(thought);

        //Now lets actually make Socrates know he knew nothing. . .
        transaction.addRelation(knowledge).putRolePlayer(thinker, socrates).putRolePlayer(thought, socratesKnowsNothing);

        //So what does Socrates know ?
        System.out.println("What is Socrates thinking ?");
        socrates.relations(thinker).forEach(relation -> {
            relation.rolePlayers().values().forEach(instance -> {
                if(!instance.equals(socrates))
                    System.out.println("    -> " + instance.getId() + ": " + instance.getValue());
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
     */
    private static void printInstancesOf(Type type){
        System.out.println("Instances of Concept Type [" + type.getId() + "]:");
        type.instances().forEach(instance -> System.out.println("    Instance: " + instance.getId()));
    }

    /**
     * Creates instances of a specific concept type
     * @param type The concept type which will get new instances
     * @param instances A list of string ids which will be the ids of the new instances
     */
    private static void addInstances(EntityType type, String ... instances){
        Arrays.asList(instances).forEach(instanceId -> transaction.putEntity(instanceId, type));
    }

}
