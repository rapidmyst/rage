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

import io.mindmaps.MindmapsGraph;
import io.mindmaps.graql.Graql;
import io.mindmaps.graql.MatchQuery;
import io.mindmaps.graql.QueryBuilder;
import io.mindmaps.graql.Reasoner;
import io.mindmaps.migration.owl.OWLMigrator;
import java.io.InputStream;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import static io.mindmaps.graql.Graql.count;
import static io.mindmaps.graql.Graql.var;


public class OWLResourceMigrator {

    public static void migrate(String resource, MindmapsGraph graph){
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLMigrator migrator = new io.mindmaps.migration.owl.OWLMigrator();
        OWLOntology ontology;

        try (InputStream in = OWLResourceMigrator.class.getResourceAsStream(resource)) {
            if (in == null)
                throw new NullPointerException("Resource : " + resource + " not found.");
            ontology = manager.loadOntologyFromOntologyDocument(in);
            migrator.ontology(ontology).graph(graph).migrate();
            migrator.graph().commit();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Prints information about the migrated database
     */
    public static void printInformationAboutWorld(MindmapsGraph graph){
        graph.rollback();

        // What are the types that were migrated?
        System.out.println("Migrated Types:");
        graph.getMetaEntityType().instances().forEach(System.out::println);

        // How many people are in the world?
        Long numberCountries = graph.graql().match(var("x").isa("tPerson")).distinct().aggregate(count()).execute();
        System.out.println("\n" + numberCountries + " people in the family tree.");

        Reasoner reasoner = new Reasoner(graph);
        QueryBuilder qb = graph.graql();

        // How many descendants does Eleanor Pringle (1741) have?
        MatchQuery descendantQuery = qb.match(
                var("x").isa("tPerson"),
                var("y").id("eeleanor_pringle_1741"),
                var().isa("op-hasAncestor")
                        .rel("owl-subject-op-hasAncestor", "x")
                        .rel("owl-object-op-hasAncestor", "y"))
                .select("x");
        final int descendants = reasoner.resolve(descendantQuery).size();
        System.out.println("Eleanor Pringle has " + descendants + " descdendants.");

        // Who are the great uncles of Ethel Archer?
        System.out.println("Great uncles of Ethel Archer:");
        MatchQuery greatUncleQuery = qb.match(
                var("x").isa("tPerson").id("eethel_archer_1912"),
                var().isa("op-hasGreatUncle")
                        .rel("owl-subject-op-hasGreatUncle", "x")
                        .rel("owl-object-op-hasGreatUncle", "y"))
                .select("y");
        reasoner.resolve(greatUncleQuery)
                .stream().map(i -> i.get("y").asInstance().getId()).forEach(System.out::println);
    }
}
