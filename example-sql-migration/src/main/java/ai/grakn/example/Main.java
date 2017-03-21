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

import ai.grakn.client.Client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private static final String SERVER_ADDRESS = "127.0.0.1:4567";
    private static final String keyspace = "world";
    private static final String user = "mindmaps";
    private static final String pass = "mindmaps";
    private static final String driver = "com.mysql.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306/world";

    public static void main(String[] args) {
        if(!Client.serverIsRunning(SERVER_ADDRESS)){
            System.out.println("Please start Mindmaps Engine");
            System.out.println("You can get more information on how to do so using our setup guide: https://mindmaps.io/pages/documentation/get-started/setup-guide.html");
            return;
        } else {
            System.out.println("=================================================================================================");
            System.out.println("|||||||||||||||||||||||||||||||   Mindmaps SQL Migration Example   ||||||||||||||||||||||||||||||");
            System.out.println("=================================================================================================");
        }

        Connection connection = getConnection(user, pass, url, driver);

        SQLWorldMigrator.migrateWorld(connection, keyspace);
        SQLWorldMigrator.printInformationAboutWorld(keyspace);

        try {
            connection.close();
        } catch (SQLException e) {
           throw new RuntimeException(e);
        }
    }

    public static Connection getConnection(String user, String pass, String url, String driver){
        try {
            Class.forName(driver).newInstance();
        } catch (ClassNotFoundException|InstantiationException|IllegalAccessException  e){
            throw new RuntimeException("Missing JDBC driver: " + driver);
        }

        // create a connection to the MySQL database
        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
