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

import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.settings.Setting;

import java.util.function.UnaryOperator;
import java.util.List;
import java.util.Collections;

import java.net.InetSocketAddress;
import java.net.InetAddress;

import io.tinyauth.elasticsearch.Constants;
import io.tinyauth.elasticsearch.Origin;
import io.tinyauth.elasticsearch.Settings;


public class TinyauthPlugin extends Plugin implements ActionPlugin {

  @Override
  public List<Setting<?>> getSettings() {
    return Settings.getSettings();
  }

  @Override
  public List<Class<? extends ActionFilter>> getActionFilters() {
      return Collections.singletonList(TinyauthActionFilter.class);
  }

  @Override
  public UnaryOperator<RestHandler> getRestHandlerWrapper(ThreadContext threadContext) {
    return restHandler -> (RestHandler) (request, channel, client) -> {
      InetSocketAddress socketAddress = (InetSocketAddress) request.getRemoteAddress();
      InetAddress inetAddress = socketAddress.getAddress();
      threadContext.putTransient(Constants.SOURCE_IP, inetAddress.getHostAddress());

      if (inetAddress.getHostAddress() == null)  
        threadContext.putTransient(Constants.ORIGIN, Origin.INTERNAL_REST);
      else
        threadContext.putTransient(Constants.ORIGIN, Origin.REST);

      threadContext.putTransient(Constants.HEADERS, request.getHeaders());

      restHandler.handleRequest(request, channel, client);
    };
  }
}
