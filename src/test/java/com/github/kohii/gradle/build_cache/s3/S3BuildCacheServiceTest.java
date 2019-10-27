package com.github.kohii.gradle.build_cache.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import org.gradle.caching.BuildCacheEntryReader;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;

class S3BuildCacheServiceTest {

  @ParameterizedTest
  @CsvSource({
      "prefix1, hashCode1, true",
      "prefix1, hashCode2, false",
      "prefix2, hashCode1, false",
      ", hashCode1, false",
  })
  void testLoad(String prefix, String hashCode, boolean expected)
      throws Exception {
    String bucketName = "bucketName1";

    AmazonS3 amazonS3 = mock(AmazonS3.class);
    when(amazonS3.doesObjectExist(eq(bucketName), anyString())).thenReturn(false);
    when(amazonS3.doesObjectExist(eq(bucketName), eq("prefix1/hashCode1"))).thenReturn(true);
    when(amazonS3.getObject(eq(bucketName), anyString())).thenThrow(new AmazonS3Exception(""));
    when(amazonS3.getObject(eq(bucketName), eq("prefix1/hashCode1"))).thenReturn(createS3Object());

    BuildCacheKey buildCacheKey = mock(BuildCacheKey.class);
    when(buildCacheKey.getHashCode()).thenReturn(hashCode);

    BuildCacheEntryReader buildCacheEntryReader = mock(BuildCacheEntryReader.class);

    S3BuildCacheService buildCacheService = new S3BuildCacheService(amazonS3, bucketName, prefix, true);

    assertEquals(expected, buildCacheService.load(buildCacheKey, buildCacheEntryReader));
  }

  private S3Object createS3Object() {
    S3Object s3Object = new S3Object();
    s3Object.setObjectContent(new ByteArrayInputStream(new byte[0]));
    return s3Object;
  }

  @ParameterizedTest
  @CsvSource({
      "true, 10, REDUCED_REDUNDANCY, java.io.ByteArrayOutputStream",
      "false, 100000000, , java.io.FileOutputStream",
  })
  void testStore(boolean reducedRedundancyStorage,
                 long fileSize,
                 String storageClass,
                 Class<?> outputStreamClass)
      throws Exception {
    String bucketName = "bucketName1";
    String prefix = "prefix1";
    String hashCode = "hashCode1";

    AmazonS3 amazonS3 = mock(AmazonS3.class);
    when(amazonS3.putObject(ArgumentMatchers.argThat(req ->
        !(Objects.equals(req.getBucketName(), bucketName)
            && Objects.equals(req.getKey(), "prefix1/hashCode1")
            && Objects.equals(req.getStorageClass(), storageClass)
            && req.getInputStream() != null))))
        .thenThrow(new AmazonS3Exception(""));

    BuildCacheKey buildCacheKey = mock(BuildCacheKey.class);
    when(buildCacheKey.getHashCode()).thenReturn(hashCode);

    BuildCacheEntryWriter buildCacheEntryWriter = mock(BuildCacheEntryWriter.class);
    when(buildCacheEntryWriter.getSize()).thenReturn(fileSize);

    S3BuildCacheService buildCacheService = new S3BuildCacheService(amazonS3, bucketName, prefix, reducedRedundancyStorage);

    buildCacheService.store(buildCacheKey, buildCacheEntryWriter);
    verify(buildCacheEntryWriter).writeTo(argThat(os -> os.getClass() == outputStreamClass));
  }

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