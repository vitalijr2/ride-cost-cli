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

import io.gitlab.vitalijr2.ridecost.estimator.RideCostEstimator;
import java.io.File;
import java.io.FileReader;
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
import picocli.CommandLine.ArgGroup;
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

  @Parameters(index = "0", paramLabel = "DISTANCE", descriptionKey = "distance", arity = "1")
  BigDecimal distance;

  @Option(names = {"--price", "-p"}, paramLabel = "PRICE", descriptionKey = "price", required = true)
  BigDecimal price;

  @ArgGroup(exclusive = true, multiplicity = "1")
  Mileage mileage;

  @Option(names = "-0", descriptionKey = "round.zero")
  boolean zeroDigits;

  @Option(names = "-2", descriptionKey = "round.two")
  boolean twoDigits;

  @Option(names = "-3", descriptionKey = "round.three")
  boolean threeDigits;

  @Option(names = "-4", descriptionKey = "round.four")
  boolean fourDigits;

  public static void main(String[] args) {
    var commandLine = new CommandLine(getInstance());

    commandLine.setResourceBundle(COMMAND_LINE_BUNDLE);
    System.exit(commandLine.execute(args));
  }

  @VisibleForTesting
  @NotNull
  static RideCost getInstance() {
    var instance = new RideCost();

    instance.mileage = new Mileage();

    try {
      var stateFile = getStateFile();

      if (stateFile.exists()) {
        var stateProperties = new Properties();

        stateProperties.load(new FileReader(stateFile));
        if (stateProperties.containsKey("distancePerVolume")) {
          instance.mileage.distancePerVolume = new BigDecimal(stateProperties.getProperty("distancePerVolume"));
        } else if (stateProperties.containsKey("volumePerDistance")) {
          instance.mileage.volumePerDistance = new BigDecimal(stateProperties.getProperty("volumePerDistance"));
        }
        if (stateProperties.containsKey("price")) {
          instance.price = new BigDecimal(stateProperties.getProperty("price"));
        }
        if (stateProperties.containsKey("roundTo")) {
          switch (stateProperties.getProperty("roundTo")) {
            case "0" -> instance.zeroDigits = true;
            case "2" -> instance.twoDigits = true;
            case "3" -> instance.threeDigits = true;
            case "4" -> instance.fourDigits = true;
          }
        }
      }
    } catch (IOException exception) {
      LOGGER.log(Level.WARNING, exception.getMessage(), exception);
    }

    return instance;
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
    validatePositiveDecimals();
    System.out.println(estimateRideCost());
  }

  @VisibleForTesting
  BigDecimal estimateRideCost() {
    BigDecimal cost;

    if (isNull(mileage.volumePerDistance)) {
      cost = RideCostEstimator.distanceByVolumeEstimator()
          .estimateCostOfRide(mileage.distancePerVolume, price, distance);
      LOGGER.log(Level.DEBUG, "Estimated cost for distance per volume is " + cost);
    } else {
      cost = RideCostEstimator.volumeByDistanceEstimator()
          .estimateCostOfRide(mileage.volumePerDistance, price, distance);
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

  private void validatePositiveDecimals() {
    Stream.of(distance, price, mileage.distancePerVolume, mileage.volumePerDistance).filter(Objects::nonNull)
        .forEach((value) -> {
          if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NonPositiveDecimalException(spec.commandLine(),
                String.format(COMMAND_LINE_BUNDLE.getString("value.non-positive"), value));
          }
        });
  }

  static class Mileage {

    @Option(names = {"--miles-per-gallon", "-m", "--kilometres-per-litre", "-k", "--mpg",
        "--kpl"}, paramLabel = "RATIO", descriptionKey = "mileage.distance-per-volume")
    BigDecimal distancePerVolume;

    @Option(names = {"--litres-per-ton-kilometres", "-l", "--gallons-per-ton-miles",
        "-g"}, paramLabel = "RATIO", descriptionKey = "mileage.volume-per-distance")
    BigDecimal volumePerDistance;

  }

}
