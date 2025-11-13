package io.gitlab.vitalijr2.ridecost.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
class ExitCodeExceptionMapperTest {

  @DisplayName("Runtime exception")
  @Test
  void runTimeException() {
    // given
    var mapper = new ExitCodeExceptionMapper();

    // when and then
    assertEquals(1, mapper.getExitCode(new RuntimeException("test exception")));
  }

}