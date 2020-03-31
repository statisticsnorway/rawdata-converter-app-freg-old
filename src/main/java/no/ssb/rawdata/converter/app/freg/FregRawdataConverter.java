package no.ssb.rawdata.converter.app.freg;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import no.ssb.avro.convert.xml.XmlToRecords;
import no.ssb.rawdata.api.RawdataMessage;
import no.ssb.rawdata.converter.core.AbstractRawdataConverter;
import no.ssb.rawdata.converter.core.AggregateSchemaBuilder;
import no.ssb.rawdata.converter.core.ConversionResult;
import no.ssb.rawdata.converter.core.ConversionResult.ConversionResultBuilder;
import no.ssb.rawdata.converter.core.Metadata;
import no.ssb.rawdata.converter.core.MetadataGenericRecordBuilder;
import no.ssb.rawdata.converter.core.RawdataMessageException;
import no.ssb.rawdata.converter.core.pseudo.PseudoService;
import no.ssb.rawdata.converter.core.util.RawdataMessageUtil;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;

import javax.inject.Singleton;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Singleton
@Slf4j
public class FregRawdataConverter extends AbstractRawdataConverter {

    private final Schema personSchema;
    private final Schema hendelseSchema;
    private final Schema aggregateSchema;

    private final PseudoService pseudoService;

    private static final String RAWDATA_ENTRY_PERSON = "person";
    private static final String RAWDATA_ENTRY_HENDELSE = "entry";

    private static final String ELEMENT_NAME_METADATA = "metadata";
    private static final String ELEMENT_NAME_FREG_PERSON = "folkeregisterperson";
    private static final String ELEMENT_NAME_FREG_HENDELSE = "dokumentForHendelse";

    public FregRawdataConverter(FregRawdataConverterConfig converterConfig, @NonNull PseudoService pseudoService) {
        personSchema = readAvroSchema(converterConfig.getSchemaFilePerson());
        hendelseSchema = readAvroSchema(converterConfig.getSchemaFileHendelse());

        aggregateSchema = new AggregateSchemaBuilder("no.ssb.dataset")
                .schema(ELEMENT_NAME_METADATA, Metadata.SCHEMA)
                .schema(ELEMENT_NAME_FREG_HENDELSE, hendelseSchema)
                .schema(ELEMENT_NAME_FREG_PERSON, personSchema)
                .build();

        this.pseudoService = pseudoService;
        log.info("converter config:\n" + converterConfig.toDebugString());
    }

    @Override
    public Schema targetAvroSchema() {
        return aggregateSchema;
    }

    @Override
    public boolean isConvertible(RawdataMessage msg) {
        try {
            RawdataMessageUtil.assertKeysPresent(msg, RAWDATA_ENTRY_PERSON, RAWDATA_ENTRY_HENDELSE);
        }
        catch (RawdataMessageException e) {
            log.warn(e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public ConversionResult convert(RawdataMessage rawdataMessage) {
        log.trace("convert freg rawdata message {}", rawdataMessage);

        ConversionResultBuilder resultBuilder = new ConversionResultBuilder(new GenericRecordBuilder(aggregateSchema));
        FregItem fregItem = FregItem.from(rawdataMessage);

        if (fregItem.hasManifestJson()) {
            GenericRecord metadataRecord = MetadataGenericRecordBuilder
              .fromRawdataManifest(fregItem.getManifestJson())
              .withULID(fregItem.getUlid())
              .build();
            resultBuilder.withRecord(ELEMENT_NAME_METADATA, metadataRecord);

        }

        if (fregItem.hasHendelse()) {
            try {
                xmlToAvro(fregItem.getHendelseXml(), ELEMENT_NAME_FREG_HENDELSE, hendelseSchema, resultBuilder);
            } catch (Exception e) {
                resultBuilder.addFailure(e);
                log.warn("Failed to convert hendelse xml", e);
            }
        }

        if (fregItem.hasPerson()) {
            try {
                xmlToAvro(fregItem.getPersonXml(), ELEMENT_NAME_FREG_PERSON, personSchema, resultBuilder);
            } catch (Exception e) {
                resultBuilder.addFailure(e);
                log.warn("Failed to convert person xml", e);
            }

        }

        return resultBuilder.build();
    }

    void xmlToAvro(byte[] xmlData, String rootXmlElementName, Schema schema, ConversionResultBuilder resultBuilder) {
        InputStream xmlInputStream = new ByteArrayInputStream(xmlData);

        try (XmlToRecords xmlToRecords = new XmlToRecords(xmlInputStream, rootXmlElementName, schema, pseudoService::pseudonyimze)) {
            xmlToRecords.forEach(record ->
              resultBuilder.withRecord(rootXmlElementName, record)
            );
        } catch (XMLStreamException | IOException e) {
            throw new FregRawdataConverterException("Error converting freg XML", e);
        }
    }

    static class FregRawdataConverterException extends RuntimeException {
        FregRawdataConverterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
