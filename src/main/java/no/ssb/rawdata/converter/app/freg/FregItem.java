package no.ssb.rawdata.converter.app.freg;

import de.huxhorn.sulky.ulid.ULID;
import lombok.Builder;
import lombok.Value;
import no.ssb.rawdata.api.RawdataMessage;

@Builder
@Value
public class FregItem {
    private final ULID.Value ulid;
    private final String position;
    private byte[] eventXml;
    private byte[] personXml;
    private byte[] metadataJson;

    public static FregItem from(RawdataMessage rawdataMessage) {
        return FregItem.builder()
                .ulid(rawdataMessage.ulid())
                .position(rawdataMessage.position())
                .eventXml(rawdataMessage.get("event"))
                .personXml(rawdataMessage.get("person"))
                .metadataJson(rawdataMessage.get("manifest.json"))
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
