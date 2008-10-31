/*
 * Copyright (c) 2008-2010 Metova, Inc.
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
 * First-in-first-out String buffer.
 */
final class FifoStringBuffer {

    private final StringBuffer buffer;
    char[] toStringHelperBuffer;

    public FifoStringBuffer() {

        buffer = new StringBuffer();
        toStringHelperBuffer = new char[1024];
    }

    public String toString() {

        return toString( buffer.length() );
    }

    /**
     * Returns string from beginning to end
     * @param end as an integer marking last char in string
     * @return String
     */
    public String toString( int end ) {

        return toString( 0, end );
    }

    public String toString( int start, int end ) {

        int length = end - start;

        ensureToStringHelperBufferCapacity( length );

        buffer.getChars( start, end, toStringHelperBuffer, start );
        return new String( toStringHelperBuffer, start, length );
    }

    private void ensureToStringHelperBufferCapacity( int capacity ) {

        int oldCapacity = toStringHelperBuffer.length;

        if ( capacity > oldCapacity ) {

            char[] newToStringHelperBuffer = new char[capacity];
            for (int i = 0; i < oldCapacity; i++) {

                newToStringHelperBuffer[i] = toStringHelperBuffer[i];
            }

            toStringHelperBuffer = newToStringHelperBuffer;
        }
    }

    public void trim() {

        trimHead();
        trimTail();
    }

    public static final boolean isWhitespace( char c ) {

        return c <= ' ';
    }

    public void trimHead() {

        int count = 0;

        for (int i = 0; i < size(); i++) {

            if ( isWhitespace( get( i ) ) ) {

                count++;
            }
            else {

                break;
            }
        }

        if ( count > 0 ) {

            pop( count );
        }
    }

    public void trimTail() {

        int count = 0;

        for (int i = size() - 1; i >= 0; i--) {

            if ( isWhitespace( get( i ) ) ) {

                count++;
            }
            else {

                break;
            }
        }

        if ( count > 0 ) {

            delete( count );
        }
    }

    public void push( char character ) {

        buffer.append( character );
    }

    public boolean isEmpty() {

        return size() < 1;
    }

    public char first() {

        return get( 0 );
    }

    public char last() {

        return get( size() - 1 );
    }

    public char get( int index ) {

        return ( size() > 0 ) ? buffer.charAt( index ) : (char) -1;
    }

    public void delete() {

        delete( 1 );
    }

    public void delete( int count ) {

        int size = size();
        buffer.delete( size - count, size );
    }

    public final void insert( char character ) {

        buffer.insert( 0, character );
    }

    public final void pop() {

        pop( 1 );
    }

    public void pop( int count ) {

        buffer.delete( 0, count );
    }

    public int size() {

        return buffer.length();
    }
}
