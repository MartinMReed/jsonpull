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

/**
 * Json-pull is an easy to use and efficient (memory, CPU) way of parsing JSON.
 * Json-pull works on both JavaME and JavaSE, so source must be Java 1.4.
 *
 */
public class JsonBase {
  public static final int START_OBJECT = '{';
  public static final int END_OBJECT   = '}';
  public static final int START_ARRAY  = '['; 
  public static final int END_ARRAY    = ']';
  public static final int END_DOCUMENT =   0;
  public static final int KEY          = 'k'; 
  public static final int STRING       = 'a';
  
  private static final int STATE_END    = 1; 
  private static final int STATE_VALUE  = 2; 
  private static final int STATE_ARRAY  = 3; 
  private static final int STATE_OBJECT = 4;
  private static final int STATE_AFTER_END = 5;
  
  private int pos;
  private String textString;
  private char[] textChars;
  private int state;
  private int stringBegin, stringEnd;
  private IntStack stateStack = new IntStack();

  /**
   * Initializes parsing of a JSON string.
   */
  public JsonBase(String str) {
    textString = str;
    textChars  = str.toCharArray();
    pos   = 0;
    state = STATE_END;
    pushAndSetState(STATE_VALUE);
  }

  /** 
   * Advances to the next token, and returns its type.
   * @returns the token type, such as START_OBJECT, STRING, END_DOCUMENT.
   */
  public int next() {
    //System.out.println("" + pos + ' ' + state + ' ' + getString());
    char c = textChars[pos++];
    switch (state) {
      case STATE_END:
        state = STATE_AFTER_END;
        return END_DOCUMENT;
        
      case STATE_AFTER_END:
        throw new ArrayIndexOutOfBoundsException("json next() after end");

      case STATE_VALUE:
        switch (c) {
          case '"': {
            parseString();
            popState();
            return STRING;
          }

          case '{': {
            state = STATE_OBJECT;
            return '{';          
          }

          case '[': {
            state = STATE_ARRAY;
            return '[';
          }

          default:
            throw new Error("" + c);
        }
        
      case STATE_ARRAY: {
        if (c == ',') {
          c = textChars[pos++];
        }
        switch (c) {
          case ']': {
            popState();
            return ']';            
          }

          case '"': {
            parseString();
            return STRING;
          }

          case '{': {
            pushAndSetState(STATE_OBJECT);
            return '{';
          }

          case '[': {
            pushAndSetState(STATE_ARRAY);
            return '[';
          }
          default: {
            throw new Error("unexpected in array " + c);
          }
        }
      }

      case STATE_OBJECT: {
        if (c == ',') {
          c = textChars[pos++];
        }
        if (c == '}') {
          popState();
          return '}';
        }
        if (c == '"') {
          parseString();
          c = textChars[pos++];
          if (c != ':') {
            throw new Error("expected ':', got '" + c + "'"); 
          }
          pushAndSetState(STATE_VALUE);
          return KEY;
        }
        throw new Error("not expected: " + c);
      }
    }
    throw new Error("lexical error: " + c);
  }

  /**
   * The last event must be STRING.
   * @returns the String value.
   */
  public String getString() {
    return new String(textChars, stringBegin, stringEnd-stringBegin);
  }

  //---- Non-public below ----

  /**
   * Advances to the next token on the given level.
   * Thus may advance through multiple tokens, until reaches the level.
   * Package-visibility, used by JsonEnumerator.
   */
  void seekLevel(int level) {
    //int l;
    while (getLevel() > level) {
      next();
    }
    //return next();
  }

  /**
   * Used by JsonEnumeration
   */
  int peekNext() {
    return textChars[pos];
  }

  /**
   * Used in Json.
   */
  int getLevel() {
    return stateStack.size();
  }

  private void parseString() {
    stringEnd   = findEndQuote(pos);
    stringBegin = pos;
    pos = stringEnd+1;
  }

  private int findEndQuote(int start) {
    int end = textString.indexOf('"', start);
    while (end > start && textString.charAt(end-1) == '\\') {
      start = end + 1;
      end = textString.indexOf('"', start);
    }
    if (end == -1) {
      throw new Error("missing closing quote");
    }
    return end;
  }

  private void popState() {
    state = stateStack.pop();
  }

  private void pushAndSetState(int newState) {
    stateStack.push(state);
    state = newState;
  }
}
