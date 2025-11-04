package io.gitlab.vitalijr2.ridecost.cli;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class MainTest {

  @DisplayName("Happy path")
  @Test
  @ExpectSystemExitWithStatus(0)
  void happyPath() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "-k", "23", "321"};

    // when
    RideCost.main(commandLineParameters);
  }

  @DisplayName("Missing argument")
  @Test
  @ExpectSystemExitWithStatus(2)
  void missingArguments() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "-k", "23"};

    // when
    RideCost.main(commandLineParameters);
  }

}