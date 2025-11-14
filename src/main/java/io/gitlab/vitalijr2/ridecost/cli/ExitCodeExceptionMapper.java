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

import picocli.CommandLine.ExitCode;
import picocli.CommandLine.IExitCodeExceptionMapper;
import picocli.CommandLine.ParameterException;

public class ExitCodeExceptionMapper implements IExitCodeExceptionMapper {

  @Override
  public int getExitCode(Throwable throwable) {
    if (throwable instanceof ParameterException) {
      return ExitCode.USAGE;
    }
    return ExitCode.SOFTWARE;
  }

}
