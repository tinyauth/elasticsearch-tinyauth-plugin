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

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.CompositeIndicesRequest;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkShardRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.stats.TransportNodesStatsAction;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.common.logging.Loggers;

import org.json.JSONStringer;

import org.guavaberry.collections.DefaultHashMap;


interface PermissionExtractor {
  void extract(Map<String, Set<String>> permissions, ActionRequest request);
}


public class ActionIndicesAdaptor {
  private static final Logger logger = Loggers.getLogger(ActionIndicesAdaptor.class);

  private String partition;
  private String service;
  private String region;

  private HashMap<Class<?>, PermissionExtractor> methods;

  public ActionIndicesAdaptor(String partition, String service, String region) {
    this.partition = partition;
    this.service = service;
    this.region = region;

    this.methods = new HashMap<>();
    
    this.methods.put(MultiGetRequest.class, (permissions, request) -> {
      MultiGetRequest req = (MultiGetRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadMget");
      req.getItems().stream()
        .flatMap(ir -> Stream.of(ir.indices()))
        .map(idx -> formatArn("index", idx))
        .forEach(permission::add);
    });

    this.methods.put(MultiSearchRequest.class, (permissions, request) -> {
      MultiSearchRequest req = (MultiSearchRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadMsearch");
      req.requests().stream()
        .flatMap(ir -> Stream.of(ir.indices()))
        .map(idx -> formatArn("index", idx))
        .forEach(permission::add);
    });

    this.methods.put(MultiTermVectorsRequest.class, (permissions, request) -> {
      MultiTermVectorsRequest req = (MultiTermVectorsRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadMtv");
      req.getRequests().stream()
        .flatMap(ir -> Stream.of(ir.indices()))
        .map(idx -> formatArn("index", idx))
        .forEach(permission::add);
    });

    this.methods.put(BulkRequest.class, (permissions, request) -> {
      BulkRequest req = (BulkRequest)request;
      req.requests().stream()
        .forEach(ir -> getIndices(permissions, (ActionRequest) ir));
    });

    this.methods.put(DeleteRequest.class, (permissions, request) -> {
      DeleteRequest req = (DeleteRequest)request;
      Set<String> permission = permissions.get("IndicesDataWriteDelete");
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);
    });

    this.methods.put(IndexRequest.class, (permissions, request) -> {
      IndexRequest req = (IndexRequest)request;
      Set<String> permission = permissions.get("IndicesDataWriteIndex");
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);
    });

    this.methods.put(SearchRequest.class, (permissions, request) -> {
      SearchRequest req = (SearchRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadSearch");
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);
    });
  }
  
  private String formatArn(String resourceType, String resource) {
    return String.join(":", 
      "arn",
      partition,
      service,
      region,
      "",
      resourceType + "/" + resource
    );
  }

  private void getIndices(Map<String, Set<String>>permissions, ActionRequest req) {
    PermissionExtractor extractor = methods.get(req.getClass());
    if (extractor == null) {
      logger.error("Unable to find adaptor for request. This is a bug!");
      return;
    }
    extractor.extract(permissions, req);
  }

  public Map<String, Set<String>> collectPermissions(ActionRequest req) {
    DefaultHashMap<String, Set<String>> permissions = new DefaultHashMap<>(() -> new HashSet<String>());
    getIndices(permissions, req);
    return permissions;
  }
  
  public void collectPermissions(ActionRequest req, JSONStringer stringer) {
    stringer.key("grant");
    stringer.object();

    for (Map.Entry<String, Set<String>> entry : collectPermissions(req).entrySet()) {
      stringer.key(entry.getKey());
      stringer.array();
      entry.getValue().stream().forEach(r -> stringer.value(r));
      stringer.endArray();
    }

    stringer.endObject();
  }
}
