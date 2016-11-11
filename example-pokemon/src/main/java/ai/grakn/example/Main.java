package ai.grakn.example;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.mindmaps.Mindmaps;
import io.mindmaps.MindmapsGraph;
import io.mindmaps.concept.ResourceType;
import io.mindmaps.concept.RoleType;
import io.mindmaps.concept.Type;
import io.mindmaps.exception.MindmapsValidationException;

/**
 * Loads some pokemon. This data is a sample drawn from https://pokeapi.co/. Big Thanks to them.
 */
public class Main {
    private static final String keyspace = "ExamplePokemon";
    private static MindmapsGraph mindmapsGraph;
    private static final String POKE_DIR = "example-pokemon/src/main/resources/";

    public static void main(String [] args){
        disableInternalLogs();

        System.out.println("=================================================================================================");
        System.out.println("|||||||||||||||||||||||||||||||||||   Mindmaps Pokemon Example   ||||||||||||||||||||||||||||||||");
        System.out.println("=================================================================================================");

        //Initialise a new mindmaps graph
        mindmapsGraph = Mindmaps.factory(Mindmaps.IN_MEMORY, keyspace).getGraph();

        System.out.println("Building Poketology (It's punny). . . ");
        buildPoketology();

        System.out.println("Loading Pokemon . . .");

        PokeLoader pokeLoader = new PokeLoader(mindmapsGraph, POKE_DIR);
        pokeLoader.loadPokemon();

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

    private static void buildPoketology(){
        //Creating some PokeRoles
        RoleType ancestor = mindmapsGraph.putRoleType(PokeConstants.ANCESTOR);
        RoleType descendant = mindmapsGraph.putRoleType(PokeConstants.DESCENDANT);
        RoleType pokemonWithType = mindmapsGraph.putRoleType(PokeConstants.POKEMON_WITH_TYPE);
        RoleType typeOfPokemon = mindmapsGraph.putRoleType(PokeConstants.TYPE_OF_POKEMON);

        //Creating some PokeTypes
        Type pokemon = mindmapsGraph.putEntityType(PokeConstants.POKEMON).
                playsRole(ancestor).
                playsRole(descendant).
                playsRole(pokemonWithType);

        Type pokemonType = mindmapsGraph.putEntityType(PokeConstants.POKEMON_TYPE).playsRole(typeOfPokemon);

        //Creating some uh . . .Resource-PokeTypes
        ResourceType<String> name = mindmapsGraph.putResourceType(PokeConstants.NAME, ResourceType.DataType.STRING);
        ResourceType<Long> pokedexNo = mindmapsGraph.putResourceType(PokeConstants.POKEDEX_NO, ResourceType.DataType.LONG);
        ResourceType<String> description = mindmapsGraph.putResourceType(PokeConstants.DESCRIPTION, ResourceType.DataType.STRING);
        ResourceType<Long> height = mindmapsGraph.putResourceType(PokeConstants.HEIGHT, ResourceType.DataType.LONG);
        ResourceType<Long> weight = mindmapsGraph.putResourceType(PokeConstants.WEIGHT, ResourceType.DataType.LONG);

        pokemon.hasResource(name);
        pokemon.hasResource(pokedexNo);
        pokemon.hasResource(description);
        pokemon.hasResource(height);
        pokemon.hasResource(weight);
        pokemonType.hasResource(name);

        //Creating some Relation-PokeTypes
        mindmapsGraph.putRelationType(PokeConstants.EVOLUTION).hasRole(ancestor).hasRole(descendant);
        mindmapsGraph.putRelationType(PokeConstants.HAS_TYPE).hasRole(pokemonWithType).hasRole(typeOfPokemon);
    }

    /**
     * Disables extra logs which come from cassandra when using the TitanFactory
     */
    private static void disableInternalLogs(){
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.OFF);
    }
}
