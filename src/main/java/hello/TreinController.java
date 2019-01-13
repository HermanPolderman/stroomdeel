package hello;

import com.basdado.trainfinder.ns.communicator.NSCommunicator;
import com.basdado.trainfinder.ns.communicator.NSCommunicatorConfiguration;
import com.basdado.trainfinder.ns.exception.NSException;
import com.basdado.trainfinder.ns.model.Departure;
import com.basdado.trainfinder.ns.model.DepartureInfoResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

@Controller
@Configuration
public class TreinController {

    public static final String DELFT = "delft";
    public static final String RIJSW = "rijswijk";
    public static final String DHHS = "Den%20Haag%20HS";

    private final Log logger = LogFactory.getLog(this.getClass());
    private NSCommunicator nsCommunicator = null;
    private static SortedMap<String, Departure> treinenOndermolen = new TreeMap<String, Departure>();

    @Value("${ns.username}")
    private String NSusername;
    @Value("${ns.password}")
    private String NSpw;

    @GetMapping(value = "/trein.txt", produces = "text/plain")
    @ResponseBody
    public String treintxt() throws IOException {
        Departure beste = getBestDeparture();
        return "spoor "+beste.getTrack()+" "+ beste.getOrigin()+" "+ beste.getDestination()+" "+beste.getDepartureTime();
    }

    @GetMapping("/trein")
    public String trein(Model model) throws IOException {

        Departure beste = getBestDeparture();

        model.addAttribute("trein", beste);

        return "trein";
    }

    private Departure getBestDeparture() {



        addDepartures(DELFT);
        addDepartures(RIJSW);
        addDepartures(DHHS);
        cleanDepartures();

        showdepartures();

        return treinenOndermolen.get(treinenOndermolen.firstKey());
    }

    private void cleanDepartures() {
        OffsetDateTime nu = OffsetDateTime.now().minus(0, ChronoUnit.MINUTES);

        Iterator<String> it = treinenOndermolen.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Departure trein = treinenOndermolen.get(key);
            if (trein.getDepartureTime().isBefore(nu)) {
                logger.info("verwijder " + key + " " + trein.getRideNumber() + " -> " + trein.getRouteText() + " naar " + trein.getDestination());
                it.remove();
            }
        }
    }

    private void showdepartures() {
        for (String key: treinenOndermolen.keySet()) {
            Departure trein = treinenOndermolen.get(key);
            logger.info(key+" "+trein.getRideNumber()+" -> "+trein.getRouteText()+" naar "+trein.getDestination());
        }
    }

    private void addDepartures(String station) {
        DepartureInfoResponse response;
        try {
            response = produceNSCommunicator().getDepartures(station);
            for (Departure vertrek : response.getDepartures()) {
                if (komtOnderMolen(station, vertrek)) {
                    vertrek.setOrigin(station);
                    String key = vertrek.getDepartureTime().format(DateTimeFormatter.ISO_DATE_TIME)+" "+station;
                    treinenOndermolen.put(key, vertrek);
                }
            }
        } catch (NSException e) {
            logger.error("NSException while trying to load departure times: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Exception while trying to load departure times: " + e.getMessage(), e);
        }
    }

    private boolean komtOnderMolen(String station, Departure kandidaat) {
        if (kandidaat == null) {
            return false;
        }
        if (kandidaat.getRouteText() == null) {
            return false;
        }
        if (DELFT.equals(station)) {
            if (kandidaat.getTrack().equals("1")) {
                return true;
            }
        } else {
            if (kandidaat.getRouteText().toLowerCase().contains("delft")) {
                return true;
            }
        }
        return false;
    }

    private NSCommunicator produceNSCommunicator() {

        if (nsCommunicator == null) {
            NSCommunicatorConfiguration nsCommunicatorConfig = new NSCommunicatorConfiguration() {

                @Override
                public String getUsername() {
                    return NSusername;
                }

                @Override
                public String getPassword() {
                    return NSpw;
                }
            };
            nsCommunicator = new NSCommunicator(nsCommunicatorConfig);
        }
        return nsCommunicator;
    }

}
