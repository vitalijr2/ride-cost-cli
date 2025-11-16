package io.gitlab.vitalijr2.ridecost.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@Tag("slow")
@ExtendWith(MockitoExtension.class)
public class PositiveDecimalValidationTest {

  @Mock
  private CommandLine commandLine;
  @Mock
  private CommandSpec spec;

  private RideCost instance;

  @BeforeEach
  void setUp() {
    when(spec.commandLine()).thenReturn(commandLine);

    instance = new RideCost();
    instance.spec = spec;
  }

  @DisplayName("Distance must be positive")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"0.0|Must be a positive value, but got 0.0",
      "-1|Must be a positive value, but got -1.0"}, delimiter = '|')
  void distanceMustBePositive(double distance, String expectedMessage) throws IOException {
    // given
    instance.distance = BigDecimal.valueOf(distance);
    instance.volumePerDistance = BigDecimal.valueOf(4.3);
    instance.price = BigDecimal.valueOf(59.99);
    instance.spec = spec;

    // when
    var exceptional = assertThrows(NonPositiveDecimalException.class, instance::run);

    // then
    assertEquals(expectedMessage, exceptional.getMessage());
  }

  @DisplayName("Price must be positive")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"0.0|Must be a positive value, but got 0.0",
      "-1|Must be a positive value, but got -1.0"}, delimiter = '|')
  void priceMustBePositive(double price, String expectedMessage) throws IOException {
    // given
    instance.distance = BigDecimal.valueOf(456);
    instance.volumePerDistance = BigDecimal.valueOf(4.3);
    instance.price = BigDecimal.valueOf(price);
    instance.spec = spec;

    // when
    var exceptional = assertThrows(NonPositiveDecimalException.class, instance::run);

    // then
    assertEquals(expectedMessage, exceptional.getMessage());
  }

  @DisplayName("Distance-per-volume mileage must be positive")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"0.0|Must be a positive value, but got 0.0",
      "-1|Must be a positive value, but got -1.0"}, delimiter = '|')
  void distancePerVolumeMileageMustBePositive(double mileage, String expectedMessage) throws IOException {
    // given
    instance.distance = BigDecimal.valueOf(456);
    instance.distancePerVolume = BigDecimal.valueOf(mileage);
    instance.price = BigDecimal.valueOf(59.99);
    instance.spec = spec;

    // when
    var exceptional = assertThrows(NonPositiveDecimalException.class, instance::run);

    // then
    assertEquals(expectedMessage, exceptional.getMessage());
  }

  @DisplayName("Volume-per-distance mileage must be positive")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"0.0|Must be a positive value, but got 0.0",
      "-1|Must be a positive value, but got -1.0"}, delimiter = '|')
  void volumePerDistanceMileageMustBePositive(double mileage, String expectedMessage) throws IOException {
    // given
    instance.distance = BigDecimal.valueOf(456);
    instance.volumePerDistance = BigDecimal.valueOf(mileage);
    instance.price = BigDecimal.valueOf(59.99);
    instance.spec = spec;

    // when
    var exceptional = assertThrows(NonPositiveDecimalException.class, instance::run);

    // then
    assertEquals(expectedMessage, exceptional.getMessage());
  }

}
