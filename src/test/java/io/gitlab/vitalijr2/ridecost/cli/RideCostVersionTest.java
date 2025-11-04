package io.gitlab.vitalijr2.ridecost.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class RideCostVersionTest {

  @DisplayName("Get a version template")
  @Test
  void getVersionTemplate() throws Exception {
    // given
    var versionProvider = new RideCostVersion();
    var projectVersion = Objects.requireNonNullElse(System.getProperty("maven.project.version"), "@project.version@");

    // when
    var version = versionProvider.getVersion();

    // then
    assertAll("Version", () -> assertThat(version, arrayWithSize(1)),
        () -> assertEquals("${COMMAND-FULL-NAME} " + projectVersion, version[0]));
  }

}