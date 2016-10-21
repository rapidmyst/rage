package io.mindmaps.example;/*
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    private Connection connection;

    public SQLConnection(String user, String pass, String url, String driver){
        try {
            Class.forName(driver).newInstance();
        } catch (ClassNotFoundException|InstantiationException|IllegalAccessException  e){
            throw new RuntimeException("Missing JDBC driver: " + driver);
        }

        // create a connection to the MySQL database
        try {
            connection = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public Connection getConnection() {
        return connection;
    }

    public void execute(String statement){
        try {
            connection.prepareStatement(statement).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
