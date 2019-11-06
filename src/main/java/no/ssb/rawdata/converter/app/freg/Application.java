package no.ssb.rawdata.converter.app.freg;

import io.micronaut.runtime.Micronaut;
import lombok.extern.slf4j.Slf4j;
import no.ssb.rawdata.converter.core.util.EnvrionmentVariables;

@Slf4j
public class Application {

    public static void main(String[] args) {

        try {
            int i = 1 / 0;
        } catch (Exception e) {
            log.warn("Failed", new RuntimeException("Couldn't divide by zero", e));
        }

        Micronaut.build(null)
          .mainClass(Application.class)
          .environmentVariableIncludes(EnvrionmentVariables.withPrefix("RAWDATA_CLIENT").toArray(new String[0]))
          .start();
    }

}