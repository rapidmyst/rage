package io.mindmaps.example;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.mindmaps.MindmapsGraph;
import io.mindmaps.concept.Entity;
import io.mindmaps.concept.EntityType;
import io.mindmaps.concept.Instance;
import io.mindmaps.concept.Relation;
import io.mindmaps.concept.RelationType;
import io.mindmaps.concept.Resource;
import io.mindmaps.concept.ResourceType;
import io.mindmaps.concept.RoleType;
import io.mindmaps.concept.Type;
import io.mindmaps.exception.MindmapsValidationException;
import io.mindmaps.factory.MindmapsClient;

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
        mindmapsGraph = MindmapsClient.getGraph(keyspace);

        createSomeConceptTypesAndInstances();

        createSomeRelations();

        createSomeResources();

        createSomeRelationsInsideRelations();

        try{
            mindmapsGraph.commit();
            System.out.println("Results committed. Why not use graql to make some queries ??");
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
        //Create a concept type to represent people
        EntityType person = mindmapsGraph.putEntityType("person");
        //Create a bunch of ancient greeks
        addInstances(person, "Socrates", "Plato", "Aristotle", "Alexander");
        //Print that bunch of ancient greeks
        printInstancesOf(person);
        //Create a concept type to represent schools of philosophy
        EntityType school = mindmapsGraph.putEntityType("school");
        //Create some schools
        addInstances(school, "Peripateticism", "Platonism", "Idealism", "Cynicism");
        //Print those schools
        printInstancesOf(school);
    }

    private static void createSomeRelations(){
        //Start by defining the type of the roles of the relation ship
        RoleType philosopher = mindmapsGraph.putRoleType("philosopher");
        RoleType philosophy = mindmapsGraph.putRoleType("philosophy");

        //Now define the actual relation type
        RelationType practice = mindmapsGraph.putRelationType("practice").hasRole(philosopher).hasRole(philosophy);

        //Find the instances we need:
        Instance socrates = mindmapsGraph.getEntity("Socrates");
        Instance plato = mindmapsGraph.getEntity("Plato");
        Instance aristotle = mindmapsGraph.getEntity("Aristotle");
        Instance platonisim = mindmapsGraph.getEntity("Platonism");

        //Oh wait we need to allow these guys to be philosophers. Luckily they are all of the type people.
        Type person = socrates.type();
        person.playsRole(philosopher);
        //We also need to allow schools to play the role of philosophy
        Type school = mindmapsGraph.getEntityType("school");
        school.playsRole(philosophy);

        //Create the actual relationship instances
        mindmapsGraph.addRelation(practice).putRolePlayer(philosopher, socrates).putRolePlayer(philosophy, platonisim);
        mindmapsGraph.addRelation(practice).putRolePlayer(philosopher, plato).putRolePlayer(philosophy, mindmapsGraph.getEntity("Idealism"));
        mindmapsGraph.addRelation(practice).putRolePlayer(philosopher, plato).putRolePlayer(philosophy, platonisim);
        mindmapsGraph.addRelation(practice).putRolePlayer(philosopher, aristotle).putRolePlayer(philosophy, mindmapsGraph.getEntity("Peripateticism"));

        //Who studies platonism?
        System.out.println("Who practices platonism?");
        platonisim.relations(philosophy).forEach(relation -> {
            relation.rolePlayers().values().forEach(rolePlayer -> {
                if (!rolePlayer.equals(platonisim))
                    System.out.println("    -> " + rolePlayer.getId());
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
                    System.out.println("    -> " + rolePlayer.getId());
            });
        }); //Pssssstttt Graql is much better at querying relationships!!
    }

    private static void createSomeResources(){
        //Lets create some resources first. Lets start with their type
        ResourceType<String> title = mindmapsGraph.putResourceType("title", ResourceType.DataType.STRING);
        ResourceType<String> epithet = mindmapsGraph.putResourceType("epithet", ResourceType.DataType.STRING);

        //Now lets create the actual resource instances:
        Resource<String> theGreat = mindmapsGraph.putResource("The Great", epithet);
        Resource<String> hegemon = mindmapsGraph.putResource("Hegemon", title);
        Resource<String> kingOfMacedon = mindmapsGraph.putResource("King of Macedon", title);
        Resource<String> shahOfPersia = mindmapsGraph.putResource("Shah of Persia", title);
        Resource<String> pharaohOfEgypt = mindmapsGraph.putResource("Pharaoh of Egypt", title);
        Resource<String> lordOfAsia = mindmapsGraph.putResource("Lord of Asia", title);

        //Lets Define a relation type
        RoleType hasResourceTarget = mindmapsGraph.putRoleType("has-resource-target");
        RoleType hasResourceValue = mindmapsGraph.putRoleType("has-resource-value");
        RelationType hasResource = mindmapsGraph.putRelationType("has-resource").hasRole(hasResourceTarget).hasRole(hasResourceValue);

        //Lets Create the relationship instances involving resources.
        Instance alexander = mindmapsGraph.getEntity("Alexander");

        //REMEMBER: We have to give the types of the instances permission to play these roles !
        alexander.type().playsRole(hasResourceTarget);
        title.playsRole(hasResourceValue);
        epithet.playsRole(hasResourceValue);

        //Now lets create the actual relations
        mindmapsGraph.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, theGreat);
        mindmapsGraph.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, hegemon);
        mindmapsGraph.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, kingOfMacedon);
        mindmapsGraph.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, shahOfPersia);
        mindmapsGraph.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, pharaohOfEgypt);
        mindmapsGraph.addRelation(hasResource).putRolePlayer(hasResourceTarget, alexander).putRolePlayer(hasResourceValue, lordOfAsia);

        //Who was the Pharaoh again?
        System.out.println("Who was the Pharaoh again ?");
        pharaohOfEgypt.ownerInstances().forEach(instance -> {
            System.out.println("    ->" +   instance.getId());
        }); //Pssssstttt Graql is much better at querying relationships!!
    }

    private static void createSomeRelationsInsideRelations(){
        //Another relation type is needed
        RoleType thinker = mindmapsGraph.putRoleType("thinker");
        RoleType thought = mindmapsGraph.putRoleType("thought");
        RelationType knowledge = mindmapsGraph.putRelationType("knowledge").hasRole(thinker).hasRole(thought);

        //Lets create a new concept type and at the same time give it permission to play the role
        EntityType fact = mindmapsGraph.putEntityType("fact").playsRole(thought);

        //We can't forget out thinker role:
        Type person = mindmapsGraph.getEntityType("person").playsRole(thinker); // Hey people can think now !

        //Let's get some facts for people to learn
        Entity sunFact = mindmapsGraph.putEntity("sun-fact", fact);
        Entity caveFact = mindmapsGraph.putEntity("cave-fact", fact);
        Entity nothing = mindmapsGraph.putEntity("nothing", fact);

        //You must have thoughts in order to think so lets give our Philosophers some thoughts:
        Entity socrates = mindmapsGraph.getEntity("Socrates");
        Entity plato = mindmapsGraph.getEntity("Plato");
        Entity aristotle = mindmapsGraph.getEntity("Aristotle");

        mindmapsGraph.addRelation(knowledge).putRolePlayer(thinker, aristotle).putRolePlayer(thought, sunFact);
        mindmapsGraph.addRelation(knowledge).putRolePlayer(thinker, plato).putRolePlayer(thought, caveFact);
        Relation socratesKnowsNothing = mindmapsGraph.addRelation(knowledge).putRolePlayer(thinker, socrates).putRolePlayer(thought, nothing);

        //So what's Aristotle thinking ?
        System.out.println("What is Aristotle thinking ?");
        aristotle.relations(thinker).forEach(relation -> {
            relation.rolePlayers().values().forEach(instance -> {
                if(!instance.equals(aristotle))
                    System.out.println("    -> " + instance.getId() + ": " + instance);
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
        Arrays.asList(instances).forEach(instanceId -> mindmapsGraph.putEntity(instanceId, type));
    }

}
