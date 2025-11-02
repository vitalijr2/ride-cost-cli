package io.gitlab.vitalijr2.ridecost.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.beust.jcommander.ParameterException;
import io.github.vitalijr2.logging.mock.MockLoggers;
import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag("slow")
@MockLoggers
class RideCostSlowTest {

  @DisplayName("Required parameter is missed")
  @Test
  void readParameters() {
    // given
    var commandLineParameters = new String[0];

    // when && then
    assertThrows(ParameterException.class, () -> RideCost.readParameters(commandLineParameters));
  }

  @DisplayName("Required parameters")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"-k,23", "-l,4.3"})
  void happyPath(String parameter, String value) {
    // given
    var commandLineParameters = new String[]{"-p", "123", parameter, value, "321"};

    // when
    var commander = assertDoesNotThrow(() -> RideCost.readParameters(commandLineParameters));

    // then
    assertAll("Configuration", () -> assertThat("Size of the object list", commander.getObjects(), hasSize(1)),
        () -> assertInstanceOf(RideCostConfiguration.class, commander.getObjects().get(0), "Type"),
        () -> assertFalse(((RideCostConfiguration) commander.getObjects().get(0)).usage, "Help parameter isn't set"));
  }

  @DisplayName("Mileage: distance per volume")
  @Test
  void distancePerVolume() {
    // given
    var configuration = new RideCostConfiguration();

    configuration.distance = BigDecimal.valueOf(456);
    configuration.mileageDistancePerVolume = BigDecimal.valueOf(23.2);
    configuration.price = BigDecimal.valueOf(59.99);

    // when
    var cost = RideCost.estimateRideCost(configuration);

    // then
    var logger = System.getLogger(RideCost.class.getName());

    verify(logger).log(Level.DEBUG, "Estimated cost for distance per volume is 1179.1136483");
    assertEquals(BigDecimal.valueOf(1179.1136483), cost, "Estimated cost for distance per volume");
  }

  @DisplayName("Mileage: volume per distance")
  @Test
  void volumePerDistance() {
    // given
    var configuration = new RideCostConfiguration();

    configuration.distance = BigDecimal.valueOf(456);
    configuration.mileageVolumePerDistance = BigDecimal.valueOf(4.3);
    configuration.price = BigDecimal.valueOf(59.99);

    // when
    var cost = RideCost.estimateRideCost(configuration);

    // then
    var logger = System.getLogger(RideCost.class.getName());

    verify(logger).log(Level.DEBUG, "Estimated cost for volume per distance is 1176.28392");
    assertEquals(BigDecimal.valueOf(1176.28392), cost, "Estimated cost for volume per distance");
  }

  @DisplayName("Happy path")
  @Test
  void happyPath() {
    // given
    var commandLineParameters = new String[]{"-p", "123", "-k", "23", "321"};

    // when
    assertDoesNotThrow(() -> RideCost.main(commandLineParameters));
  }

  @DisplayName("Usage")
  @Test
  void usage() {
    // given
    var commandLineParameters = new String[]{"-h"};

    // when
    assertDoesNotThrow(() -> RideCost.main(commandLineParameters));

    // then
    var logger = System.getLogger(RideCost.class.getName());

    verify(logger).log(Level.DEBUG, "Show help message");
  }

  @DisplayName("Warning message")
  @Test
  void warning() {
    // given
    var commandLineParameters = new String[0];

    // when
    assertDoesNotThrow(() -> RideCost.main(commandLineParameters));

    // then
    var logger = System.getLogger(RideCost.class.getName());

    verify(logger).log(Level.WARNING, "Main parameters are required (\"Distance of the ride, kilometers or miles\")");
  }

}