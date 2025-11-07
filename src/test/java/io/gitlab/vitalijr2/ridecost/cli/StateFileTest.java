package io.gitlab.vitalijr2.ridecost.cli;

import static java.util.Objects.isNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mockito;

@Tag("slow")
class StateFileTest {

  @DisplayName("The state file in .local/state")
  @Test
  void localState() throws IOException {
    // when and then
    assertThat(RideCost.getStateFile("****").getCanonicalPath(), endsWith("/.local/state/ridecost.properties"));
  }

  @DisplayName("The state file in a custom folder")
  @Test
  void customState() throws IOException {
    // given
    var customParent = System.getProperty("user.home");

    // when and then
    assertEquals(customParent + "/ridecost.properties", RideCost.getStateFile("HOME").getCanonicalPath());
  }

  @DisplayName("Read state file")
  @ParameterizedTest(name = "{0}")
  @CsvFileSource(resources = "state-file-test.csv", numLinesToSkip = 1, nullValues = "NIL")
  void readStateFile(String name, BigDecimal expectedDistancePerVolume, BigDecimal expectedVolumePerDistance,
      BigDecimal expectedPrice, int expectedRoundTo) {
    // given
    try (var ridecost = Mockito.mockStatic(RideCost.class)) {
      ridecost.when(RideCost::getStateFile).thenReturn(new File("src/test/resources/" + name + ".properties"));
      ridecost.when(RideCost::getInstance).thenCallRealMethod();

      // when
      var instance = RideCost.getInstance();

      // then
      checkBigDecimalIsNullOrExactValue(expectedDistancePerVolume, instance.mileage.distancePerVolume);
      checkBigDecimalIsNullOrExactValue(expectedVolumePerDistance, instance.mileage.volumePerDistance);
      checkBigDecimalIsNullOrExactValue(expectedPrice, instance.price);
      switch (expectedRoundTo) {
        case 0:
          checkRoundTo(instance, true, false, false, false);
          break;
        case 2:
          checkRoundTo(instance, false, true, false, false);
          break;
        case 3:
          checkRoundTo(instance, false, false, true, false);
          break;
        case 4:
          checkRoundTo(instance, false, false, false, true);
          break;
        default:
          checkRoundTo(instance, false, false, false, false);
          break;
      }
    }
  }

  @DisplayName("I/O exception")
  @Test
  void exception() {
    try (var ridecost = Mockito.mockStatic(RideCost.class)) {
      ridecost.when(RideCost::getStateFile).thenReturn(new File("src/test/resources"));
      ridecost.when(RideCost::getInstance).thenCallRealMethod();

      // when
      var instance = assertDoesNotThrow(RideCost::getInstance);

      // then
      assertAll("Clean bean", () -> assertNull(instance.mileage.distancePerVolume),
          () -> assertNull(instance.mileage.volumePerDistance), () -> assertNull(instance.price),
          () -> assertFalse(instance.zeroDigits), () -> assertFalse(instance.twoDigits),
          () -> assertFalse(instance.threeDigits), () -> assertFalse(instance.fourDigits));
    }
  }

  private void checkBigDecimalIsNullOrExactValue(BigDecimal expectedValue, BigDecimal actualValue) {
    if (isNull(expectedValue)) {
      assertNull(actualValue);
    } else {
      assertEquals(expectedValue, actualValue);
    }
  }

  private void checkRoundTo(RideCost instance, boolean expectedZeroDigits, boolean expectedTwoDigits,
      boolean expectedThreeDigits, boolean expectedFourDigits) {
    assertEquals(expectedZeroDigits, instance.zeroDigits);
    assertEquals(expectedTwoDigits, instance.twoDigits);
    assertEquals(expectedThreeDigits, instance.threeDigits);
    assertEquals(expectedFourDigits, instance.fourDigits);
  }

}
