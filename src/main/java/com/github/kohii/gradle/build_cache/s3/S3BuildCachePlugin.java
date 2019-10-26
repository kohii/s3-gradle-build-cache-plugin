/*
 * Copyright 2019 kohii
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.kohii.gradle.build_cache.s3;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.caching.configuration.BuildCacheConfiguration;

public class S3BuildCachePlugin implements Plugin<Settings> {

  @Override
  public void apply(Settings settings) {
    BuildCacheConfiguration buildCacheConfiguration = settings.getBuildCache();
    buildCacheConfiguration.registerBuildCacheService(S3BuildCache.class, S3BuildCacheServiceFactory.class);
  }
}
