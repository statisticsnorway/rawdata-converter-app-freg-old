package no.ssb.rawdata.converter.app.freg;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import no.ssb.rawdata.api.RawdataClient;
import no.ssb.rawdata.api.RawdataClientInitializer;
import no.ssb.rawdata.api.RawdataProducer;
import no.ssb.service.provider.api.ProviderConfigurator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FregPublisherTest {

    /** Can be used to populate the rawdata storage with some test data. */
    @Test
    @Disabled @SuppressWarnings({"squid:S1607", "squid:S2699"})
    public void publishLocalFregData() throws Exception {
        DynamicConfiguration configuration = configuration();
        String rawdataFregFiles = configuration.evaluateToString("test.rawdata.freg.files");
        RawdataClient client = createRawdataClient(configuration);
        RawdataProducer producer = client.producer("freg");
        List<String> positions = new ArrayList<>();

        Files.list(Paths.get(rawdataFregFiles)).sorted().filter(Files::isDirectory).forEach(path -> {
            String position = path.getFileName().toString();
            positions.add(position);
            try {
                producer.buffer(producer.builder()
                  .put("event-document", Files.readAllBytes(findFile(path, "event-document")))
                  .put("person-document", Files.readAllBytes(findFile(path, "person-document")))
                  .position(position));
            }
            catch (IOException e) {
                throw new RuntimeException("Error buffering rawdata position " + position);
            }
        });

        producer.publish(positions);
        System.out.println(String.format("Published %d rawdata messages to '%s' topic using %s provider",
          positions.size(),
          producer.topic(),
          configuration.evaluateToString("rawdata.client.provider")));
    }

    static Path findFile(Path path, String filePrefix) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            return walk
              .filter(p -> p.getFileName().toString().startsWith(filePrefix))
              .findFirst()
              .orElseThrow(FileNotFoundException::new);
        }
    }

    DynamicConfiguration configuration() {
        return new StoreBasedDynamicConfiguration.Builder()
          .propertiesResource("application-test.properties")
          .build();
    }

    RawdataClient createRawdataClient(DynamicConfiguration configuration) {
        return ProviderConfigurator.configure(configuration.asMap(), configuration.evaluateToString("rawdata.client.provider"), RawdataClientInitializer.class);
    }

}