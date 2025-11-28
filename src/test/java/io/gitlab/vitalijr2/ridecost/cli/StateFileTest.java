package io.gitlab.vitalijr2.ridecost.cli;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.vitalijr2.logging.mock.MockLoggers;
import io.gitlab.vitalijr2.ridecost.estimator.RideCostEstimator.Rounding;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mockito;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@Tag("slow")
@MockLoggers
class StateFileTest {

  @AfterAll
  static void tearDownClass() {
    var stateFile = new File("target/create-and-overwrite.properties");

    if (stateFile.exists()) {
      assertTrue(stateFile.delete(), "Could not delete the state file: " + stateFile.getAbsolutePath());
    }
  }

  @DisplayName("The state file in .local/state")
  @Test
  void localState() throws IOException {
    // when and then
    assertThat(RideCost.getStateFile("****", "****").getCanonicalPath(), endsWith("/.local/state/ridecost.properties"));
  }

  @DisplayName("The custom state file from the environment variable")
  @Test
  void customStateFile() throws IOException {
    // when and then
    assertThat(RideCost.getStateFile().getCanonicalPath(), endsWith("/src/test/resources/empty.properties"));
  }

  @DisplayName("The custom state file doesn't exist")
  @Test
  void customStateFileDoesntExist() throws IOException {
    // when and then
    assertThat(RideCost.getStateFile("RIDECOST_STATE_DOESNT_EXIST", "****").getCanonicalPath(),
        endsWith("/.local/state/ridecost.properties"));
  }

  @DisplayName("The state file in a custom folder")
  @Test
  void customStateFolder() throws IOException {
    // given
    var customParent = System.getProperty("user.home");

    // when and then
    assertEquals(customParent + "/ridecost.properties", RideCost.getStateFile("****", "HOME").getCanonicalPath());
  }

