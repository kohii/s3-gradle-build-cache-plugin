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

import lombok.Getter;
import lombok.Setter;
import org.gradle.caching.configuration.AbstractBuildCache;

@Setter
@Getter
public class S3BuildCache extends AbstractBuildCache {

  private String awsAccessKeyId;
  private String awsSecretKey;
  private String sessionToken;

  private String region;
  private String bucket;
  private String prefix;
  private String endpoint;
  private boolean reducedRedundancyStorage = true;
}
