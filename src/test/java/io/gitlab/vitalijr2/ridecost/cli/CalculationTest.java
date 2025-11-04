package io.gitlab.vitalijr2.ridecost.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import io.github.vitalijr2.logging.mock.MockLoggers;
import io.gitlab.vitalijr2.ridecost.cli.RideCost.Mileage;
import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
@MockLoggers
class CalculationTest {

  private RideCost tool;

  @BeforeEach
  void setUp() {
    tool = new RideCost();
  }

  @DisplayName("Round to a whole number")
  @Test
  void roundToWholeNumber() {
    // given
    tool.distance = BigDecimal.valueOf(456);
    tool.mileage = new Mileage();
    tool.mileage.volumePerDistance = BigDecimal.valueOf(4.3);
    tool.price = BigDecimal.valueOf(59.99);
    tool.zeroDigits = true;

    // when
    var cost = tool.estimateRideCost();

    // then
    assertEquals(BigDecimal.valueOf(1176), cost);
  }

  @DisplayName("Round to two digits")
  @Test
  void roundToTwoDigits() {
    // given
    tool.distance = BigDecimal.valueOf(456);
    tool.mileage = new Mileage();
    tool.mileage.volumePerDistance = BigDecimal.valueOf(4.3);
    tool.price = BigDecimal.valueOf(59.99);
    tool.twoDigits = true;

    // when
    var cost = tool.estimateRideCost();

    // then
    assertEquals(BigDecimal.valueOf(1176.28), cost);
  }

  @DisplayName("Round to three digits")
  @Test
  void roundToThreeDigits() {
    // given
    tool.distance = BigDecimal.valueOf(456);
    tool.mileage = new Mileage();
    tool.mileage.volumePerDistance = BigDecimal.valueOf(4.3);
    tool.price = BigDecimal.valueOf(59.99);
    tool.threeDigits = true;

    // when
    var cost = tool.estimateRideCost();

    // then
    assertEquals(BigDecimal.valueOf(1176.284), cost);
  }

  @DisplayName("Round to four digits")
  @Test
  void roundToFourDigits() {
    // given
    tool.distance = BigDecimal.valueOf(456);
    tool.mileage = new Mileage();
    tool.mileage.volumePerDistance = BigDecimal.valueOf(4.3);
    tool.price = BigDecimal.valueOf(59.99);
    tool.fourDigits = true;

    // when
    var cost = tool.estimateRideCost();

    // then
    assertEquals(BigDecimal.valueOf(1176.2839), cost);
  }

  @DisplayName("Mileage: distance per volume")
  @Test
  void distancePerVolume() {
    // given
    tool.distance = BigDecimal.valueOf(456);
    tool.mileage = new Mileage();
    tool.mileage.distancePerVolume = BigDecimal.valueOf(23.2);
    tool.price = BigDecimal.valueOf(59.99);

    // when
    var cost = tool.estimateRideCost();

    // then
    var logger = System.getLogger(RideCost.class.getName());

    verify(logger).log(Level.DEBUG, "Estimated cost for distance per volume is 1179.1136483");
    assertEquals(BigDecimal.valueOf(1179.1136483), cost, "Estimated cost for distance per volume");
  }

  @DisplayName("Mileage: volume per distance")
  @Test
  void volumePerDistance() {
    // given
    tool.distance = BigDecimal.valueOf(456);
    tool.mileage = new Mileage();
    tool.mileage.volumePerDistance = BigDecimal.valueOf(4.3);
    tool.price = BigDecimal.valueOf(59.99);

    // when
    var cost = tool.estimateRideCost();

    // then
    var logger = System.getLogger(RideCost.class.getName());

    verify(logger).log(Level.DEBUG, "Estimated cost for volume per distance is 1176.28392");
    assertEquals(BigDecimal.valueOf(1176.28392), cost, "Estimated cost for volume per distance");
  }

}