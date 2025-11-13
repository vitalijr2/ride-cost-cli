package io.gitlab.vitalijr2.ridecost.cli;

import picocli.CommandLine.ExitCode;
import picocli.CommandLine.IExitCodeExceptionMapper;
import picocli.CommandLine.ParameterException;

public class RideCostExitCodeExceptionMapper implements IExitCodeExceptionMapper {

  @Override
  public int getExitCode(Throwable throwable) {
    if (throwable instanceof ParameterException) {
      return ExitCode.USAGE;
    }
    return ExitCode.SOFTWARE;
  }

}
