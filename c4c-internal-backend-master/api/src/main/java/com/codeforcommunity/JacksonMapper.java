package com.codeforcommunity;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A class for getting a JacksonMapper.
 * TODO: This will be more useful once there are more configurations needed.
 * TODO: This will eventually move into a common module.
 */
public class JacksonMapper {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static ObjectMapper getMapper() {
    return mapper;
  }
}
