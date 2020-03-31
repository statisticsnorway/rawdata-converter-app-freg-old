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
    private byte[] hendelseXml;
    private byte[] personXml;
    private byte[] manifestJson;

    public static FregItem from(RawdataMessage rawdataMessage) {
        return FregItem.builder()
                .ulid(rawdataMessage.ulid())
                .position(rawdataMessage.position())
                .hendelseXml(rawdataMessage.get("event"))
                .personXml(rawdataMessage.get("person"))
                .manifestJson(rawdataMessage.get("manifest.json"))
                .build();
    }

    public boolean hasManifestJson() {
        return manifestJson != null;
    }

    public String getManifestJsonAsString() {
        return hasManifestJson() ? new String(manifestJson) : null;
    }

    public boolean hasHendelse() {
        return hendelseXml != null;
    }

    public String getHendelseXmlAsString() {
        return hasHendelse() ? new String(hendelseXml) : null;
    }

    public boolean hasPerson() {
        return personXml != null;
    }

    public String getPersonXmlAsString() {
        return hasPerson() ? new String(personXml) : null;
    }

    public String toIdString() {
        return String.format("%s (pos=%s)", ulid, position);
    }
}
