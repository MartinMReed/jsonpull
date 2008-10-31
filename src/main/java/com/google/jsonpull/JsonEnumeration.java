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

final class JsonEnumeration implements java.util.Enumeration {

    private Json parser;
    private int terminator;
    private int level;

    public JsonEnumeration(Json parser, int level) {

        this( parser, level, -1 );
    }

    public JsonEnumeration(Json parser, int level, int terminator) {

        this.parser = parser;
        this.level = level;
        this.terminator = terminator;
    }

    public boolean hasMoreElements() {

        if ( terminator != -1 ) {

            parser.seekLevel( level );

            if ( parser.nextValue() != terminator ) {

                return true;
            }

            parser.next();
        }

        return false;
    }

    /**
     * Only neeed to conform to Enumeration interface, otherwise not useful.
     */
    public Object nextElement() {

        return new Integer( parser.next() );
    }
}
