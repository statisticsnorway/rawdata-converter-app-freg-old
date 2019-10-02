package no.ssb.rawdata.converter.app.freg;

import de.huxhorn.sulky.ulid.ULID;
import lombok.Builder;
import lombok.Value;
import no.ssb.rawdata.api.RawdataMessage;

import java.util.TreeSet;

@Builder
@Value
public class FregItem {
    private final ULID.Value ulid;
    private final String position;
    private String personId;
    private byte[] eventXml;
    private byte[] personXml;

    // TODO: Include info from the metadata section instead
    public static FregItem from(RawdataMessage rawdataMessage) {
        TreeSet<String> keys = new TreeSet(rawdataMessage.keys());

        String eventDocKey = keys.ceiling("event-document");
        String personDocKey = keys.ceiling("person-document");
        String personId = null;
        if (personDocKey != null) {
            personId = personDocKey.replace("person-document-", "").replace(".xml", "");
        }

        return FregItem.builder()
          .ulid(rawdataMessage.ulid())
          .position(rawdataMessage.position())
          .personId(personId)
          .eventXml(rawdataMessage.get(eventDocKey))
          .personXml(rawdataMessage.get(personDocKey))
          .build();
    }

    public boolean hasPerson() {
        return personXml != null;
    }

    public boolean hasEvent() {
        return eventXml != null;
    }

    public String toIdString() {
        return String.format("%s (pos=%s, person=%s)", ulid, position, personId);
    }
}
