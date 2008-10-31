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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public abstract class JsonBase {

    private static final String PARSE_STRING_OPTIONS = "\"";
    private static final String PARSE_VARIABLE_OPTIONS = ",]}";

    public static final int START_OBJECT = '{';
    public static final int END_OBJECT = '}';
    public static final int START_ARRAY = '[';
    public static final int END_ARRAY = ']';
    public static final int KEY = 'k';
    public static final int STRING = 'a';
    public static final int VARIABLE = 'v';
    public static final int SEPERATOR = ',';
    public static final int NULL = 'n';

    private static final int STATE_END = 1;
    private static final int STATE_VALUE = 2;
    private static final int STATE_ARRAY = 3;
    private static final int STATE_OBJECT = 4;
    private static final int STATE_AFTER_END = 6;

    private int state = STATE_END;

    private int pos, end;
    private final Reader reader;
    private final StateStack stateStack;
    private final FifoStringBuffer buffer;

    private String stringValue;

    protected JsonBase(InputStream inputStream, String encoding) {

        Reader reader = null;

        if ( encoding != null ) {

            try {

                reader = new InputStreamReader( inputStream, encoding );
            }
            catch (UnsupportedEncodingException e) {

                System.out.println( "Encoding[" + encoding + "] not supported" );
            }
        }

        if ( reader == null ) {

            reader = new InputStreamReader( inputStream );
        }

        this.reader = reader;
        buffer = new FifoStringBuffer();
        stateStack = new StateStack();
        pushAndSetState( STATE_VALUE );
    }

    private int read() {

        try {

            int read = reader.read();
            buffer.push( (char) read );
            pos++;
            return read;
        }
        catch (IOException e) {

            throw new RuntimeException( e.getMessage() );
        }
    }

    public char nextValue() {

        char read = nextBuffervalue();

        // remove all whitespace
        while (FifoStringBuffer.isWhitespace( read )) {

            buffer.pop();
            read = nextBuffervalue();
        }

        return read;
    }

    private char nextBuffervalue() {

        if ( buffer.isEmpty() ) {

            read();
        }

        return buffer.first();
    }

    private int returnValue( int value, int state ) {

        buffer.pop();
        this.state = state;
        return value;
    }

    /** 
     * Advances to the next token, and returns its type.
     * @returns the token type, such as START_OBJECT, STRING, END_DOCUMENT.
     */
    public int next() {

        char next = nextValue();

        //        System.out.println( "state[" + state + "], next[" + next + "], stringValue[" + stringValue + "]" );

        switch (state) {

            case STATE_END: {

                state = STATE_AFTER_END;
            }

            case STATE_AFTER_END: {

                throw new IllegalStateException( "Called next() after the end" );
            }

            case STATE_VALUE: {

                return valueState( next );
            }

            case STATE_ARRAY: {

                return arrayState( next );
            }

            case STATE_OBJECT: {

                return objectState( next );
            }
        }

        throw new IllegalStateException( "lexical error: " + next );
    }

    private int valueState( char next ) {

        switch (next) {

            case '"': {

                parseAndSetString();
                popState();

                return returnValue( STRING, state );
            }

            case START_OBJECT: {

                return returnValue( START_OBJECT, STATE_OBJECT );
            }

            case START_ARRAY: {

                return returnValue( START_ARRAY, STATE_ARRAY );
            }

            default: {

                return defaultValueState( next );
            }
        }
    }

    private int defaultValueState( char next ) {

        parseAndSetVariable();
        popState();

        if ( isNull( getString() ) ) {

            stringValue = null;
            return returnValue( NULL, state );
        }

        return returnValue( VARIABLE, state );
    }

    private int arrayState( char next ) {

        if ( next == ',' ) {

            buffer.pop();
            next = nextValue();
        }

        switch (next) {

            case END_ARRAY: {

                popState();
                return returnValue( END_ARRAY, state );
            }

            case '"': {

                parseAndSetString();
                return returnValue( STRING, state );
            }

            case START_OBJECT: {

                pushAndSetState( STATE_OBJECT );
                return returnValue( START_OBJECT, state );
            }

            case START_ARRAY: {

                pushAndSetState( STATE_ARRAY );
                return returnValue( START_ARRAY, state );
            }

            default: {

                return defaultArrayState( next );
            }
        }
    }

    private int defaultArrayState( char next ) {

        parseAndSetVariable();

        if ( isNull( getString() ) ) {

            stringValue = null;
            return returnValue( NULL, state );
        }

        return returnValue( VARIABLE, state );
    }

    private int objectState( char next ) {

        if ( next == ',' ) {

            buffer.pop();
            next = nextValue();
        }

        switch (next) {

            case END_OBJECT: {

                popState();
                return returnValue( END_OBJECT, state );
            }

            case '"': {

                parseAndSetString();

                buffer.pop();
                next = nextValue();

                if ( next != ':' ) {

                    throw new IllegalStateException( "Expected ':', found '" + next + "' at pos[" + pos + "]" );
                }

                pushAndSetState( STATE_VALUE );
                return returnValue( KEY, state );
            }

            default: {

                throw new IllegalStateException( "Unexpected '" + next + "' in object at pos[" + pos + "]" );
            }
        }
    }

    private boolean isNull( String text ) {

        return text == null || text.length() == 0 || "null".equals( text );
    }

    protected String getString() {

        return stringValue;
    }

    protected void setString( boolean trimWhitespace ) {

        stringValue = buffer.toString( end );

        if ( trimWhitespace ) {

            stringValue = stringValue.trim();
        }

        buffer.pop( end );
    }

    /**
     * Advances to the next token on the given level.
     * Thus may advance through multiple tokens, until reaches the level.
     */
    public void seekLevel( int level ) {

        while (getLevel() > level) {

            next();
        }
    }

    public int getLevel() {

        return stateStack.size();
    }

    private void parseAndSetString() {

        // remove the starting " character
        buffer.pop();

        end = indexOf( PARSE_STRING_OPTIONS ) - 1;

        // remove the ending " character
        buffer.delete();

        setString( false );
    }

    private void parseAndSetVariable() {

        end = indexOf( PARSE_VARIABLE_OPTIONS );

        setString( true );

        // push a dummy character
        buffer.insert( (char) -1 );
    }

    private int indexOf( String find ) {

        for (int read = read(), i = 1; read > -1; read = read(), i++) {

            if ( read == '\\' ) {

                read();
                i++;
            }
            else if ( find.indexOf( read ) != -1 ) {

                return i;
            }
        }

        throw new IllegalStateException( "missing closing characters" );
    }

    private void popState() {

        state = stateStack.pop();
    }

    private void pushAndSetState( int newState ) {

        stateStack.push( state );
        state = newState;
    }
}
