package io.gitlab.vitalijr2.ridecost.cli;

import static com.ginsberg.junit.exit.assertions.SystemExitAssertion.assertThatCallsSystemExit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class MainTest {

  @DisplayName("Happy path")
  @Test
  void happyPath() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "-k", "23", "321"};

    // when
    assertThatCallsSystemExit(() -> RideCost.main(commandLineParameters)).withExitCode(0);
  }

  @DisplayName("Missing argument")
  @Test
  void missingArguments() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "-k", "23"};

    // when
    assertThatCallsSystemExit(() -> RideCost.main(commandLineParameters)).withExitCode(2);
  }

}