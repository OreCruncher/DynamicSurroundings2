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

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

public final class Dynamic {

	private Dynamic() {

	}

	public final static class DynamicNumber extends NumberValue implements IDynamicVariant {
		
		private boolean needsSet = true;
		private final Supplier<Float> supplier;

		public DynamicNumber(@Nonnull final String name, @Nonnull final Supplier<Float> func) {
			super(name);
			
			this.supplier = func;
		}

		@Override
		public void reset() {
			this.needsSet = true;
		}
		
		protected void update() {
			if (this.needsSet) {
				this.value = this.supplier.get();
				this.needsSet = false;
			}
		}

		@Override
		public float asNumber() {
			update();
			return super.asNumber();
		}

		@Override
		@Nonnull
		public String asString() {
			update();
			return super.asString();
		}

		@Override
		public boolean asBoolean() {
			update();
			return super.asBoolean();
		}

		// Operator support in case of strings
		@Override
		@Nonnull
		public IVariant add(@Nonnull final IVariant term) {
			update();
			return super.add(term);
		}
	}

	public final static class DynamicString extends StringValue implements IDynamicVariant {
		
		private boolean needsSet = true;
		private final Supplier<String> supplier;

		public DynamicString(@Nonnull final String name, @Nonnull final Supplier<String> func) {
			super(name, StringUtils.EMPTY);

			this.supplier = func;
		}

		@Override
		public void reset() {
			this.needsSet = true;
		}

		protected void update() {
			if (this.needsSet) {
				this.value = this.supplier.get();
				this.needsSet = false;
			}
		}

		@Override
		public float asNumber() {
			update();
			return super.asNumber();
		}

		@Override
		@Nonnull
		public String asString() {
			update();
			return super.asString();
		}

		@Override
		public boolean asBoolean() {
			update();
			return super.asBoolean();
		}

		// Operator support in case of strings
		@Override
		@Nonnull
		public IVariant add(@Nonnull final IVariant term) {
			update();
			return super.add(term);
		}
	}

	public final static class DynamicBoolean extends BooleanValue implements IDynamicVariant {
		
		private boolean needsSet = true;
		private final Supplier<Boolean> supplier;

		public DynamicBoolean(@Nonnull final String name, @Nonnull final Supplier<Boolean> func) {
			super(name);
			
			this.supplier = func;
		}

		@Override
		public void reset() {
			this.needsSet = true;
		}

		protected void update() {
			if (this.needsSet) {
				this.value = this.supplier.get();
				this.needsSet = false;
			}
		}

		@Override
		public float asNumber() {
			update();
			return super.asNumber();
		}

		@Override
		@Nonnull
		public String asString() {
			update();
			return super.asString();
		}

		@Override
		public boolean asBoolean() {
			update();
			return super.asBoolean();
		}

		// Operator support in case of strings
		@Override
		@Nonnull
		public IVariant add(@Nonnull final IVariant term) {
			update();
			return super.add(term);
		}
	}
}
