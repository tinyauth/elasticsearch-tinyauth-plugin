/*
 * Copyright 2017 tinyauth.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
 
package io.tinyauth.elasticsearch;

import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.elasticsearch.action.ActionRequest;
 
 
public class ActionNameAdaptor {

  public static String nameForRequest(String req) {
    return Stream.of(req.split(":"))
      .flatMap(part -> Stream.of(part.split("/")))
      .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
      .collect(Collectors.joining());
  }
}
