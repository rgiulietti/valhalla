/*
 * Copyright (c) 2001, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.internal.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import jdk.internal.value.PrimitiveClass;
import jdk.internal.misc.Unsafe;

/** Base class for jdk.internal.misc.Unsafe-based FieldAccessors. The
    observation is that there are only nine types of fields from the
    standpoint of reflection code: the eight primitive types and
    Object. Using class Unsafe instead of generated bytecodes saves
    memory and loading time for the dynamically-generated
    FieldAccessors. */

abstract class UnsafeFieldAccessorImpl extends FieldAccessorImpl {
    static final Unsafe unsafe = Unsafe.getUnsafe();

    protected final long    fieldOffset;
    protected final boolean isFinal;

    UnsafeFieldAccessorImpl(Field field) {
        super(field);
        int mods = field.getModifiers();
        this.isFinal = Modifier.isFinal(mods);
        if (Modifier.isStatic(mods))
            fieldOffset = unsafe.staticFieldOffset(field);
        else
            fieldOffset = unsafe.objectFieldOffset(field);
    }

    protected boolean isFlattened() {
        return unsafe.isFlattened(field);
    }

    protected boolean canBeNull() {
        return !PrimitiveClass.isPrimitiveClass(field.getType()) ||
                PrimitiveClass.isPrimaryType(field.getType());
    }

    protected Object checkValue(Object value) {
        if (!canBeNull() && value == null)
            throw new NullPointerException(field + " cannot be set to null");

        if (value != null) {
            Class<?> type = value.getClass();
            if (PrimitiveClass.isPrimitiveClass(type)) {
                type = PrimitiveClass.asValueType(type);
            }
            if (!field.getType().isInstance(value)) {
                throwSetIllegalArgumentException(value);
            }
        }
        return value;
    }

}