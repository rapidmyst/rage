package ai.grakn.example;/*
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
import io.mindmaps.Mindmaps;
import io.mindmaps.MindmapsGraph;
import io.mindmaps.util.REST;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;

public class Main {

    private static final String user = "mindmaps";
    private static final String pass = "mindmaps";
    private static final String driver = "com.mysql.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306/world";

    private static final String keyspace = "WORLD";

    public static void main(String[] args) {
        disableInternalLogs();

        if(!mindmapsEngineRunning()){
            System.out.println("Please start Mindmaps Engine");
            System.out.println("You can get more information on how to do so using our setup guide: https://mindmaps.io/pages/documentation/get-started/setup-guide.html");
            return;
        } else {
            System.out.println("=================================================================================================");
            System.out.println("|||||||||||||||||||||||||||||||   Mindmaps SQL Migration Example   ||||||||||||||||||||||||||||||");
            System.out.println("=================================================================================================");
        }

        Connection connection = new SQLConnection(user, pass, url, driver).getConnection();

        // get connection to the graph
        MindmapsGraph graph = Mindmaps.factory(Mindmaps.DEFAULT_URI, keyspace).getGraph();

        SQLWorldMigrator.migrateWorld(connection, graph);
        SQLWorldMigrator.printInformationAboutWorld(graph);
    }

    /**
     * Check if Mindmaps Engine is running
     * @return true if mindmaps engine is running
     */
    public static boolean mindmapsEngineRunning(){
        try {
            URL url = new URL("http://localhost:4567" + REST.WebPath.GRAPH_FACTORY_URI);
            URLConnection conn = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) conn;
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            http.getResponseMessage();
        }
        catch (IOException e){
            return false;
        }

        return true;
    }

    /**
     * Disables extra logs which come from cassandra when using the TitanFactory
     */
    private static void disableInternalLogs(){
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.OFF);
    }
}
