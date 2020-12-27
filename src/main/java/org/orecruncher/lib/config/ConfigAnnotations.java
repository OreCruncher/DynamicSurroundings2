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

package org.orecruncher.lib.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ConfigAnnotations {

    /**
     * Comment that will show up in the configuration file.  It will also be supplied as a tool tip if a translation
     * is not available.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.FIELD })
    public @interface Comment {
        /**
         * Comment that shows in the config file
         */
        String value();
    }

    /**
     * Indicates that the class defines a category and can contain individual settings as well as other
     * categories.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    public @interface Category {
        /**
         * The name that shows in the config file
         */
        String value();

        /**
         * Tooltip to displays in the GUI
         */
        String translationKey() default "";
    }

    /**
     * Indicates that the field will map to an option in a config file
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public @interface OptionString {
        /**
         * The name that shows in the config file
         */
        String value();

        /**
         * Tooltip that displays in the GUI
         */
        String translationKey() default "";
    }

    /**
     * The option is an integer type with a defined range.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public @interface OptionBoolean {
        String value();
        String translationKey() default "";
    }

    /**
     * The option is an integer type with a defined range.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public @interface OptionInteger {
        /**
         * The name that shows in the config file
         */
        String value();

        /**
         * Tooltip that displays in the GUI
         */
        String translationKey() default "";

        /**
         * Minimum possible value for the setting
         */
        int min() default Integer.MIN_VALUE;

        /**
         * Maximum possible value for the setting
         */
        int max() default Integer.MAX_VALUE;
    }

    /**
     * The option is a float type with a defined range
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public @interface OptionDouble {
        /**
         * The name that shows in the config file
         */
        String value();

        /**
         * Tooltip that displays in the GUI
         */
        String translationKey() default "";

        /**
         * Minimum possible value for the setting
         */
        double min() default Double.MIN_VALUE;

        /**
         * Maximum possible value for the setting
         */
        double max() default Double.MAX_VALUE;
    }

    /**
     * The option is an enum type
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public @interface OptionEnum {
        /**
         * The name that shows in the config file
         */
        String value();

        /**
         * Tooltip that displays in the GUI
         */
        String translationKey() default "";

        /**
         * The enumeration type
         */
        Class<? extends Enum> enumeration();
    }

    /**
     * A restart is required if the value of the setting changed
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public @interface RestartRequired {

    }

    /**
     * The setting will be suppressed from GUI displays.  Manipulation is only possible if the user goes directly
     * to the underlying config file.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public @interface Hidden {

    }
}
