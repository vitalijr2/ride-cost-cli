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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import io.gitlab.vitalijr2.ridecost.estimator.RideCostEstimator;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

public class RideCost {

  private static final Logger LOGGER = System.getLogger(RideCost.class.getName());

  public static void main(String[] args) {
    try {
      var commander = readParameters(args);
      var configuration = (RideCostConfiguration) commander.getObjects().get(0);

      if (configuration.usage) {
        commander.usage();
        return;
      }
      System.out.println(estimateRideCost(configuration));
    } catch (ParameterException parameterException) {
      LOGGER.log(Level.WARNING, parameterException.getMessage());
      parameterException.usage();
    }
  }

  @VisibleForTesting
  @NotNull
  static BigDecimal estimateRideCost(RideCostConfiguration configuration) {
    BigDecimal cost;

    if (isNull(configuration.mileageVolumePerDistance)) {
      cost = RideCostEstimator.distanceByVolumeEstimator()
          .estimateCostOfRide(configuration.mileageDistancePerVolume, configuration.price, configuration.distance);
      LOGGER.log(Level.DEBUG, "Estimated cost for distance per volume is " + cost);
    } else {
      cost = RideCostEstimator.volumeByDistanceEstimator()
          .estimateCostOfRide(configuration.mileageVolumePerDistance, configuration.price, configuration.distance);
      LOGGER.log(Level.DEBUG, "Estimated cost for volume per distance is " + cost);
    }
    if (configuration.zeroDigits) {
      cost = cost.setScale(0, RoundingMode.HALF_UP);
      LOGGER.log(Level.DEBUG, "Round to a whole number: {0}", cost);
    } else if (configuration.twoDigits) {
      cost = cost.setScale(2, RoundingMode.HALF_UP);
      LOGGER.log(Level.DEBUG, "Round to two digits: {0}", cost);
    } else if (configuration.threeDigits) {
      cost = cost.setScale(3, RoundingMode.HALF_UP);
      LOGGER.log(Level.DEBUG, "Round to three digits: {0}", cost);
    } else if (configuration.fourDigits) {
      cost = cost.setScale(4, RoundingMode.HALF_UP);
      LOGGER.log(Level.DEBUG, "Round to four digits: {0}", cost);
    }

    return cost;
  }

  @VisibleForTesting
  @NotNull
  static JCommander readParameters(String[] args) throws ParameterException {
    var commander = JCommander.newBuilder().addObject(new RideCostConfiguration()).programName(RideCost.class.getName())
        .args(args).build();
    var configuration = (RideCostConfiguration) commander.getObjects().get(0);
    var parameterBundle = ResourceBundle.getBundle("ParameterBundle");

    if (configuration.usage) {
      LOGGER.log(Level.DEBUG, "Show help message");
      return commander;
    }
    if (nonNull(configuration.mileageDistancePerVolume) && nonNull(configuration.mileageVolumePerDistance)) {
      var parameterException = new ParameterException(
          parameterBundle.getString("exclusive.two-mileages-simultaneously"));

      parameterException.setJCommander(commander);
      throw parameterException;
    }
    if (isNull(configuration.mileageDistancePerVolume) && isNull(configuration.mileageVolumePerDistance)) {
      var parameterException = new ParameterException(parameterBundle.getString("required.any-mileage"));

      parameterException.setJCommander(commander);
      throw parameterException;
    }

    return commander;
  }

  private RideCost() {
  }

}
