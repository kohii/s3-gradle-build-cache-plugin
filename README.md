# s3-gradle-build-cache-plugin

[![Apache License 2.0](https://img.shields.io/badge/License-Apache%20License%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/kohii/s3-gradle-build-cache-plugin.svg?branch=master)](https://travis-ci.org/kohii/s3-gradle-build-cache-plugin)

A Gradle build cache that uses AWS S3 to store build artifacts.

## Compatibility

Plugin made with gradle 5.2.1 so should work for Gradle 5.x. Might work with gradle version 4.x but haven't tested.

## Usage

Please note that this plugin is not yet ready for production.

### Apply plugin

settings.gradle

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.kohii.s3-gradle-build-cache-plugin:0.1.0"
  }
}

apply plugin: 'com.github.kohii.s3-gradle-build-cache-plugin'

ext.isCiServer = System.getenv().containsKey("CI")
 
buildCache {
  local {
    enabled = !isCiServer
  }
  remote(com.github.kohii.gradle.build_cache.s3.S3BuildCache) {
    region = '<your region>'
    bucket = '<your bucket name>'
    push = isCiServer
  }
}
```

### Configuration

| Configuration Key        | Type    | Description                                                                                                | Mandatory | Default Value |
| ------------------------ | ------- | ---------------------------------------------------------------------------------------------------------- | --------- | ------------- |
| awsAccessKeyId           | String  | The AWS access key id                                                                                      |           | [Using the Default Credential Provider Chain](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html#credentials-default) |
| awsSecretKey             | String  | The AWS secret access key                                                                                  |           | [Using the Default Credential Provider Chain](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html#credentials-default) |
| sessionToken             | String  | The AWS sessionToken                                                                                       |           | [Using the Default Credential Provider Chain](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html#credentials-default) |
| region                   | String  | The AWS region                                                                                             | yes       |               |
| bucket                   | String  | The name of the AWS S3 bucket where cache objects should be stored.                                        | yes       |               |
| prefix                   | String  | The prefix of the AWS S3 object key in the bucket                                                          |           |               |
| endpoint                 | String  | The S3 endpoint                                                                                            |           |               |
| reducedRedundancyStorage | boolean | Whether to use [Reduced Redundancy Storage](https://aws.amazon.com/s3/reduced-redundancy/?nc1=h_ls) or not |           | `true`        |

### AWS credentials

By default, this plugin uses `Default Credential Provider Chain` to lookup the AWS credentials.  
See [Using the Default Credential Provider Chain - AWS SDK for Java](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html#credentials-default) for more details.

If you want to set `access key id` and `secret access key` manually,
configure `awsAccessKeyId` and `awsSecretKey` (and `sessionToken` optionally).


### S3 Bucket Policy

The AWS credential must have at least the following permissions to the bucket:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
          "s3:PutObject",
          "s3:GetObject",
          "s3:ListBucket"
      ],
      "Resource": [
          "arn:aws:s3:::your-bucket/*",
          "arn:aws:s3:::your-bucket"
      ]
    }
  ]
}
```

### Run build with build cache

The Gradle build cache is an incubating feature and needs to be enabled per build (`--build-cache`) or in the Gradle properties (`org.gradle.caching=true`).
See [the official doc](https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_enable) for details.

# License

[LICENSE](./LICENSE)