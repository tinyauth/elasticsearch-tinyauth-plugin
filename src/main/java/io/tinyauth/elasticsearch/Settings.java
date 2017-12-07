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

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;

import java.util.Arrays;
import java.util.List;


public class Settings {

  public static final Setting<String> PARTITION_SETTING = Setting.simpleString(
    "tinyauth.partition",
    Property.NodeScope
  );

  public static final Setting<String> SERVICE_NAME_SETTING = Setting.simpleString(
    "tinyauth.service_name",
    Property.NodeScope
  );

  public static final Setting<String> REGION_SETTING = Setting.simpleString(
    "tinyauth.region",
    Property.NodeScope
  );

  public static final Setting<String> ENDPOINT_SETTING = Setting.simpleString(
      "tinyauth.endpoint",
      Property.NodeScope
  );

  public static final Setting<String> ACCESS_KEY_ID_SETTING = Setting.simpleString(
      "tinyauth.access_key_id",
      Property.NodeScope
  );

  public static final Setting<String> SECRET_ACCESS_KEY_SETTING = Setting.simpleString(
      "tinyauth.secret_access_key",
      Property.NodeScope
  );

  public static final Setting<Boolean> SSL_VERIFY_SETTING = Setting.boolSetting(
      "tinyauth.ssl_verify",
      false,
      Property.NodeScope
  );

  public static List<Setting<?>> getSettings() {
    return Arrays.asList(
      PARTITION_SETTING,
      SERVICE_NAME_SETTING,
      REGION_SETTING,
      ENDPOINT_SETTING,
      ACCESS_KEY_ID_SETTING,
      SECRET_ACCESS_KEY_SETTING,
      SSL_VERIFY_SETTING
    );
  }
}
