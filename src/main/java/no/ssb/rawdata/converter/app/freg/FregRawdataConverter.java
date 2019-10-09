package no.ssb.rawdata.converter.app.freg;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.ssb.rawdata.api.RawdataMessage;
import no.ssb.rawdata.converter.core.AggregateSchemaBuilder;
import no.ssb.rawdata.converter.core.RawdataConverter;
import no.ssb.transform.xmlparquet.RecursiveXmlParser;
import no.ssb.transfrom.parquet.SchemaAwareElement;
import no.ssb.transfrom.parquet.SchemaWrapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
@Slf4j
public class FregRawdataConverter implements RawdataConverter {
    private final Schema fregPersonSchema;
    private final Schema fregEventSchema;
    private final Schema aggregateSchema;

    private static final String ELEMENT_NAME_FREG_PERSON = "folkeregisterperson";
    private static final String ELEMENT_NAME_FREG_EVENT = "dokumentForHendelse";

    private static final String DEFAULT_SCHEMA_FILE_PERSON = "schema/freg-person.avsc";
    private static final String DEFAULT_SCHEMA_FILE_EVENT = "schema/freg-event.avsc";

    public FregRawdataConverter(FregRawdataConverterConfig fregConverterConfig) {

        try {
            fregPersonSchema = new Schema.Parser().parse(loadSchema(fregConverterConfig.getSchemaFilePerson(), DEFAULT_SCHEMA_FILE_PERSON));
            fregEventSchema = new Schema.Parser().parse(loadSchema(fregConverterConfig.getSchemaFileEvent(), DEFAULT_SCHEMA_FILE_EVENT));
            aggregateSchema = new AggregateSchemaBuilder("no.ssb.dataset")
              .schema(ELEMENT_NAME_FREG_PERSON, fregPersonSchema)
              .schema(ELEMENT_NAME_FREG_EVENT, fregEventSchema)
              .build();
        }
        catch (IOException e) {
            throw new FregRawdataConverterException("Unable to locate avro schema. " + e.getMessage());
        }
    }

    @Override
    public Schema targetAvroSchema() {
        return aggregateSchema;
    }

    @Override
    public GenericRecord convert(RawdataMessage rawdataMessage) {
        log.trace("convert no.ssb.rawdata message {}", rawdataMessage);
        GenericRecordBuilder rootRecordBuilder = new GenericRecordBuilder(aggregateSchema);
        FregItem fregItem = FregItem.from(rawdataMessage);
        if (fregItem.hasPerson()) {
            xmlToAvro(fregPersonSchema, ELEMENT_NAME_FREG_PERSON, fregItem.getPersonXml()).forEach(record ->
                rootRecordBuilder.set(ELEMENT_NAME_FREG_PERSON, record)
            );
        }
        else {
            log.info("Missing person data for freg item {}", fregItem.toIdString());
        }

        if (fregItem.hasEvent()) {
            xmlToAvro(fregEventSchema, ELEMENT_NAME_FREG_EVENT, fregItem.getEventXml()).forEach(record ->
                rootRecordBuilder.set(ELEMENT_NAME_FREG_EVENT, record)
            );
        }
        else {
            log.info("Missing event data for freg item {}", fregItem.toIdString());
        }

        return rootRecordBuilder.build();
    }

    List<GenericRecord> xmlToAvro(Schema schema, String rootXmlElementName, byte [] xmlData) {
        SchemaWrapper schemaWrapper = SchemaWrapper.parse(schema);
        try (RecursiveXmlParser recursiveXmlParser = new RecursiveXmlParser(new ByteArrayInputStream(xmlData), rootXmlElementName)) {
            return StreamSupport.stream(recursiveXmlParser.spliterator(), false)
              .map(dataElement -> SchemaAwareElement.toRecord(dataElement, schemaWrapper))
              .collect(Collectors.toList());
        } catch (Exception e) {
            throw new FregRawdataConverterException("Error converting XML to Avro", e);
        }
    }

    @SneakyThrows
    private InputStream loadSchema(Optional<String> schemaFile, String defaultSchemaFile) {
        try {
            if (schemaFile.isPresent()) {
                return new FileInputStream(Paths.get(schemaFile.get()).toAbsolutePath().normalize().toFile());
            }
            else {
                return getClass().getClassLoader().getResourceAsStream(defaultSchemaFile);
            }
        }
        catch (FileNotFoundException e) {
            throw new FregRawdataConverterException("Unable to locate avro schema: " + e.getMessage());
        }
    }

    static class FregRawdataConverterException extends RuntimeException {
        public FregRawdataConverterException(String message) {
            super(message);
        }

        public FregRawdataConverterException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
