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

import ai.grakn.engine.loader.BlockingLoader;
import ai.grakn.engine.loader.Loader;
import ai.grakn.graql.Graql;
import ai.grakn.migration.base.Migrator;
import ai.grakn.migration.base.io.MigrationLoader;
import ai.grakn.migration.json.JsonMigrator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;

public class Main {

    private static final String GRAPH_NAME = "giphy";
    private static final String DATA_DIR = "trending";
    private static final String TEMPLATE = "template.gql";
    private static final String ONTOLOGY = "ontology.gql";

    private static final Loader loader = new BlockingLoader(GRAPH_NAME);

    public static void main(String[] args){

        try {
            // load your ontology
            loadOntology();

            // get resources
            String template = getResourceAsString(TEMPLATE);
            File jsonData = new File(getResource(DATA_DIR).toString());

            // create a migrator with your macro
            Migrator migrator = new JsonMigrator(template, jsonData)
                    .registerMacro(new GiphyMacro());

            System.out.println("Beginning migration");

            // load data in directory
            MigrationLoader.load(loader, migrator);

            System.out.println("Migration complete");
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        System.exit(0);
    }

    public static void loadOntology() throws IOException {
        String ontology = getResourceAsString(ONTOLOGY);

        loader.add(Graql.parse(ontology));
        loader.flush();
        loader.waitToFinish();
    }

    public static Path getResource(String resourceName){
        return Paths.get(Main.class.getClassLoader().getResource(resourceName).getPath());
    }

    public static String getResourceAsString(String resourceName) throws IOException {
        return Files.readAllLines(getResource(resourceName)).stream().collect(joining("\n"));
    }
}
