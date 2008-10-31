/*
 * Copyright (c) 2008-2010 Metova, Inc.
 * Copyright (c) 2007 Google Inc.
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

import java.io.InputStream;
import java.util.Enumeration;

import m.org.apache.commons.io.StringInputStream;
import m.org.apache.commons.lang.StringEscapeUtils;

public final class Json extends JsonBase {

    /**
     * Creates a json object from a String.
     * @param input - a String used to create the json object
     */
    public Json(String input) {

        this( new StringInputStream( input ) );
    }

    /**
     * Creates a json object from an input stream.
     * @param inputStream - an InputStream used to create the json object
     */
    public Json(InputStream inputStream) {

        this( inputStream, null );
    }

    /**
     * Creates a json object from an input stream with a specific encoding type.
     * @param inputStream - an InputStream used to create the json object
     * @param encoding = a String representing the encoding type to create the json object
     */
    public Json(InputStream inputStream, String encoding) {

        super( inputStream, encoding );
    }

    /**
     * Retrieves all of the object elements that are contained within json
     * Must be called from inside an Object (e.g. right after reading START_OBJECT).
     * @return an Enumeration object containing each object element in the json
     */
    public Enumeration objectElements() {

        if ( next( START_OBJECT ) ) {

            return new JsonEnumeration( this, getLevel(), END_OBJECT );
        }
        else {

            return new JsonEnumeration( this, getLevel() );
        }
    }

    /**
     * Retrieves all of the array elements that are contained within json
     * Must be called from inside an Array (e.g. right after reading START_ARRAY).
      * @return an Enumeration object containing each array element in the json
     */
    public Enumeration arrayElements() {

        if ( next( START_ARRAY ) ) {

            return new JsonEnumeration( this, getLevel(), END_ARRAY );
        }
        else {

            return new JsonEnumeration( this, getLevel() );
        }
    }

    /**
     * Consumes a single event, which must be of the specified type.
     * @return boolean - returns whether or not the next element is of the expected type
     */
    private boolean next( int assertEvent ) {

        int event = next();
        assertEventType( event, assertEvent );
        return !( event == NULL && ( assertEvent == START_ARRAY || assertEvent == START_OBJECT ) );
    }

    /**
     * An assertion check that throws an exception if the event type is not of the expected type.
     */
    private void assertEventType( int actual, int expected ) {

        if ( actual != NULL && actual != expected ) {

            throw new IllegalArgumentException( "expected " + (char) expected + ", found " + (char) actual + ' ' + getString() );
        }
    }

    /**
     * Skips forward inside an object until finds the given key.
     * Must be called from inside an object (e.g. right after reading START_OBJECT).
     * 
     * @param key - the string the user is looking for
     * @returns true if the key was found, false otherwise.
     */
    public boolean seekInObject( String key ) {

        Enumeration enumerator = objectElements();
        while (enumerator.hasMoreElements()) {

            next( KEY );

            if ( getString().equals( key ) ) {

                return true;
            }
        }

        return false;
    }

    /**
     * Calls seekInObject(key). If the call is successful,
     * it returns getStringValue().
     * @param key - the string the user is looking for
     * @return String value for given key if it exists, else null
     */
    public String tryGetStringValue( String key ) {

        return seekInObject( key ) ? getStringValue() : null;
    }

    /**
     * Returns the next key as a String.
     * 
     * @return String that is the next key
     */
    public String getKey() {

        next( KEY );
        return getString();
    }

    /**
     * Return the String value and unescapes any Java
     * @return String the next escaped string is unescaped and then returned
     */
    public String getStringValue() {

        next( STRING );
        return StringEscapeUtils.unescapeJava( getString() );
    }

    public String getUnknownValue() {

        next();
        return getString();
    }

    /**
     * Return the original String value.
     * @return String the original value of the next escaped string
     */
    public String getEscapedStringValue() {

        next( STRING );
        return getString();
    }

    /**
     * Return a variable String.
     * @return String - the next variable in the json
     */
    public String getVariableValue() {

        next( VARIABLE );
        return getString();
    }

    /**
     * Return a boolean is true is the next String.
     * @return boolean - returns true if the next string equals "true", false otherwise.
     */
    public boolean getBooleanValue() {

        next();
        return "true".equals( getString() );
    }

    /**
     * Return an int value.
     * Warning: If your Json response has any white space after the integer value you
     * are trying to obtain and the element is the last element of a block, this will fail.
     * 
     * @return int - the next value as an int. Throws an exception if the value cannot be parsed to an int
     */
    public int getIntegerValue() {

        next();

        try {

            return Integer.parseInt( getString() );
        }
        catch (Exception e) {

            return 0;
        }
    }

    /**
     * Return a double value.
     * Warning: If your Json response has any white space after the double value you
     * are trying to obtain and the element is the last element of a block, this will fail.
     * @return double - the next value as an double. Throws an exception if the value cannot be parsed to an double
     */
    public double getDoubleValue() {

        next();

        try {

            return Double.parseDouble( getString() );
        }
        catch (Exception e) {

            return 0;
        }
    }

    /**
     * Return a long value.
     * Warning: If your Json response has any white space after the long value you
     * are trying to obtain and the element is the last element of a block, this will fail.
     * @return long - the next value as an long. Throws an exception if the value cannot be parsed to an long
     */
    public long getLongValue() {

        next();

        try {

            return Long.parseLong( getString() );
        }
        catch (Exception e) {

            return 0;
        }
    }

    /**
     * Converts to a long date value from a Date string.
     * @return long - date value in milliseconds
     */
    public long getDateValue() {

        try {

            String text = getStringValue();

            text = text.substring( "\\/Date(".length() - 1, text.length() - ")\\/".length() + 1 );

            return Long.parseLong( text );
        }
        catch (Exception e) {

            return 0;
        }
    }
}
