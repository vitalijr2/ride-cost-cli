module ride.cost.cli {
  requires org.jetbrains.annotations;
  requires ride.cost.estimator;
  requires info.picocli;
  opens io.gitlab.vitalijr2.ridecost.cli to info.picocli;
}