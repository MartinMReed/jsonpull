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

package com.google.jsonpull;

import java.util.Enumeration;

/**
 * Adds some utility methods above JsonBase.
 */
public class Json extends JsonBase {
  public Json(String text) {
    super(text);
  }

  /**
   * Must be called from inside an Object (e.g. right after reading START_OBJECT).
   */
  public Enumeration objectElements() {
    return new JsonEnumeration(this, getLevel(), END_OBJECT);
  }

  /**
   * Must be called from inside an Array (e.g. right after reading START_ARRAY).
   */
  public Enumeration arrayElements() {
    return new JsonEnumeration(this, getLevel(), END_ARRAY);
  }

  /**
   * Consumes a single event, which must be of the specified type.
   * @throws FormatException if the event is of another type.
   */
  public void eat(int targetEvent) throws FormatException {
    int event = next();
    if (event != targetEvent) {
      String mes = "expected " + (char)targetEvent + ", found " + (char)event + ' ' + getString();
      throw new FormatException(mes);
    }
  }

  /**
   * Skips forward inside an object until finds the given key.
   * Must be called from inside an object (e.g. right after reading START_OBJECT).
   * @returns true if the key was found, false otherwise.
   */
  public boolean seekInObject(String key) throws SyntaxException {
    for (Enumeration elems = objectElements(); elems.hasMoreElements(); ) {
      try {
        eat(Json.KEY);
      } catch (FormatException e) {
        throw new SyntaxException("" + e);
      }
      if (getString().equals(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Assumes the next token is a String, and returns its value.
   * @throws FormatException if the next token is not a String.
   */
  public String getStringValue() throws FormatException {
    eat(Json.STRING);
    return getString();
  }

  /**
   * The user assumes that an object is starting.
   * Inside that object, locate given key, and return its value,
   * which *must* be String.
   * @returns the String value, or null if key not found.
   * @throws FormatException if either assumption (Object starts, value of key is String) fails.
   */
  public String getStringValue(String key) throws FormatException, SyntaxException {
    eat('{');
    if (seekInObject(key)) {
      eat(Json.STRING);
      return getString();
    } else {
      return null;
    }
  }
}