  @DisplayName("Read from a state file")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "read-state-file-test.csv", numLinesToSkip = 1, nullValues = "NIL")
  void readFromStateFile(String name, BigDecimal expectedDistancePerVolume, BigDecimal expectedVolumePerDistance,
      BigDecimal expectedPrice, Rounding expectedRounding, String expectedMessage, Integer expectedScale) {
    // given
    try (var ridecost = Mockito.mockStatic(RideCost.class)) {
      ridecost.when(RideCost::getStateFile).thenReturn(new File("src/test/resources/" + name + ".properties"));

      // when
      var instance = new RideCost();

      // then
      var logger = System.getLogger(RideCost.class.getName());

      if (isNull(expectedScale)) {
        verify(logger).log(Level.DEBUG, expectedMessage);
      } else {
        verify(logger).log(Level.DEBUG, expectedMessage, expectedScale);
      }
      if (isNull(expectedRounding)) {
        assertNull(instance.rounding);
      } else {
        assertEquals(expectedRounding, instance.rounding);
      }
      assertBigDecimalIsNullOrExactValue(expectedDistancePerVolume, instance.distancePerVolume);
      assertBigDecimalIsNullOrExactValue(expectedVolumePerDistance, instance.volumePerDistance);
      assertBigDecimalIsNullOrExactValue(expectedPrice, instance.price);
    }
  }

  @DisplayName("Save to a state file")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "save-state-file-test.csv", numLinesToSkip = 1, nullValues = "NIL")
  void saveToStateFile(String name, BigDecimal expectedDistancePerVolume, BigDecimal expectedVolumePerDistance,
      BigDecimal expectedPrice, Rounding expectedRounding, String expectedMessage) throws IOException {
    // given
    try (var ridecost = Mockito.mockStatic(RideCost.class)) {
      var tempFile = File.createTempFile(name + '_', ".properties");

      ridecost.when(RideCost::getStateFile).thenReturn(tempFile);

      var instance = new RideCost();

      instance.distance = BigDecimal.valueOf(500);
      instance.distancePerVolume = expectedDistancePerVolume;
      instance.volumePerDistance = expectedVolumePerDistance;
      instance.price = expectedPrice;
      instance.rounding = expectedRounding;
      instance.saveState = true;

      // when
      instance.run();

      // then
      var logger = System.getLogger(RideCost.class.getName());
      var actualState = new Properties();

      actualState.load(new FileReader(tempFile));
      assertBigDecimalIsNullOrExactValue(expectedDistancePerVolume, actualState.getProperty("distancePerVolume"));
      assertBigDecimalIsNullOrExactValue(expectedVolumePerDistance, actualState.getProperty("volumePerDistance"));
      assertBigDecimalIsNullOrExactValue(expectedPrice, actualState.getProperty("price"));
      if (nonNull(expectedRounding)) {
        assertEquals(expectedRounding, Rounding.valueOf(Integer.parseInt(actualState.getProperty("roundTo"))));
        verify(logger).log(Level.DEBUG, expectedMessage);
      } else {
        assertFalse(actualState.containsKey("roundTo"));
      }
    }
  }

  @DisplayName("Create a state file and overwrite existed one")
  @RepeatedTest(2)
  void createAndOverwrite(RepetitionInfo repetitionInfo) {
    // given
    try (var ridecost = Mockito.mockStatic(RideCost.class)) {
      var stateFile = new File("target/create-and-overwrite.properties");

      if (stateFile.exists() && 0 != repetitionInfo.getCurrentRepetition() % 2) {
        assumeTrue(stateFile.delete(), "Could not delete the state file: " + stateFile.getAbsolutePath());
      }
      ridecost.when(RideCost::getStateFile).thenReturn(stateFile);

      var instance = new RideCost();

      instance.distance = BigDecimal.valueOf(500);
      instance.volumePerDistance = BigDecimal.valueOf(4.3);
      instance.price = BigDecimal.valueOf(59.99);
      if (0 != repetitionInfo.getCurrentRepetition() % 2) {
        instance.zeroDigits = true;
      } else {
        instance.fourDigits = true;
      }
      instance.saveState = true;
      instance.spec = mock(CommandSpec.class);

      when(instance.spec.commandLine()).thenReturn(mock(CommandLine.class));

      // when
      assertDoesNotThrow(instance::run);
    }
  }

  @DisplayName("State file isn't writable")
  @Test
  void stateFileIsNotWritable() {
    // given
    try (var ridecost = Mockito.mockStatic(RideCost.class)) {
      ridecost.when(RideCost::getStateFile).thenReturn(new File("/dev/null"), new File("/proc/version"));

      var instance = new RideCost();

      instance.distance = BigDecimal.valueOf(500);
      instance.volumePerDistance = BigDecimal.valueOf(4.3);
      instance.price = BigDecimal.valueOf(59.99);
      instance.saveState = true;
      instance.spec = mock(CommandSpec.class);

      when(instance.spec.commandLine()).thenReturn(mock(CommandLine.class));

      // when
      assertDoesNotThrow(instance::run);
    }
  }

  @DisplayName("I/O exception while reading")
  @Test
  void readingException() {
    // given
    try (var ridecost = Mockito.mockStatic(RideCost.class)) {
      ridecost.when(RideCost::getStateFile).thenReturn(new File("src/test/resources"));

      // when
      var instance = assertDoesNotThrow(RideCost::new);

      // then
      var logger = System.getLogger(RideCost.class.getName());

      verify(logger).log(Level.WARNING, "src/test/resources (Is a directory)");
      assertAll("Clean bean", () -> assertNull(instance.distancePerVolume),
          () -> assertNull(instance.volumePerDistance), () -> assertNull(instance.price),
          () -> assertFalse(instance.zeroDigits), () -> assertFalse(instance.twoDigits),
          () -> assertFalse(instance.threeDigits), () -> assertFalse(instance.fourDigits));
    }
  }

  @DisplayName("I/O exception while writing")
  @Test
  void writingException() {
    // given
    try (var ridecost = Mockito.mockStatic(RideCost.class)) {
      ridecost.when(RideCost::getStateFile).thenReturn(new File("/dev/null"), new File("target"));

      var instance = new RideCost();

      instance.distance = BigDecimal.valueOf(500);
      instance.distancePerVolume = BigDecimal.valueOf(123.45);
      instance.price = BigDecimal.valueOf(67.89);
      instance.saveState = true;

      // when
      assertDoesNotThrow(instance::run);

      // then
      var logger = System.getLogger(RideCost.class.getName());

      verify(logger).log(Level.WARNING, "target (Is a directory)");
    }
  }

  private void assertBigDecimalIsNullOrExactValue(BigDecimal expectedValue, BigDecimal actualValue) {
    if (isNull(expectedValue)) {
      assertNull(actualValue);
    } else {
      assertEquals(expectedValue, actualValue);
    }
  }

  private void assertBigDecimalIsNullOrExactValue(BigDecimal expectedValue, String actualValue) {
    if (isNull(expectedValue)) {
      assertNull(actualValue);
    } else {
      assertEquals(expectedValue, new BigDecimal(actualValue));
    }
  }

}
