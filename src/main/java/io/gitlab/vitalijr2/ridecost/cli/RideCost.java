/*-
 * ---------------LICENSE_START-----------------
 * Ride Cost Command-Line Tool
 * ---------------------------------------------
 * Copyright (C) 2025 Vitalij Berdinskih
 * ---------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---------------LICENSE_END-------------------
 */
package io.gitlab.vitalijr2.ridecost.cli;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import io.gitlab.vitalijr2.ridecost.estimator.RideCostEstimator;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "ridecost", mixinStandardHelpOptions = true, requiredOptionMarker = '*', versionProvider = RideCostVersion.class)
public class RideCost implements Runnable {

  private static final Logger LOGGER = System.getLogger(RideCost.class.getName());
  private static final ResourceBundle COMMAND_LINE_BUNDLE = ResourceBundle.getBundle("CommandLineBundle");

  @Spec
  CommandSpec spec;

  @Parameters(index = "0", paramLabel = "DISTANCE", descriptionKey = "distance", arity = "0")
  BigDecimal distance;

  @Option(names = {"--miles-per-gallon", "-m", "--kilometres-per-litre", "-k", "--mpg",
      "--kpl"}, paramLabel = "RATIO", descriptionKey = "mileage.distance-per-volume")
  BigDecimal distancePerVolume;

  @Option(names = {"--litres-per-ton-kilometres", "-l", "--gallons-per-ton-miles",
      "-g"}, paramLabel = "RATIO", descriptionKey = "mileage.volume-per-distance")
  BigDecimal volumePerDistance;

  @Option(names = {"--price", "-p"}, paramLabel = "PRICE", descriptionKey = "price")
  BigDecimal price;

  @Option(names = "-0", descriptionKey = "round.zero")
  boolean zeroDigits;

  @Option(names = "-2", descriptionKey = "round.two")
  boolean twoDigits;

  @Option(names = "-3", descriptionKey = "round.three")
  boolean threeDigits;

  @Option(names = "-4", descriptionKey = "round.four")
  boolean fourDigits;

  @Option(names = {"--save", "-s"}, descriptionKey = "state.save")
  boolean saveState;

  public RideCost() {
    try {
      loadState();
    } catch (IOException exception) {
      LOGGER.log(Level.WARNING, exception.getMessage());
    }
  }

  public static void main(String[] args) {
    var commandLine = new CommandLine(new RideCost());

    commandLine.setExitCodeExceptionMapper(new RideCostExitCodeExceptionMapper());
    commandLine.setResourceBundle(COMMAND_LINE_BUNDLE);
    System.exit(commandLine.execute(args));
  }

  @VisibleForTesting
  @NotNull
  static File getStateFile() {
    return getStateFile("XDG_STATE_HOME");
  }

  @VisibleForTesting
  @NotNull
  static File getStateFile(String stateFolderName) {
    var stateFolder = System.getenv().getOrDefault(stateFolderName, System.getProperty("user.home") + "/.local/state");

    return new File(stateFolder, "ridecost.properties");
  }

  @Override
  public void run() {
    validateOptions();
    validatePositiveDecimals();
    System.out.println(estimateRideCost());

    try {
      saveState();
    } catch (IOException exception) {
      LOGGER.log(Level.WARNING, exception.getMessage());
    }
  }

  @VisibleForTesting
  @NotNull BigDecimal estimateRideCost() {
    BigDecimal cost;

    if (isNull(volumePerDistance)) {
      cost = RideCostEstimator.distanceByVolumeEstimator().estimateCostOfRide(distancePerVolume, price, distance);
      LOGGER.log(Level.DEBUG, "Estimated cost for distance per volume is " + cost);
    } else {
      cost = RideCostEstimator.volumeByDistanceEstimator().estimateCostOfRide(volumePerDistance, price, distance);
      LOGGER.log(Level.DEBUG, "Estimated cost for volume per distance is " + cost);
    }
    if (zeroDigits) {
      cost = cost.setScale(0, RoundingMode.HALF_UP);
      LOGGER.log(Level.DEBUG, "Round to a whole number: {0}", cost);
    } else if (twoDigits) {
      cost = cost.setScale(2, RoundingMode.HALF_UP);
      LOGGER.log(Level.DEBUG, "Round to two digits: {0}", cost);
    } else if (threeDigits) {
      cost = cost.setScale(3, RoundingMode.HALF_UP);
      LOGGER.log(Level.DEBUG, "Round to three digits: {0}", cost);
    } else if (fourDigits) {
      cost = cost.setScale(4, RoundingMode.HALF_UP);
      LOGGER.log(Level.DEBUG, "Round to four digits: {0}", cost);
    }

    return cost;
  }

  private void loadState() throws IOException {
    var stateFile = getStateFile();

    if (!stateFile.exists()) {
      return;
    }

    var stateProperties = new Properties();

    stateProperties.load(new FileReader(stateFile));
    restoreMileage(stateProperties);
    restorePrice(stateProperties);
    restoreRounding(stateProperties);
  }

