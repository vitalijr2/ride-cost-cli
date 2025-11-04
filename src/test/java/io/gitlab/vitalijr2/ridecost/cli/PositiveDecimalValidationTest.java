package io.gitlab.vitalijr2.ridecost.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.gitlab.vitalijr2.ridecost.cli.RideCost.Mileage;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@Tag("fast")
@ExtendWith(MockitoExtension.class)
public class PositiveDecimalValidationTest {

  @Mock
  private CommandLine commandLine;
  @Mock
  private CommandSpec spec;

  @InjectMocks
  private RideCost tool;

  @BeforeEach
  void setUp() {
    when(spec.commandLine()).thenReturn(commandLine);
  }

  @DisplayName("Distance must be positive")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"0.0|Must be a positive value, but got 0.0",
      "-1|Must be a positive value, but got -1.0"}, delimiter = '|')
  void distanceMustBePositive(double distance, String expectedMessage) {
    // given
    tool.distance = BigDecimal.valueOf(distance);
    tool.mileage = new Mileage();
    tool.mileage.volumePerDistance = BigDecimal.valueOf(4.3);
    tool.price = BigDecimal.valueOf(59.99);

    // when
    var exceptional = assertThrows(NonPositiveDecimalException.class, tool::run);

    // then
    assertEquals(expectedMessage, exceptional.getMessage());
  }

  @DisplayName("Price must be positive")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"0.0|Must be a positive value, but got 0.0",
      "-1|Must be a positive value, but got -1.0"}, delimiter = '|')
  void priceMustBePositive(double price, String expectedMessage) {
    // given
    tool.distance = BigDecimal.valueOf(456);
    tool.mileage = new Mileage();
    tool.mileage.volumePerDistance = BigDecimal.valueOf(4.3);
    tool.price = BigDecimal.valueOf(price);

    // when
    var exceptional = assertThrows(NonPositiveDecimalException.class, tool::run);

    // then
    assertEquals(expectedMessage, exceptional.getMessage());
  }

  @DisplayName("Distance-per-volume mileage must be positive")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"0.0|Must be a positive value, but got 0.0",
      "-1|Must be a positive value, but got -1.0"}, delimiter = '|')
  void distancePerVolumeMileageMustBePositive(double mileage, String expectedMessage) {
    // given
    tool.distance = BigDecimal.valueOf(456);
    tool.mileage = new Mileage();
    tool.mileage.distancePerVolume = BigDecimal.valueOf(mileage);
    tool.price = BigDecimal.valueOf(59.99);

    // when
    var exceptional = assertThrows(NonPositiveDecimalException.class, tool::run);

    // then
    assertEquals(expectedMessage, exceptional.getMessage());
  }

  @DisplayName("Volume-per-distance mileage must be positive")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"0.0|Must be a positive value, but got 0.0",
      "-1|Must be a positive value, but got -1.0"}, delimiter = '|')
  void volumePerDistanceMileageMustBePositive(double mileage, String expectedMessage) {
    // given
    tool.distance = BigDecimal.valueOf(456);
    tool.mileage = new Mileage();
    tool.mileage.volumePerDistance = BigDecimal.valueOf(mileage);
    tool.price = BigDecimal.valueOf(59.99);

    // when
    var exceptional = assertThrows(NonPositiveDecimalException.class, tool::run);

    // then
    assertEquals(expectedMessage, exceptional.getMessage());
  }

}
