package hello.ns;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VertrekkendeTrein {
    @JsonProperty("RitNummer")
    public String RitNummer;

    @JsonProperty("VertrekTijd")
    public String VertrekTijd;

    @JsonProperty("VertrekVertraging")
    public String VertrekVertraging;

    @JsonProperty("VertrekVertragingTekst")
    public String VertrekVertragingTekst;

    @JsonProperty("EindBestemming")
    public String EindBestemming;

    @JsonProperty("TreinSoort")
    public String TreinSoort;

    @JsonProperty("RouteTekst")
    public String RouteTekst;

    @JsonProperty("Vervoerder")
    public String Vervoerder;

    @JsonProperty("VertrekSpoor")
    public String VertrekSpoor;
}
