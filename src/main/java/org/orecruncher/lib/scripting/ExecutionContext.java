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

package org.orecruncher.lib.scripting;

import org.orecruncher.lib.Lib;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.logging.IModLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ExecutionContext {

    private static final IModLog LOGGER = Lib.LOGGER;

    private static final String FUNCTION_SHELL = "%s;";

    private final String contextName;
    private final ScriptEngine engine;
    private final ObjectArray<VariableSet<?>> variables = new ObjectArray<>(8);
    private final Map<String, CompiledScript> compiled = new HashMap<>();
    private final CompiledScript error;

    public ExecutionContext(@Nonnull final String contextName) {
        this.contextName = contextName;
        this.engine = new ScriptEngineManager().getEngineByExtension("js");
        this.error = makeFunction("'<ERROR>'");
        this.engine.put("lib", new LibraryFunctions());

        Lib.LOGGER.info("JavaScript engine provided: %s", this.engine.getFactory().getEngineName());
    }

    public void put(@Nonnull final String name, @Nullable final Object obj) {
        this.engine.put(name, obj);
    }

    public void add(@Nonnull final VariableSet<?> varSet) {
        if (this.engine.get(varSet.getSetName()) != null)
            throw new IllegalStateException(String.format("Variable set '%s' already defined!", varSet.getSetName()));

        this.variables.add(varSet);
        this.engine.put(varSet.getSetName(), varSet.getInterface());
    }

    public String getName() {
        return this.contextName;
    }

    public void update() {
        this.variables.forEach(VariableSet::update);
    }

    public boolean check(@Nonnull final String script) {
        final Optional<Object> result = eval(script);
        if (result.isPresent())
            return "true".equalsIgnoreCase(result.toString());
        return false;
    }

    @Nonnull
    public Optional<Object> eval(@Nonnull final String script) {
        CompiledScript func = compiled.get(script);
        if (func == null) {
            func = makeFunction(script);
            compiled.put(script, func);
        }

        try {
            final Object result = func.eval();
            return Optional.ofNullable(result);
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Error execution script: %s", script);
            compiled.put(script, this.error);
        }

        return Optional.of("ERROR?");
    }

    @Nonnull
    private CompiledScript makeFunction(@Nonnull final String script) {
        final String source = String.format(FUNCTION_SHELL, script);
        try {
            return ((Compilable) this.engine).compile(source);
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Error compiling script: %s", source);
        }
        return this.error;
    }

}
