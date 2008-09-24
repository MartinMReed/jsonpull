/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonpull;

/**
 * A simple stack of integers.
 * Avoids the object creation Integer(value) that would happen
 * if using java.util.Stack or java.util.Vector.
 * Only used internally by Json.
 */
class IntStack {
    int[] values = null;
    int allocSize;
    int mSize;

    IntStack() {
        allocSize = 32;
        values = new int[allocSize];
        mSize = 0;
    }

    int pop() {
        return values[--mSize];
    }

    void push(int value) {
        if (mSize == allocSize) {
            int newSize = allocSize << 1;
            int[] newValues = new int[newSize];
            System.arraycopy(values, 0, newValues, 0, mSize);
            values    = newValues;
            allocSize = newSize;
        }
        values[mSize++] = value;
    }

    int size() {
        return mSize;
    }
}
