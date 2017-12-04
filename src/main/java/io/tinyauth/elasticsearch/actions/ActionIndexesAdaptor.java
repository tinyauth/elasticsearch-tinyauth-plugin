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

package io.tinyauth.elasticsearch.actions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

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
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.stats.TransportNodesStatsAction;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.common.logging.Loggers;


public class ActionIndexesAdaptor {
  private static final Logger logger = Loggers.getLogger(ActionIndexesAdaptor.class);
  private ArrayList<Method> methods;

  public ActionIndexesAdaptor() {
    methods = new ArrayList<Method>();

    for (Method m: this.getClass().getMethods()) {
      if (m.getName() != "extractIndices")
        continue;

      if (!m.getGenericReturnType().toString().equals("java.util.Set<java.lang.String>"))
        continue;

      logger.error(m);
      methods.add(m);
    }
    // Collections.sort(methods, methodComparator);
  }

  public Set<String> extractIndices(SearchRequest req) {
    logger.error("SearchRequest");
    Set<String> idxs = new HashSet<String>();
    Collections.addAll(idxs, req.indices()); 
    return idxs;
  }

  public Set<String> extractIndices(ActionRequest req) {
    logger.error("ActionRequest");
    return new HashSet<String>();
  }
  
  public Set<String> getIndices(ActionRequest req) {
    for (Method m: methods) {
      Class<?>[] c = m.getParameterTypes();
      if (c[0].isInstance(req)) {
        logger.error("Found adaptor for type " + c[0]);

        try {
          @SuppressWarnings("unchecked")
          Set<String> indices = (Set<String>) m.invoke(this, req);
          return indices;
        } catch (IllegalAccessException e) {
          logger.error("IllegalAccessException");
        } catch (IllegalArgumentException e) {
          logger.error("IllegalArgumentException");
        } catch (InvocationTargetException e) {
          logger.error("InvocationTargetException");
        } catch (NullPointerException e) {
          logger.error("NullPointerException");
        } catch (ExceptionInInitializerError e) {
          logger.error("NullPointerException");
        }
      }
    }
    
    logger.error("Unable to find adaptor for request");
    return new HashSet<String>();
  }
}
