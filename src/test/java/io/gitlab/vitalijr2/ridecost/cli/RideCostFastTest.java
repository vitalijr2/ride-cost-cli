package io.gitlab.vitalijr2.ridecost.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beust.jcommander.ParameterException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("fast")
class RideCostFastTest {

  @DisplayName("Show help message")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"--help", "-h"})
  void showHelp(String helpParameter) {
    // given
    var commandLineParameters = new String[]{helpParameter};

    // when
    var commander = RideCost.readParameters(commandLineParameters);

    // then
    assertTrue(((RideCostConfiguration) commander.getObjects().get(0)).usage);
  }

  @DisplayName("Show help message when other parameters exist")
  @ParameterizedTest(name = "{2}")
  @CsvSource(value = {"-p,123,--help,-k,23,321", "-p,123,-h,-k,23,321"})
  void showHelp(String parameter1, String parameter2, String parameter3, String parameter4, String parameter5,
      String parameter6) {
    // given
    var commandLineParameters = new String[]{parameter1, parameter2, parameter3, parameter4, parameter5, parameter5};

    // when
    var commander = RideCost.readParameters(commandLineParameters);

    // then
    assertTrue(((RideCostConfiguration) commander.getObjects().get(0)).usage);
  }

  @DisplayName("Mileages are mutually exclusive")
  @Test
  void mileagesAreMutuallyExclusive() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "-k", "23", "-l", "4.3", "321"};

    // when
    var exception = assertThrows(ParameterException.class, () -> RideCost.readParameters(commandLineParameters));

    // then
    assertEquals("Fuel efficiency by distance or fuel consumption are mutually exclusive", exception.getMessage());
  }

  @DisplayName("Mileage must be specified")
  @Test
  void mileageMustBeSpecified() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "321"};

    // when
    var exception = assertThrows(ParameterException.class, () -> RideCost.readParameters(commandLineParameters));

    // then
    assertEquals("Fuel efficiency must be specified", exception.getMessage());
  }

  @DisplayName("Round to a whole number")
  @Test
  void roundToWholeNumber() {
    // given
    var configuration = new RideCostConfiguration();

    configuration.distance = BigDecimal.valueOf(456);
    configuration.mileageVolumePerDistance = BigDecimal.valueOf(4.3);
    configuration.price = BigDecimal.valueOf(59.99);
    configuration.zeroDigits = true;

    // when
    var cost = RideCost.estimateRideCost(configuration);

    // then
    assertEquals(BigDecimal.valueOf(1176), cost);
  }

  @DisplayName("Round to two digits")
  @Test
  void roundToTwoDigits() {
    // given
    var configuration = new RideCostConfiguration();

    configuration.distance = BigDecimal.valueOf(456);
    configuration.mileageVolumePerDistance = BigDecimal.valueOf(4.3);
    configuration.price = BigDecimal.valueOf(59.99);
    configuration.twoDigits = true;

    // when
    var cost = RideCost.estimateRideCost(configuration);

    // then
    assertEquals(BigDecimal.valueOf(1176.28), cost);
  }

  @DisplayName("Round to three digits")
  @Test
  void roundToThreeDigits() {
    // given
    var configuration = new RideCostConfiguration();

    configuration.distance = BigDecimal.valueOf(456);
    configuration.mileageVolumePerDistance = BigDecimal.valueOf(4.3);
    configuration.price = BigDecimal.valueOf(59.99);
    configuration.threeDigits = true;

    // when
    var cost = RideCost.estimateRideCost(configuration);

    // then
    assertEquals(BigDecimal.valueOf(1176.284), cost);
  }

  @DisplayName("Round to four digits")
  @Test
  void roundToFourDigits() {
    // given
    var configuration = new RideCostConfiguration();

    configuration.distance = BigDecimal.valueOf(456);
    configuration.mileageVolumePerDistance = BigDecimal.valueOf(4.3);
    configuration.price = BigDecimal.valueOf(59.99);
    configuration.fourDigits = true;

    // when
    var cost = RideCost.estimateRideCost(configuration);

    // then
    assertEquals(BigDecimal.valueOf(1176.2839), cost);
  }

  @DisplayName("Without rounding")
  @Test
  void withoutRounding() {
    // given
    var configuration = new RideCostConfiguration();

    configuration.distance = BigDecimal.valueOf(456);
    configuration.mileageVolumePerDistance = BigDecimal.valueOf(4.3);
    configuration.price = BigDecimal.valueOf(59.99);

    // when
    var cost = RideCost.estimateRideCost(configuration);

    // then
    assertEquals(BigDecimal.valueOf(1176.28392), cost);
  }

}