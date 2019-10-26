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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory;

public class S3BuildCacheServiceFactory implements BuildCacheServiceFactory<S3BuildCache> {

  @Override
  public BuildCacheService createBuildCacheService(S3BuildCache configuration, Describer describer) {
    describer
        .type("S3")
        .config("region", configuration.getRegion())
        .config("bucket", configuration.getBucket())
        .config("reducedRedundancyStorage", String.valueOf(configuration.isReducedRedundancyStorage()));

    if (!isEmpty(configuration.getPrefix())) {
      describer.config("prefix", configuration.getPrefix());
    }
    if (!isEmpty(configuration.getEndpoint())) {
      describer.config("endpoint", configuration.getEndpoint());
    }

    return new S3BuildCacheService(
        createAmazonS3Client(configuration),
        configuration.getBucket(),
        configuration.getPrefix(),
        configuration.isReducedRedundancyStorage()
    );
  }

  private AmazonS3 createAmazonS3Client(S3BuildCache configuration) {

    AmazonS3ClientBuilder s3Builder = AmazonS3ClientBuilder.standard();

    if (!isEmpty(configuration.getAwsAccessKeyId()) || !isEmpty(configuration.getAwsSecretKey())) {
      AWSCredentials credentials;
      if (isEmpty(configuration.getSessionToken())) {
        credentials = new BasicAWSCredentials(
            configuration.getAwsAccessKeyId(),
            configuration.getAwsSecretKey());
      } else {
        credentials = new BasicSessionCredentials(
            configuration.getAwsAccessKeyId(),
            configuration.getAwsSecretKey(),
            configuration.getSessionToken());
      }
      s3Builder.setCredentials(new AWSStaticCredentialsProvider(credentials));
    }

    if (!isEmpty(configuration.getRegion())) {
      s3Builder.setRegion(configuration.getRegion());
    }

    if (!isEmpty(configuration.getEndpoint())) {
      s3Builder.setEndpointConfiguration(
          new AwsClientBuilder.EndpointConfiguration(configuration.getEndpoint(), configuration.getRegion()));
    }

    return s3Builder.build();
  }

  private boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }
}
