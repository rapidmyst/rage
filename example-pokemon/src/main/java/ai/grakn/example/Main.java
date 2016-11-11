package ai.grakn.example;

import ai.grakn.Grakn;
import ai.grakn.GraknGraph;
import ai.grakn.concept.ResourceType;
import ai.grakn.concept.RoleType;
import ai.grakn.concept.Type;
import ai.grakn.exception.GraknValidationException;

/**
 * Loads some pokemon. This data is a sample drawn from https://pokeapi.co/. Big Thanks to them.
 */
public class Main {
    private static final String keyspace = "ExamplePokemon";
    private static GraknGraph graknGraph;
    private static final String POKE_DIR = "example-pokemon/src/main/resources/";

    public static void main(String [] args){
        System.out.println("=================================================================================================");
        System.out.println("||||||||||||||||||||||||||}}|||||||||   Grakn Pokemon Example   ||||||||||||||||||||||||||||||||||");
        System.out.println("=================================================================================================");

        //Initialise a new grakn graph
        graknGraph = Grakn.factory(Grakn.IN_MEMORY, keyspace).getGraph();

        System.out.println("Building Poketology (It's punny). . . ");
        buildPoketology();

        System.out.println("Loading Pokemon . . .");

        PokeLoader pokeLoader = new PokeLoader(graknGraph, POKE_DIR);
        pokeLoader.loadPokemon();

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

    private static void buildPoketology(){
        //Creating some PokeRoles
        RoleType ancestor = graknGraph.putRoleType(PokeConstants.ANCESTOR);
        RoleType descendant = graknGraph.putRoleType(PokeConstants.DESCENDANT);
        RoleType pokemonWithType = graknGraph.putRoleType(PokeConstants.POKEMON_WITH_TYPE);
        RoleType typeOfPokemon = graknGraph.putRoleType(PokeConstants.TYPE_OF_POKEMON);

        //Creating some PokeTypes
        Type pokemon = graknGraph.putEntityType(PokeConstants.POKEMON).
                playsRole(ancestor).
                playsRole(descendant).
                playsRole(pokemonWithType);

        Type pokemonType = graknGraph.putEntityType(PokeConstants.POKEMON_TYPE).playsRole(typeOfPokemon);

        //Creating some uh . . .Resource-PokeTypes
        ResourceType<String> name = graknGraph.putResourceType(PokeConstants.NAME, ResourceType.DataType.STRING);
        ResourceType<Long> pokedexNo = graknGraph.putResourceType(PokeConstants.POKEDEX_NO, ResourceType.DataType.LONG);
        ResourceType<String> description = graknGraph.putResourceType(PokeConstants.DESCRIPTION, ResourceType.DataType.STRING);
        ResourceType<Long> height = graknGraph.putResourceType(PokeConstants.HEIGHT, ResourceType.DataType.LONG);
        ResourceType<Long> weight = graknGraph.putResourceType(PokeConstants.WEIGHT, ResourceType.DataType.LONG);

        pokemon.hasResource(name);
        pokemon.hasResource(pokedexNo);
        pokemon.hasResource(description);
        pokemon.hasResource(height);
        pokemon.hasResource(weight);
        pokemonType.hasResource(name);

        //Creating some Relation-PokeTypes
        graknGraph.putRelationType(PokeConstants.EVOLUTION).hasRole(ancestor).hasRole(descendant);
        graknGraph.putRelationType(PokeConstants.HAS_TYPE).hasRole(pokemonWithType).hasRole(typeOfPokemon);
    }
}
