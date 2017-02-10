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

import ai.grakn.graql.macro.Macro;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Macro that will take the value from the "slug" field, split it by -
 * and return all but the last value of the split.
 */
public class GiphyMacro implements Macro<List<String>> {

    public List<String> apply(List<Object> values) {
        // get first value as string
        String slug = values.get(0).toString();

        // split
        String[] keywords = slug.split("-");

        // return all except last

        return keywords.length > 1 ? Arrays.asList(Arrays.copyOf(keywords, keywords.length-1)) : singletonList("gif");
    }

    public String name() {
        return "giphy";
    }
}
