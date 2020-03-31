package no.ssb.rawdata.converter.app.freg;

import com.google.common.base.Strings;
import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties("rawdata.converter.app.freg")
@Data
public class FregRawdataConverterConfig {

    @NotEmpty
    private String schemaFilePerson;

    @NotEmpty
    private String schemaFileHendelse;

    public String toDebugString() {
        return debugItem("schema person", schemaFilePerson)
          + debugItem("schema hendelse", schemaFileHendelse);
    }

    private String debugItem(String label, Object value) {
        return Strings.padEnd(label, 24, '.') + " " + value + "\n";
    }

}
