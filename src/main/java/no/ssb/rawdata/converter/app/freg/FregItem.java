package no.ssb.rawdata.converter.app.freg;

import de.huxhorn.sulky.ulid.ULID;
import lombok.Builder;
import lombok.Value;
import no.ssb.rawdata.api.RawdataMessage;

import java.util.function.Function;

@Builder
@Value
public class FregItem {
    private final ULID.Value ulid;
    private final String position;
    private byte[] eventXml;
    private byte[] personXml;
    private byte[] metadataJson;

    public static FregItem from(RawdataMessage rawdataMessage, Function<byte[], byte[]> tryDecryptContent) {
        return FregItem.builder()
                .ulid(rawdataMessage.ulid())
                .position(rawdataMessage.position())
                .eventXml(tryDecryptContent.apply(rawdataMessage.get("event")))
                .personXml(tryDecryptContent.apply(rawdataMessage.get("person")))
                .metadataJson(tryDecryptContent.apply(rawdataMessage.get("manifest.json")))
                .build();
    }

    public boolean hasPerson() {
        return personXml != null;
    }

    public boolean hasEvent() {
        return eventXml != null;
    }

    public String toIdString() {
        return String.format("%s (pos=%s)", ulid, position);
    }
}
