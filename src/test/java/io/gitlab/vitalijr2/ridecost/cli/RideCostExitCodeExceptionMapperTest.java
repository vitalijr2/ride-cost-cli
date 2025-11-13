package io.gitlab.vitalijr2.ridecost.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideCostExitCodeExceptionMapperTest {

  @DisplayName("Runtime exception")
  @Test
  void runTimeException() {
    // given
    var mapper = new RideCostExitCodeExceptionMapper();

    // when and then
    assertEquals(1, mapper.getExitCode(new RuntimeException("test exception")));
  }

}