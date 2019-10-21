package no.ssb.rawdata.converter.app.freg;

import io.micronaut.runtime.Micronaut;
import no.ssb.rawdata.converter.core.util.EnvrionmentVariables;

public class Application {

    public static void main(String[] args) {
        Micronaut.build(null)
          .mainClass(Application.class)
          .environmentVariableIncludes(EnvrionmentVariables.withPrefix("RAWDATA_CLIENT").toArray(new String[0]))
          .start();
    }

}