package hello.ns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class ActueleVertrekTijden {

    @JsonProperty("VertrekkendeTrein")
    @JacksonXmlElementWrapper(useWrapping = false)
    public VertrekkendeTrein[] vertrekkendeTrein;

}
