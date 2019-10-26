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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StorageClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gradle.caching.BuildCacheEntryReader;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheException;
import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.BuildCacheService;

@Slf4j
@RequiredArgsConstructor
public class S3BuildCacheService implements BuildCacheService {

  private static final String BUILD_CACHE_CONTENT_TYPE = "application/vnd.gradle.build-cache-artifact";

  private final AmazonS3 amazonS3;
  private final String bucketName;
  private final String prefix;
  private final boolean reducedRedundancyStorage;


  @Override
  public boolean load(BuildCacheKey buildCacheKey, BuildCacheEntryReader buildCacheEntryReader)
      throws BuildCacheException {
    String key = createS3Key(prefix, buildCacheKey.getHashCode());
    if (!amazonS3.doesObjectExist(bucketName, key)) {
      log.info("Build cache not found. key={}", key);
      return false;
    }

    try (S3Object object = amazonS3.getObject(bucketName, key);
         InputStream is = object.getObjectContent()) {
      buildCacheEntryReader.readFrom(is);
      log.info("Build cache found. key={}", key);
      return true;
    } catch (IOException e) {
      throw new BuildCacheException("Error while reading cache object from S3 bucket", e);
    }
  }

  @Override
  public void store(BuildCacheKey buildCacheKey, BuildCacheEntryWriter buildCacheEntryWriter)
      throws BuildCacheException {
    String key = createS3Key(prefix, buildCacheKey.getHashCode());
    log.info("Start storing cache entry. key={}", key);

    try {
      if (buildCacheEntryWriter.getSize() < 10_000_000 /* 10MB */) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
          buildCacheEntryWriter.writeTo(os);
          byte[] bytes = os.toByteArray();

          try (InputStream is = new ByteArrayInputStream(bytes)) {
            putObject(key, is, bytes.length);
          }
        }
      } else {
        File file = File.createTempFile("s3-gradle-build-cache-plugin", ".tmp");
        try (FileOutputStream os = new FileOutputStream(file)) {
          buildCacheEntryWriter.writeTo(os);

          try (InputStream is = new FileInputStream(file)) {
            putObject(key, is, file.length());
          }
        } finally {
          file.delete();
        }
      }
    } catch (IOException e) {
      throw new BuildCacheException("Error while storing cache object in S3 bucket", e);
    }
  }

  private void putObject(String key, InputStream is, long size) {
    ObjectMetadata meta = new ObjectMetadata();
    meta.setContentType(BUILD_CACHE_CONTENT_TYPE);
    meta.setContentLength(size);

    PutObjectRequest request = new PutObjectRequest(bucketName, key, is, meta);
    if (reducedRedundancyStorage) {
      request.setStorageClass(StorageClass.ReducedRedundancy);
    }
    amazonS3.putObject(request);
  }

  static String createS3Key(String prefix, String buildCacheHashCode) {
    if (prefix == null || prefix.isEmpty()) {
      return buildCacheHashCode;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    if (!prefix.endsWith("/")) {
      sb.append("/");
    }
    sb.append(buildCacheHashCode);
    return sb.toString();
  }

  @Override
  public void close() {
    amazonS3.shutdown();
  }
}
