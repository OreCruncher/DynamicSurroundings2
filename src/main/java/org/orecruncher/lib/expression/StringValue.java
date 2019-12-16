/*
 * Licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.lib.expression;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

public class StringValue extends Variant {

	protected String value;

	public StringValue() {
		this.value = StringUtils.EMPTY;
	}

	public StringValue(@Nonnull final String v) {
		this.value = v;
	}

	public StringValue(@Nonnull final String name, @Nonnull final String value) {
		super(name);
		this.value = value;
	}

	@Override
	public float asNumber() {
		return Float.parseFloat(this.value);
	}

	@Override
	@Nonnull
	public String asString() {
		return this.value;
	}

	@Override
	public boolean asBoolean() {
		// Eliminates a lot of false positive situations.  The expression system provides a string
		// on error so we have to make sure it does not match otherwise bad things will happen.
		return "TRUE".equalsIgnoreCase(this.value);
	}

	@Override
	public int compareTo(@Nonnull final IVariant variant) {
		return this.value.compareTo(variant.asString());
	}

	@Override
	@Nonnull
	public IVariant add(@Nonnull final IVariant term) {
		return new StringValue(this.value.concat(term.asString()));
	}

}
