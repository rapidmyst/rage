/*
 * MindmapsDB - A Distributed Semantic Database
 * Copyright (C) 2016  Mindmaps Research Ltd
 *
 * MindmapsDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MindmapsDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MindmapsDB. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.mindmaps.MindmapsGraph;
import io.mindmaps.engine.MindmapsEngineServer;
import io.mindmaps.engine.util.ConfigProperties;
import io.mindmaps.factory.GraphFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class MainInMemory {

    private static final String worldFile = "world.sql";

    private static final String user = "test";
    private static final String pass = "";
    private static final String driver = "org.h2.Driver";
    private static final String url = "jdbc:h2:~/test;";

    private static final String keyspace = "WORLD";

    public static void main(String[] args){
        disableInternalLogs();

        System.setProperty(ConfigProperties.CONFIG_FILE_SYSTEM_PROPERTY,ConfigProperties.TEST_CONFIG_FILE);

        System.out.println("=================================================================================================");
        System.out.println("|||||||||||||||||||||||||||||||   Mindmaps SQL Migration Example   ||||||||||||||||||||||||||||||");
        System.out.println("=================================================================================================");

        // create connection to the in-memory DB
        SQLConnection connection = new SQLConnection(user, pass, url, driver);
        System.out.println("Loading SQL database...");
        connection.execute(clear());
        connection.execute(readWorld());
        System.out.println("Finished loading SQL database");

        MindmapsEngineServer.start();

        // get connection to the graph
        MindmapsGraph graph = GraphFactory.getInstance().getGraphBatchLoading(keyspace);

        SQLWorldMigrator.migrateWorld(connection.getConnection(), graph);
        SQLWorldMigrator.printInformationAboutWorld(graph);

        MindmapsEngineServer.stop();
    }

    /**
     * Read the world.sql resource into a string.
     */
    public static String readWorld() {
        try {
            URL dataUrl = SQLWorldMigrator.class.getClassLoader().getResource(worldFile);
            assert dataUrl != null;
            return Files.readAllLines(Paths.get(dataUrl.toURI())).stream()
                    .filter(line -> !line.startsWith("--") && !line.startsWith("/*!"))
                    .collect(Collectors.joining("\n"));
        }
        catch (URISyntaxException |IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Statement to clear the graph
     */
    public static String clear(){
        return "DROP ALL OBJECTS";
    }

    /**
     * Disables extra logs which come from cassandra when using the TitanFactory
     */
    private static void disableInternalLogs(){
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.OFF);
    }
}
