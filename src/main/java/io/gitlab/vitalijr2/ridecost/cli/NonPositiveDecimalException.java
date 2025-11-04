package io.gitlab.vitalijr2.ridecost.cli;

import java.io.Serial;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

public class NonPositiveDecimalException extends ParameterException {

  @Serial
  private static final long serialVersionUID = 9042814604700414593L;

  public NonPositiveDecimalException(CommandLine commandLine, String msg) {
    super(commandLine, msg);
  }

}
