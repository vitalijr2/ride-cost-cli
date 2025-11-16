package io.gitlab.vitalijr2.ridecost.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import io.github.vitalijr2.logging.mock.MockLoggers;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
@MockLoggers
class CalculationTest {

  private static final Logger LOGGER = System.getLogger(RideCost.class.getName());

  private RideCost instance;

  @BeforeEach
  void setUp() {
    instance = new RideCost();
  }

  @DisplayName("Round to a whole number")
  @Test
  void roundToWholeNumber() {
    // given
    instance.distance = BigDecimal.valueOf(456);
    instance.volumePerDistance = BigDecimal.valueOf(4.3);
    instance.price = BigDecimal.valueOf(59.99);
    instance.zeroDigits = true;

    // when
    var cost = instance.estimateRideCost();

    // then
    verify(LOGGER).log(Level.DEBUG, "Exact value is used");
    verify(LOGGER).log(Level.DEBUG, "Estimated cost for volume per distance is 1176.28392");
    verify(LOGGER).log(Level.DEBUG, "Round to a whole number: {0}", cost);

    assertEquals(BigDecimal.valueOf(1176), cost);
  }

  @DisplayName("Round to two digits")
  @Test
  void roundToTwoDigits() {
    // given
    instance.distance = BigDecimal.valueOf(456);
    instance.volumePerDistance = BigDecimal.valueOf(4.3);
    instance.price = BigDecimal.valueOf(59.99);
    instance.twoDigits = true;

    // when
    var cost = instance.estimateRideCost();

    // then
    verify(LOGGER).log(Level.DEBUG, "Exact value is used");
    verify(LOGGER).log(Level.DEBUG, "Estimated cost for volume per distance is 1176.28392");
    verify(LOGGER).log(Level.DEBUG, "Round to two digits: {0}", cost);

    assertEquals(BigDecimal.valueOf(1176.28), cost);
  }

  @DisplayName("Round to three digits")
  @Test
  void roundToThreeDigits() {
    // given
    instance.distance = BigDecimal.valueOf(456);
    instance.volumePerDistance = BigDecimal.valueOf(4.3);
    instance.price = BigDecimal.valueOf(59.99);
    instance.threeDigits = true;

    // when
    var cost = instance.estimateRideCost();

    // then
    verify(LOGGER).log(Level.DEBUG, "Exact value is used");
    verify(LOGGER).log(Level.DEBUG, "Estimated cost for volume per distance is 1176.28392");
    verify(LOGGER).log(Level.DEBUG, "Round to three digits: {0}", cost);

    assertEquals(BigDecimal.valueOf(1176.284), cost);
  }

  @DisplayName("Round to four digits")
  @Test
  void roundToFourDigits() {
    // given
    instance.distance = BigDecimal.valueOf(456);
    instance.volumePerDistance = BigDecimal.valueOf(4.3);
    instance.price = BigDecimal.valueOf(59.99);
    instance.fourDigits = true;

    // when
    var cost = instance.estimateRideCost();

    // then
    verify(LOGGER).log(Level.DEBUG, "Exact value is used");
    verify(LOGGER).log(Level.DEBUG, "Estimated cost for volume per distance is 1176.28392");
    verify(LOGGER).log(Level.DEBUG, "Round to four digits: {0}", cost);

    assertEquals(BigDecimal.valueOf(1176.2839), cost);
  }

  @DisplayName("Mileage: distance per volume")
  @Test
  void distancePerVolume() {
    // given
    instance.distance = BigDecimal.valueOf(456);
    instance.distancePerVolume = BigDecimal.valueOf(23.2);
    instance.price = BigDecimal.valueOf(59.99);

    // when
    var cost = instance.estimateRideCost();

    // then
    verify(LOGGER).log(Level.DEBUG, "Estimated cost for distance per volume is 1179.1136483");

    assertEquals(BigDecimal.valueOf(1179.1136483), cost, "Estimated cost for distance per volume");
  }

  @DisplayName("Mileage: volume per distance")
  @Test
  void volumePerDistance() {
    // given
    instance.distance = BigDecimal.valueOf(456);
    instance.volumePerDistance = BigDecimal.valueOf(4.3);
    instance.price = BigDecimal.valueOf(59.99);

    // when
    var cost = instance.estimateRideCost();

    // then
    verify(LOGGER).log(Level.DEBUG, "Estimated cost for volume per distance is 1176.28392");

    assertEquals(BigDecimal.valueOf(1176.28392), cost, "Estimated cost for volume per distance");
  }

}
