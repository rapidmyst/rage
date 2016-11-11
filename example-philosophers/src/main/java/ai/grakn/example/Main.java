/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016  Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License:
 * along with Grakn. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package ai.grakn.example;

import ai.grakn.Grakn;
import ai.grakn.GraknGraph;
import ai.grakn.concept.Entity;
import ai.grakn.concept.EntityType;
import ai.grakn.concept.Instance;
import ai.grakn.concept.Relation;
import ai.grakn.concept.RelationType;
import ai.grakn.concept.Resource;
import ai.grakn.concept.ResourceType;
import ai.grakn.concept.RoleType;
import ai.grakn.concept.Rule;
import ai.grakn.concept.Type;
import ai.grakn.exception.GraknValidationException;

import java.util.Arrays;

public class Main {
    private static GraknGraph graknGraph;
    private static final String keyspace = "ExamplePhilosophers";


    public static void main(String [] args){
        System.out.println("=================================================================================================");
        System.out.println("|||||||||||||||||||||||||||||||||   Grakn Philosopher's Example   ||||||||||||||||||||||||||||||||");
        System.out.println("=================================================================================================");

        //Initialise a new mindmaps graph
        graknGraph = Grakn.factory(Grakn.IN_MEMORY, keyspace).getGraph();

        createSomeConceptTypesAndInstances();

        createSomeRelations();

        createSomeResources();

        createSomeRelationsInsideRelations();

        try{
            graknGraph.commit();
            System.out.println("Results committed.");
        } catch (GraknValidationException e){
            System.out.println("Validation errors have occurred during committing:" + e.getMessage());
        } finally {
            try {
                graknGraph.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    private static void createSomeConceptTypesAndInstances(){
        //Create a name resource type to attach information to instances
        ResourceType<String> name = graknGraph.putResourceType("name", ResourceType.DataType.STRING);
        //Create a concept type to represent people
        EntityType person = graknGraph.putEntityType("person");
        person.hasResource(name);
        //Create a bunch of ancient greeks
        addInstances(person, name, "Socrates", "Plato", "Aristotle", "Alexander");
        //Print that bunch of ancient greeks
        printEntitiesOf(person, name);
        //Create a concept type to represent schools of philosophy
        EntityType school = graknGraph.putEntityType("school");
        school.hasResource(name);
        //Create some schools
        addInstances(school, name, "Peripateticism", "Platonism", "Idealism", "Cynicism");
        //Print those schools
        printEntitiesOf(school, name);
    }

    private static void createSomeRelations(){
        ResourceType<String> name = graknGraph.getResourceType("name");

        //Start by defining the type of the roles of the relation ship
        RoleType philosopher = graknGraph.putRoleType("philosopher");
        RoleType philosophy = graknGraph.putRoleType("philosophy");

        //Now define the actual relation type
        RelationType practice = graknGraph.putRelationType("practice").hasRole(philosopher).hasRole(philosophy);

        //Find the instances we need:
        Instance socrates = graknGraph.getResource("Socrates", name).owner();
        Instance plato = graknGraph.getResource("Plato", name).owner();
        Instance aristotle = graknGraph.getResource("Aristotle", name).owner();
        Instance platonisim = graknGraph.getResource("Platonism", name).owner();

        Instance idealism = graknGraph.getResource("Idealism", name).owner();
        Instance peripateticism = graknGraph.getResource("Peripateticism", name).owner();

        //Oh wait we need to allow these guys to be philosophers. Luckily they are all of the type people.
        Type person = socrates.type();
        person.playsRole(philosopher);
        //We also need to allow schools to play the role of philosophy
        Type school = graknGraph.getEntityType("school");
        school.playsRole(philosophy);

        //Create the actual relationship instances
        graknGraph.addRelation(practice).putRolePlayer(philosopher, socrates).putRolePlayer(philosophy, platonisim);
        graknGraph.addRelation(practice).putRolePlayer(philosopher, plato).putRolePlayer(philosophy, idealism);
        graknGraph.addRelation(practice).putRolePlayer(philosopher, plato).putRolePlayer(philosophy, platonisim);
        graknGraph.addRelation(practice).putRolePlayer(philosopher, aristotle).putRolePlayer(philosophy, peripateticism);

        //Who studies platonism?
        System.out.println("Who practices platonism?");
        platonisim.relations(philosophy).forEach(relation -> {
            relation.rolePlayers().values().forEach(rolePlayer -> {
                if (!rolePlayer.equals(platonisim))
                    System.out.println("    -> " + resourceOf(rolePlayer, name));
            });
        }); //Pssssstttt Graql is much better at querying relationships!!

        //Lets define another type of relationship. First some lovely roles:
        RoleType teacher = graknGraph.putRoleType("teacher");
        RoleType student = graknGraph.putRoleType("student");
        RelationType education = graknGraph.putRelationType("education").hasRole(teacher).hasRole(student);

        //WAIT !! People aren't allowed to be teachers and students yet:
        person.playsRole(teacher).playsRole(student);

        //Create the actual relationship instances of the new Relation Type
        graknGraph.addRelation(education).putRolePlayer(teacher, socrates).putRolePlayer(student, plato);
        graknGraph.addRelation(education).putRolePlayer(teacher, plato).putRolePlayer(student, aristotle);
        graknGraph.addRelation(education).putRolePlayer(teacher, aristotle).putRolePlayer(student, graknGraph.getEntity("Alexander"));

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
        ResourceType<String> name = graknGraph.putResourceType("name", ResourceType.DataType.STRING);
        ResourceType<String> title = graknGraph.putResourceType("title", ResourceType.DataType.STRING);
        ResourceType<String> epithet = graknGraph.putResourceType("epithet", ResourceType.DataType.STRING);

        //Let a person have these resources
        EntityType person = graknGraph.getEntityType("person");
        person.hasResource(title);
        person.hasResource(epithet);

        //Now lets create the actual resource instances:
        Resource<String> theGreat = graknGraph.putResource("The Great", epithet);
        Resource<String> hegemon = graknGraph.putResource("Hegemon", title);
        Resource<String> kingOfMacedon = graknGraph.putResource("King of Macedon", title);
        Resource<String> shahOfPersia = graknGraph.putResource("Shah of Persia", title);
        Resource<String> pharaohOfEgypt = graknGraph.putResource("Pharaoh of Egypt", title);
        Resource<String> lordOfAsia = graknGraph.putResource("Lord of Asia", title);

        //Lets Create the relationship instances involving resources.
        Instance alexander = graknGraph.getResource("Alexander", name).owner();

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
        ResourceType<String> name = graknGraph.getResourceType("name");
        RoleType thinker = graknGraph.putRoleType("thinker");
        RoleType thought = graknGraph.putRoleType("thought");
        RelationType knowledge = graknGraph.putRelationType("knowledge").hasRole(thinker).hasRole(thought);

        //Lets create a new concept type and at the same time give it permission to play the role
        EntityType fact = graknGraph.putEntityType("fact").playsRole(thought);

        //Create a new resource type to hold information about the facts
        ResourceType<String> factIsAbout = graknGraph.putResourceType("factIsAbout", ResourceType.DataType.STRING);
        fact.hasResource(factIsAbout);

        //We can't forget out thinker role:
        Type person = graknGraph.getEntityType("person").playsRole(thinker); // Hey people can think now !

        //Let's get some facts for people to learn
        Entity sunFact = graknGraph.addEntity(fact);
        sunFact.hasResource(graknGraph.putResource("sun-fact", factIsAbout));
        Entity caveFact = graknGraph.addEntity(fact);
        sunFact.hasResource(graknGraph.putResource("cave-fact", factIsAbout));
        Entity nothing = graknGraph.addEntity(fact);
        sunFact.hasResource(graknGraph.putResource("nothing", factIsAbout));

        //You must have thoughts in order to think so lets give our Philosophers some thoughts:
        Entity socrates = graknGraph.getResource("Socrates", name).owner().asEntity();
        Entity plato = graknGraph.getResource("Plato", name).owner().asEntity();
        Entity aristotle = graknGraph.getResource("Aristotle", name).owner().asEntity();

        graknGraph.addRelation(knowledge).putRolePlayer(thinker, aristotle).putRolePlayer(thought, sunFact);
        graknGraph.addRelation(knowledge).putRolePlayer(thinker, plato).putRolePlayer(thought, caveFact);
        Relation socratesKnowsNothing = graknGraph.addRelation(knowledge).putRolePlayer(thinker, socrates).putRolePlayer(thought, nothing);

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
        graknGraph.addRelation(knowledge).putRolePlayer(thinker, socrates).putRolePlayer(thought, socratesKnowsNothing);

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
            Resource<String> resource = graknGraph.putResource(instanceId, resourceType);
            graknGraph.addEntity(instanceType).hasResource(resource);
        });
    }

}
