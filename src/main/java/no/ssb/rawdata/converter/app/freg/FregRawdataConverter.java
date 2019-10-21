package no.ssb.rawdata.converter.app.freg;

import lombok.extern.slf4j.Slf4j;
import no.ssb.avro.convert.xml.XmlToRecords;
import no.ssb.rawdata.api.RawdataMessage;
import no.ssb.rawdata.converter.core.AbstractRawdataConverter;
import no.ssb.rawdata.converter.core.AggregateSchemaBuilder;
import no.ssb.rawdata.converter.core.ConversionResult;
import no.ssb.rawdata.converter.core.ConversionResult.ConversionResultBuilder;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecordBuilder;

import javax.inject.Singleton;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Singleton
@Slf4j
public class FregRawdataConverter extends AbstractRawdataConverter {

    private final Schema fregPersonSchema;
    private final Schema fregEventSchema;
    private final Schema aggregateSchema;

    private static final String ELEMENT_NAME_FREG_PERSON = "folkeregisterperson";
    private static final String ELEMENT_NAME_FREG_EVENT = "dokumentForHendelse";

    private static final String DEFAULT_SCHEMA_FILE_PERSON = "schema/freg-person.avsc";
    private static final String DEFAULT_SCHEMA_FILE_EVENT = "schema/freg-event.avsc";

    public FregRawdataConverter(FregRawdataConverterConfig fregConverterConfig) {
        fregPersonSchema = readAvroSchema(fregConverterConfig.getSchemaFilePerson(), DEFAULT_SCHEMA_FILE_PERSON);
        fregEventSchema = readAvroSchema(fregConverterConfig.getSchemaFileEvent(), DEFAULT_SCHEMA_FILE_EVENT);
        aggregateSchema = new AggregateSchemaBuilder("no.ssb.dataset")
                .schema(ELEMENT_NAME_FREG_PERSON, fregPersonSchema)
                .schema(ELEMENT_NAME_FREG_EVENT, fregEventSchema)
                .build();
    }

    @Override
    public Schema targetAvroSchema() {
        return aggregateSchema;
    }

    @Override
    public ConversionResult convert(RawdataMessage rawdataMessage) {
        log.trace("convert no.ssb.rawdata message {}", rawdataMessage);

        ConversionResultBuilder resultBuilder = new ConversionResultBuilder(new GenericRecordBuilder(aggregateSchema));

        FregItem fregItem = FregItem.from(rawdataMessage);

        if (fregItem.hasPerson()) {
            try {
                xmlToAvro(fregItem.getPersonXml(), ELEMENT_NAME_FREG_PERSON, fregPersonSchema, resultBuilder);
            } catch (Exception e) {
                resultBuilder.addFailure(e);
                log.warn("Failed to convert person xml", e);
            }
        } else {
            log.error("Missing person data for freg item {}", fregItem.toIdString());
        }

        if (fregItem.hasEvent()) {
            try {
                xmlToAvro(fregItem.getEventXml(), ELEMENT_NAME_FREG_EVENT, fregEventSchema, resultBuilder);
            } catch (Exception e) {
                resultBuilder.addFailure(e);
                log.warn("Failed to convert event xml", e);
            }
        } else {
            log.error("Missing event data for freg item {}", fregItem.toIdString());
        }

        return resultBuilder.build();
    }

    private void xmlToAvro(byte[] xmlData, String rootXmlElementName, Schema schema, ConversionResultBuilder resultBuilder) {
        InputStream xmlInputStream = new ByteArrayInputStream(xmlData);
        try (XmlToRecords xmlToRecords = new XmlToRecords(xmlInputStream, rootXmlElementName, schema)) {
            xmlToRecords.forEach(record -> resultBuilder.withRecord(rootXmlElementName, record));
        } catch (XMLStreamException | IOException e) {
            throw new FregRawdataConverterException("Could not convert Freg XML", e);
        }
    }

    static class FregRawdataConverterException extends RuntimeException {
        FregRawdataConverterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
