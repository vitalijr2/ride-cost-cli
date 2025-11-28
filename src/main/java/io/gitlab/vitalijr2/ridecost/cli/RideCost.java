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

import static io.gitlab.vitalijr2.ridecost.cli.RideCostVersion.COMMAND_NAME;
import static io.gitlab.vitalijr2.ridecost.cli.RideCostVersion.VERSION;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import io.gitlab.vitalijr2.ridecost.estimator.RideCostEstimator;
import io.gitlab.vitalijr2.ridecost.estimator.RideCostEstimator.Rounding;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.math.BigDecimal;
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

@Command(name = COMMAND_NAME, mixinStandardHelpOptions = true, requiredOptionMarker = '*', version = {
    COMMAND_NAME + ' ' + VERSION, "picocli " + CommandLine.VERSION,
    "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
    "OS: ${os.name} ${os.version} ${os.arch}"})
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

  @VisibleForTesting
  RideCostEstimator.Rounding rounding;

  public RideCost() {
    try {
      loadState();
    } catch (IOException exception) {
      LOGGER.log(Level.WARNING, exception.getMessage());
    }
  }

  public static void main(String[] args) {
    var commandLine = new CommandLine(new RideCost());

    commandLine.setExitCodeExceptionMapper(new ExitCodeExceptionMapper());
    commandLine.setResourceBundle(COMMAND_LINE_BUNDLE);
    System.exit(commandLine.execute(args));
  }

  @VisibleForTesting
  @NotNull
  static File getStateFile() {
    return getStateFile("RIDECOST_STATE", "XDG_STATE_HOME");
  }

  @VisibleForTesting
  @NotNull
  static File getStateFile(String stateVariableName, String stateFolderName) {
    var stateFileName = System.getenv(stateVariableName);

    if (nonNull(stateFileName)) {
      var stateFile = new File(stateFileName);

      if (stateFile.exists()) {
        return stateFile;
      }
    }

    var stateFolder = System.getenv().getOrDefault(stateFolderName, System.getProperty("user.home") + "/.local/state");

    return new File(stateFolder, "ridecost.properties");
  }

  @Override
  public void run() {
    validateOptions();
    validatePositiveDecimals();
    resolveRounding();
    System.out.println(estimateRideCost());

    try {
      if (saveState) {
        saveState();
      }
    } catch (IOException exception) {
      LOGGER.log(Level.WARNING, exception.getMessage());
    }
  }

  @VisibleForTesting
  @NotNull BigDecimal estimateRideCost() {
    BigDecimal cost;

    if (isNull(volumePerDistance)) {
      cost = RideCostEstimator.distanceByVolumeEstimator()
          .estimateCostOfRide(distancePerVolume, price, distance, rounding);
      LOGGER.log(Level.DEBUG, "Estimated cost for distance per volume is " + cost);
    } else {
      cost = RideCostEstimator.volumeByDistanceEstimator()
          .estimateCostOfRide(volumePerDistance, price, distance, rounding);
      LOGGER.log(Level.DEBUG, "Estimated cost for volume per distance is " + cost);
    }

    return cost;
  }

  @VisibleForTesting
  void resolveRounding() {
    if (zeroDigits) {
      rounding = Rounding.WHOLE;
    } else if (twoDigits) {
      rounding = Rounding.TWO_DECIMAL_PLACES;
    } else if (threeDigits) {
      rounding = Rounding.THREE_DECIMAL_PLACES;
    } else if (fourDigits) {
      rounding = Rounding.FOUR_DECIMAL_PLACES;
    }
    if (nonNull(rounding)) {
      LOGGER.log(Level.DEBUG, rounding.roundingDescription + " is used");
    } else {
      LOGGER.log(Level.DEBUG, "Exact value is used");
    }
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
      LOGGER.log(Level.DEBUG, "Restore distance per volume: {0}", distancePerVolume);
    } else if (stateProperties.containsKey("volumePerDistance")) {
      volumePerDistance = new BigDecimal(stateProperties.getProperty("volumePerDistance"));
      LOGGER.log(Level.DEBUG, "Restore volume per distance: {0}", volumePerDistance);
    }
  }

  private void restorePrice(Properties stateProperties) {
    if (stateProperties.containsKey("price")) {
      price = new BigDecimal(stateProperties.getProperty("price"));
      LOGGER.log(Level.DEBUG, "Restore price: {0}", price);
    }
  }

  private void restoreRounding(Properties stateProperties) {
    try {
      rounding = Rounding.valueOf(Integer.parseInt(stateProperties.getProperty("roundTo", "*")));
      if (isNull(rounding)) {
        LOGGER.log(Level.DEBUG, "Inappropriate rounding: {0}",
            Integer.parseInt(stateProperties.getProperty("roundTo")));
      } else {
        LOGGER.log(Level.DEBUG, rounding.roundingDescription + " is restored");
      }
    } catch (NumberFormatException exception) {
      LOGGER.log(Level.DEBUG, "Rounding isn't saved");
    }
  }

  private void saveMileage(Properties stateProperties) {
    if (nonNull(distancePerVolume)) {
      stateProperties.setProperty("distancePerVolume", distancePerVolume.toString());
      LOGGER.log(Level.DEBUG, "Save distance per volume: {0}", distancePerVolume);
    } else {
      stateProperties.setProperty("volumePerDistance", volumePerDistance.toString());
      LOGGER.log(Level.DEBUG, "Save volume per distance: {0}", volumePerDistance);
    }
  }

  private void savePrice(Properties stateProperties) {
    stateProperties.setProperty("price", price.toString());
    LOGGER.log(Level.DEBUG, "Save price: {0}", price);
  }

  private void saveRounding(Properties stateProperties) {
    if (nonNull(rounding)) {
      stateProperties.put("roundTo", Integer.toString(rounding.decimalPlaces));
      LOGGER.log(Level.DEBUG, rounding.roundingDescription + " is saved");
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
    stateProperties.store(new FileWriter(stateFile), COMMAND_NAME + ' ' + VERSION);
  }

  private void validatePositiveDecimals() {
    Stream.of(distance, price, distancePerVolume, volumePerDistance).filter(Objects::nonNull).forEach((value) -> {
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
      throw new RequiredParameterException(spec.commandLine(), COMMAND_LINE_BUNDLE.getString("required.distance"));
    }
  }

}
