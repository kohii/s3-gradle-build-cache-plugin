package com.github.kohii.gradle.build_cache.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class S3BuildCacheServiceTest {

  @ParameterizedTest
  @CsvSource({
      "abc, def, abc/def",
      "abc/, def, abc/def",
      ", def, def"
  })
  void testCreateS3Key(String prefix, String hashCode, String expected) {
    assertEquals(expected, S3BuildCacheService.createS3Key(prefix, hashCode));
  }
}