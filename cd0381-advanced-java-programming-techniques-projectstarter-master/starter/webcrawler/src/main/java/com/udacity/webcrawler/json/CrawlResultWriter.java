package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class CrawlResultWriter {

  private final CrawlResult result;

  public CrawlResultWriter(CrawlResult result) {
    this.result = result;
  }

  public void write(Path path) {
    try (Writer writer = Files.newBufferedWriter(
            path,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
    )) {
      write(writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void write(Writer writer) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

    try {
      mapper.writeValue(writer, result);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}