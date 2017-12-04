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

import java.util.Arrays;
import java.util.List;
import java.io.IOException;

import org.apache.logging.log4j.Logger;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Singleton;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.env.Environment;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.transport.TransportRequest;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.CompositeIndicesRequest;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkShardRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.common.util.concurrent.ThreadContext;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.Set;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import io.tinyauth.elasticsearch.exceptions.ConnectionError;
import io.tinyauth.elasticsearch.Constants;
import io.tinyauth.elasticsearch.Origin;

import static org.elasticsearch.common.xcontent.XContentFactory.*;
import static io.tinyauth.elasticsearch.RequestToIndices.getIndices;


@Singleton
public class TinyauthActionFilter extends AbstractComponent implements ActionFilter {

  private static final Logger logger = Loggers.getLogger(TinyauthActionFilter.class);
  private final ThreadPool threadPool;

  private String endpoint;
  private String access_key_id;
  private String secret_access_key;

  @Inject
  public TinyauthActionFilter(Settings settings, ThreadPool threadPool) {
    super(settings);
    this.threadPool = threadPool;

    endpoint = settings.get("tinyauth.endpoint");
    access_key_id = settings.get("tinyauth.access_key_id");
    secret_access_key = settings.get("tinyauth.secret_access_key");
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }

  @Override
  public <Request extends ActionRequest, Response extends ActionResponse> void apply(Task task,
                                                                                     String action,
                                                                                     Request request,
                                                                                     ActionListener<Response> listener,
                                                                                     ActionFilterChain<Request, Response> chain) {

    ThreadContext threadContext = threadPool.getThreadContext();

    if (threadContext.getTransient(Constants.ORIGIN) == null)
      threadContext.putTransient(Constants.ORIGIN, Origin.LOCAL);

    if ((String)threadContext.getTransient(Constants.ORIGIN) != Origin.REST) {
      logger.debug("Tinyauth only enabled for requests coming from external REST channels");
      chain.proceed(task, action, request, listener);
      return;
    }

    if (action.startsWith("internal")) {
      logger.debug("No authentication required for internal requests");
      chain.proceed(task, action, request, listener);
      return;
    }

    if (endpoint == null || access_key_id == null || secret_access_key == null) {
      logger.error("Authentication endpoint for tinyauth not configured");
      listener.onFailure(new ConnectionError("Authentication not attempted"));
      return;
    }

    String body = "";

    try {
        XContentBuilder builder = jsonBuilder()
          .startObject()
          .field("action", action)
          .field("resource", "")
          .startArray("headers");

        if (threadContext.getTransient(Constants.HEADERS) != null) {
          Map<String,List<String>> headers = threadContext.getTransient(Constants.HEADERS);
          for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
              builder = builder.startArray().value(entry.getKey()).value(value).endArray();
            }
          }
        }
        builder = builder.endArray()
          .startObject("context")
          .field("SourceIp", (String) threadContext.getTransient(Constants.SOURCE_IP))
          .endObject()
          .endObject();

        body = builder.string();

        logger.error(body);
    } catch (IOException e) {
       listener.onFailure(new ConnectionError("Unexpected exception"));
       logger.error("IO error while building auth request for " + action);
    }

    Unirest.post(endpoint + "v1/authorize")
      .basicAuth(access_key_id, secret_access_key)
      .header("accept", "application/json")
      .header("content-type", "application/json")
      .body(body)
      .asStringAsync(new Callback<String>() {
        @Override
        public void failed(UnirestException e) {
          logger.error("The request failed" + e);
          listener.onFailure(new ConnectionError("The authorization could not be completed"));
        }

        @Override
        public void completed(HttpResponse<String> response) {
          logger.error("The request completed\n" + response.getBody());

          try {
            JSONObject authz = new JSONObject(response.getBody());
            if (authz.getBoolean("Authorized")) {
              logger.error("is authorized");
              chain.proceed(task, action, request, listener);
            } else {
              logger.error("is not authorized");
              listener.onFailure(new ElasticsearchSecurityException("no permissions for user", RestStatus.FORBIDDEN));
            }
          } catch (JSONException e) {
            logger.error(e);
            listener.onFailure(new ConnectionError("Authentication failed"));
            return;
          }
        }

        @Override
        public void cancelled() {
          logger.error("The request was cancelled");
          listener.onFailure(new ConnectionError("The authorization was cancelled"));
        }
      });
  }
}
