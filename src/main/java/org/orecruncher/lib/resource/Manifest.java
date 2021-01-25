/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package org.orecruncher.lib.resource;

import com.google.gson.annotations.SerializedName;

class Manifest {

    @SerializedName("version")
    protected int version = 0;
    @SerializedName("name")
    protected String name = "(unspecified)";
    @SerializedName("author")
    protected String author = "(unspecified)";
    @SerializedName("website")
    protected String website = "(unspecified)";

    public int getVersion() {
        return this.version;
    }

    public String getName() {
        return this.name != null ? this.name : "(unspecified)";
    }

    public String getAuthor() {
        return this.author != null ? this.author : "(unspecified)";
    }

    public String getWebsite() {
        return this.website != null ? this.website : "(unspecified)";
    }
}
