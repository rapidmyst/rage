package io.mindmaps.example;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.mindmaps.MindmapsTransaction;
import io.mindmaps.core.Data;
import io.mindmaps.core.MindmapsGraph;
import io.mindmaps.core.implementation.exception.MindmapsValidationException;
import io.mindmaps.core.model.ResourceType;
import io.mindmaps.core.model.RoleType;
import io.mindmaps.core.model.Type;
import io.mindmaps.factory.MindmapsClient;

/**
 * Loads some pokemon. This data is a sample drawn from https://pokeapi.co/. Big Thanks to them.
 */
public class Main {
    private static final String keyspace = "ExamplePokemon";
    private static MindmapsGraph mindmapsGraph;
    private static MindmapsTransaction mindmapsTransaction;
    private static final String POKE_DIR = "src/main/resources/";

    public static void main(String [] args){
        disableInternalLogs();

        System.out.println("=================================================================================================");
        System.out.println("|||||||||||||||||||||||||||||||||||   Mindmaps Pokemon Example   ||||||||||||||||||||||||||||||||");
        System.out.println("=================================================================================================");

        //Initialise a new mindmaps graph
        mindmapsGraph = MindmapsClient.getGraph(keyspace);

        //Get the transaction
        mindmapsTransaction = mindmapsGraph.getTransaction();

        System.out.println("Building Poketology (It's punny). . . ");
        buildPoketology();

        System.out.println("Loading Pokemons . . .");

        PokeLoader pokeLoader = new PokeLoader(mindmapsTransaction, POKE_DIR);
        pokeLoader.loadPokemon();

        try{
            mindmapsTransaction.commit();
            System.out.println("Results committed. Why not use graql to make some queries ??");
        } catch (MindmapsValidationException e){
            System.out.println("Validation errors have occurred during committing:" + e.getMessage());
        } finally {
            try {
                mindmapsTransaction.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }

    private static void buildPoketology(){
        //Creating some PokeRoles
        RoleType hasResourceTarget = mindmapsTransaction.putRoleType(PokeConstants.HAS_RESOURCE_TARGET);
        RoleType hasResourceValue = mindmapsTransaction.putRoleType(PokeConstants.HAS_RESOURCE_VALUE);
        RoleType ancestor = mindmapsTransaction.putRoleType(PokeConstants.ANCESTOR);
        RoleType descendant = mindmapsTransaction.putRoleType(PokeConstants.DESCENDANT);
        RoleType pokemonWithType = mindmapsTransaction.putRoleType(PokeConstants.POKEMON_WITH_TYPE);
        RoleType typeOfPokemon = mindmapsTransaction.putRoleType(PokeConstants.TYPE_OF_POKEMON);

        //Creating some PokeTypes
        Type pokemon = mindmapsTransaction.putEntityType(PokeConstants.POKEMON).
                playsRole(hasResourceTarget).
                playsRole(ancestor).
                playsRole(descendant).
                playsRole(pokemonWithType);

        Type pokemonType = mindmapsTransaction.putEntityType(PokeConstants.POKEMON_TYPE).playsRole(typeOfPokemon);

        //Creating some uh . . .Resource-PokeTypes
        ResourceType<Long> pokedexNo = mindmapsTransaction.putResourceType(PokeConstants.POKEDEX_NO, Data.LONG).playsRole(hasResourceValue);
        ResourceType<String> description = mindmapsTransaction.putResourceType(PokeConstants.DESCRIPTION, Data.STRING).playsRole(hasResourceValue);
        ResourceType<Long> height = mindmapsTransaction.putResourceType(PokeConstants.HEIGHT, Data.LONG).playsRole(hasResourceValue);
        ResourceType<Long> weight = mindmapsTransaction.putResourceType(PokeConstants.WEIGHT, Data.LONG).playsRole(hasResourceValue);

        //Creating some Relation-PokeTypes
        mindmapsTransaction.putRelationType(PokeConstants.HAS_RESOURCE).hasRole(hasResourceTarget).hasRole(hasResourceValue);
        mindmapsTransaction.putRelationType(PokeConstants.EVOLUTION).hasRole(ancestor).hasRole(descendant);
        mindmapsTransaction.putRelationType(PokeConstants.HAS_TYPE).hasRole(pokemonWithType).hasRole(typeOfPokemon);
    }

    /**
     * Disables extra logs which come from cassandra when using the TitanFactory
     */
    private static void disableInternalLogs(){
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.OFF);
    }
}