  private void restoreMileage(Properties stateProperties) {
    if (stateProperties.containsKey("distancePerVolume")) {
      distancePerVolume = new BigDecimal(stateProperties.getProperty("distancePerVolume"));
      LOGGER.log(Level.DEBUG, "Saved distance per volume: {0}", distancePerVolume);
    } else if (stateProperties.containsKey("volumePerDistance")) {
      volumePerDistance = new BigDecimal(stateProperties.getProperty("volumePerDistance"));
      LOGGER.log(Level.DEBUG, "Saved volume per distance: {0}", volumePerDistance);
    }
  }

  private void restorePrice(Properties stateProperties) {
    if (stateProperties.containsKey("price")) {
      price = new BigDecimal(stateProperties.getProperty("price"));
      LOGGER.log(Level.DEBUG, "Saved price: {0}", price);
    }
  }

  private void restoreRounding(Properties stateProperties) {
    switch (stateProperties.getProperty("roundTo", "*")) {
      case "0" -> {
        zeroDigits = true;
        LOGGER.log(Level.DEBUG, "Restore rounding to zero digits");
      }
      case "2" -> {
        twoDigits = true;
        LOGGER.log(Level.DEBUG, "Restore rounding to two digits");
      }
      case "3" -> {
        threeDigits = true;
        LOGGER.log(Level.DEBUG, "Restore rounding to three digits");
      }
      case "4" -> {
        fourDigits = true;
        LOGGER.log(Level.DEBUG, "Restore rounding to four digits");
      }
      default -> LOGGER.log(Level.DEBUG, "Exact value is used");
    }
  }

  private void saveMileage(Properties stateProperties) {
    if (nonNull(distancePerVolume)) {
      stateProperties.setProperty("distancePerVolume", distancePerVolume.toString());
      LOGGER.log(Level.DEBUG, "Save distance per volume: {0}", distancePerVolume);
    } else if (nonNull(volumePerDistance)) {
      stateProperties.setProperty("volumePerDistance", volumePerDistance.toString());
      LOGGER.log(Level.DEBUG, "Save volume per distance: {0}", volumePerDistance);
    }
  }

  private void savePrice(Properties stateProperties) {
    stateProperties.setProperty("price", price.toString());
    LOGGER.log(Level.DEBUG, "Save price: {0}", price);
  }

  private void saveRounding(Properties stateProperties) {
    if (zeroDigits) {
      stateProperties.put("roundTo", "0");
      LOGGER.log(Level.DEBUG, "Save rounding to zero digits");
    } else if (twoDigits) {
      stateProperties.put("roundTo", "2");
      LOGGER.log(Level.DEBUG, "Save rounding to two digits");
    } else if (threeDigits) {
      stateProperties.put("roundTo", "3");
      LOGGER.log(Level.DEBUG, "Save rounding to three digits");
    } else if (fourDigits) {
      stateProperties.put("roundTo", "4");
      LOGGER.log(Level.DEBUG, "Save rounding to four digits");
    }
  }

  private void saveState() throws IOException {
    var stateFile = getStateFile();

    if (stateFile.createNewFile()) {
      LOGGER.log(Level.DEBUG, "New state file created: {0}", stateFile.getCanonicalFile());
    } else if (stateFile.canWrite()) {
      LOGGER.log(Level.DEBUG, "Re-use state file: {0}", stateFile.getCanonicalFile());
    } else {
      LOGGER.log(Level.WARNING, "Cannot write to the state file: {0}", stateFile.getCanonicalFile());
      return;
    }

    var stateProperties = new Properties();

    saveMileage(stateProperties);
    savePrice(stateProperties);
    saveRounding(stateProperties);
    stateProperties.store(new FileWriter(stateFile), "ridecost");
  }

  private void validatePositiveDecimals() {
    Stream.of(distance, price, distancePerVolume, volumePerDistance).filter(Objects::nonNull)
        .forEach((value) -> {
          if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NonPositiveDecimalException(spec.commandLine(),
                String.format(COMMAND_LINE_BUNDLE.getString("value.non-positive"), value));
          }
        });
  }

  private void validateOptions() {
    if (nonNull(distancePerVolume) && nonNull(volumePerDistance)) {
      throw new ExclusiveOptionException(spec.commandLine(),
          COMMAND_LINE_BUNDLE.getString("exclusive.two-mileages-simultaneously"));
    } else if (isNull(distancePerVolume) && isNull(volumePerDistance)) {
      throw new RequiredOptionException(spec.commandLine(), COMMAND_LINE_BUNDLE.getString("required.any-mileage"));
    }
    if (isNull(price)) {
      throw new RequiredOptionException(spec.commandLine(), COMMAND_LINE_BUNDLE.getString("required.price"));
    }
    if (isNull(distance)) {
      throw new RequiredOptionException(spec.commandLine(), COMMAND_LINE_BUNDLE.getString("required.distance"));
    }
  }

}
