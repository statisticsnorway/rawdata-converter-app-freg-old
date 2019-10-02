package no.ssb.rawdata.converter.app.freg;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@ConfigurationProperties("rawdata.converter.app.freg")
@Value
@Slf4j
public class FregRawdataConverterConfig {

    private Optional<String> schemaFilePerson = Optional.empty();

    private Optional<String> schemaFileEvent = Optional.empty();

}
