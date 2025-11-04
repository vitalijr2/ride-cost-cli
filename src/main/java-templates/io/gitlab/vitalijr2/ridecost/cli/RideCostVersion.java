package io.gitlab.vitalijr2.ridecost.cli;

import picocli.CommandLine.IVersionProvider;

public class RideCostVersion implements IVersionProvider {

  @Override
  public String[] getVersion() throws Exception {
    return new String[]{"${COMMAND-FULL-NAME} @project.version@"};
  }

}
