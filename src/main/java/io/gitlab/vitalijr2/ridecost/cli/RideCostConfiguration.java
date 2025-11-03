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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.math.BigDecimal;

@Parameters(resourceBundle = "ParameterBundle")
class RideCostConfiguration {

  @Parameter(converter = BigDecimalConverter.class, descriptionKey = "distance", arity = 1, required = true)
  BigDecimal distance;

  @Parameter(names = {"--price",
      "-p"}, converter = BigDecimalConverter.class, descriptionKey = "price", required = true)
  BigDecimal price;

  @Parameter(names = {"--miles-per-gallon", "-m", "--kilometres-per-litre", "-k", "--mpg",
      "--kpl"}, converter = BigDecimalConverter.class, descriptionKey = "mileage.distance-per-volume")
  BigDecimal mileageDistancePerVolume;

  @Parameter(names = {"--litres-per-ton-kilometres", "-l", "--gallons-per-ton-miles",
      "-g"}, converter = BigDecimalConverter.class, descriptionKey = "mileage.volume-per-distance")
  BigDecimal mileageVolumePerDistance;

  @Parameter(names = "-0", descriptionKey = "round.zero")
  boolean zeroDigits;

  @Parameter(names = "-2", descriptionKey = "round.two")
  boolean twoDigits;

  @Parameter(names = "-3", descriptionKey = "round.three")
  boolean threeDigits;

  @Parameter(names = "-4", descriptionKey = "round.four")
  boolean fourDigits;

  @Parameter(names = {"--help", "-h"}, descriptionKey = "help", help = true)
  boolean usage;

}
