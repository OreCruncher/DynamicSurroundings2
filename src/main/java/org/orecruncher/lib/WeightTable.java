/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

package org.orecruncher.lib;

import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Random;

/**
 * Classic WeightTable for random weighted selection.
 *
 * @param <T>
 */
public class WeightTable<T> {

    protected static final Random RANDOM = XorShiftRandom.current();

    protected final ObjectArray<IItem<T>> entries =  new ObjectArray<>();
    protected int totalWeight = 0;

    public WeightTable() {
    }

    public WeightTable(@Nonnull final IItem<T>[] items) {
        for (final IItem<T> i : items)
            add(i);
    }

    public WeightTable(@Nonnull final Collection<? extends IItem<T>> input) {
        for (final IItem<T> i : input)
            add(i);
    }

    public void add(@Nonnull final T e, final int weight) {
        add(new IItem<T>() {
            @Override
            public int getWeight() {
                return weight;
            }

            @Override
            public T getItem() {
                return e;
            }
        });
    }

    public void add(@Nonnull final IItem<T> entry) {
        entries.add(entry);
        totalWeight += entry.getWeight();
    }

    @Nullable
    public T next() {
        if (this.totalWeight <= 0)
            return null;

        int targetWeight = RANDOM.nextInt(this.totalWeight);

        IItem<T> selected = null;
        int i = -1;
        do {
            selected = entries.get(++i);
            targetWeight -= selected.getWeight();
        } while (targetWeight >= 0);

        return selected.getItem();
    }

    public interface IItem<T> {

        int getWeight();

        T getItem();
    }

}
