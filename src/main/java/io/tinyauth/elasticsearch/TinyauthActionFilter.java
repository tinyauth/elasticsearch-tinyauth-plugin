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

import java.util.List;
import java.util.Set;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Singleton;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONStringer;

import io.tinyauth.elasticsearch.exceptions.ConnectionError;
import io.tinyauth.elasticsearch.Constants;
import io.tinyauth.elasticsearch.Origin;
import io.tinyauth.elasticsearch.ActionIndicesAdaptor;
import io.tinyauth.elasticsearch.ActionNameAdaptor;


@Singleton
public class TinyauthActionFilter extends AbstractComponent implements ActionFilter {

  private static final Logger logger = Loggers.getLogger(TinyauthActionFilter.class);
  private final ActionIndicesAdaptor indexExtractor;

  private final ThreadPool threadPool;

  private String partition;
  private String serviceName;
  private String region;
  private String endpoint;
  private String accessKeyId;
  private String secretAccessKey;

  @Inject
  public TinyauthActionFilter(Settings settings, ThreadPool threadPool) {
    super(settings);
    this.threadPool = threadPool;

    partition = settings.get("tinyauth.partition", "tinyauth");
    serviceName = settings.get("tinyauth.service_name", "es");
    region = settings.get("tinyauth.region", "default");
    endpoint = settings.get("tinyauth.endpoint");
    accessKeyId = settings.get("tinyauth.access_key_id");
    secretAccessKey = settings.get("tinyauth.secret_access_key");

    indexExtractor = new ActionIndicesAdaptor(partition, serviceName, region);
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

    if (endpoint == null || accessKeyId == null || secretAccessKey == null) {
      logger.error("Authentication endpoint for tinyauth not configured");
      listener.onFailure(new ConnectionError("Authentication not attempted"));
      return;
    }

    String body = "";

    logger.error(indexExtractor.collectPermissions(request));
    

      JSONStringer builder = new JSONStringer();
      builder.object();
      indexExtractor.collectPermissions(request, builder);

      builder.key("headers").array();

      if (threadContext.getTransient(Constants.HEADERS) != null) {
        Map<String,List<String>> headers = threadContext.getTransient(Constants.HEADERS);
        headers.entrySet().stream().forEach(headerPair -> {
          headerPair.getValue().stream().forEach(value -> {
            builder.array().value(headerPair.getKey()).value(value).endArray();
          });
        });
      }

      builder.endArray()
        .key("context")
        .object()
        .key("SourceIp")
        .value((String) threadContext.getTransient(Constants.SOURCE_IP))
        .endObject()
        .endObject();

        body = builder.toString();

        logger.error(body);

    Unirest.post(endpoint + "v1/{service}/authorize-by-token")
      .routeParam("service", serviceName)
      .basicAuth(accessKeyId, secretAccessKey)
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
