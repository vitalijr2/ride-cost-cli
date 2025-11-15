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

  @DisplayName("Missing a required parameter")
  @Test
  void missingRequiredParameter() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "-k", "23"};

    // when
    assertThatCallsSystemExit(() -> RideCost.main(commandLineParameters)).withExitCode(2);
  }

  @DisplayName("Missing a required option")
  @Test
  void missingRequiredOption() {
    // given
    var commandLineParameters = new String[]{"-k", "23", "321"};

    // when
    assertThatCallsSystemExit(() -> RideCost.main(commandLineParameters)).withExitCode(2);
  }

  @DisplayName("Missing an exclusive option")
  @Test
  void missingExclusiveOption() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "321"};

    // when
    assertThatCallsSystemExit(() -> RideCost.main(commandLineParameters)).withExitCode(2);
  }

  @DisplayName("Mutually exclusive options")
  @Test
  void mutuallyExclusiveOptions() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "-k", "23", "-l", "4.3", "321"};

    // when
    assertThatCallsSystemExit(() -> RideCost.main(commandLineParameters)).withExitCode(2);
  }

}