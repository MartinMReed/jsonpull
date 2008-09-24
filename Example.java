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

import org.jsonpull.Json;
import org.jsonpull.FormatException;
import org.jsonpull.SyntaxException;

import java.util.Enumeration;

/** 
 * Json-pull usage example.
 */
public class Example {
  public static void main(String[] args) {
    String sampleNoQuotes = "{'version':'1.0','people':[{'name':'john','contact':'+123456','detail':'ignore'},{'name':'sam','contact':'+456789'}]}";
    String sample = sampleNoQuotes.replace('\'', '"');
    Json parser = new Json(sample);
    try {
      //expects an Object
      parser.eat('{');
      if (parser.seekInObject("people")) {
        parser.eat('['); //people is an array
        for (Enumeration personEnum = parser.arrayElements(); personEnum.hasMoreElements(); ) {
          Person person = new Person(parser);
          System.out.println("" + person);
        }
      }
    } catch (FormatException e) {
      System.out.println("" + e);
    } catch (SyntaxException e) {
      System.out.println("syntax " + e);
    }
  }
}

class Person {
  String name;
  String contact;

  Person(Json parser) throws FormatException {
    parser.eat('{'); //person is an object
    for (Enumeration field = parser.objectElements(); field.hasMoreElements(); ) {
      parser.eat(Json.KEY);
      String key = parser.getString();
      if (key.equals("name")) {
        parser.eat(Json.STRING);
        name = parser.getString();
      } else if (key.equals("contact")) {
        contact = parser.getStringValue();
      }
    }
  }
  
  public String toString() {
    return "name: " + name + "; contact: " + contact;
  }
}
