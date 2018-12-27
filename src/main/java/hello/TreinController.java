package hello;

import com.basdado.trainfinder.ns.communicator.NSCommunicator;
import com.basdado.trainfinder.ns.communicator.NSCommunicatorConfiguration;
import com.basdado.trainfinder.ns.exception.NSException;
import com.basdado.trainfinder.ns.model.Departure;
import com.basdado.trainfinder.ns.model.DepartureInfoResponse;
import com.basdado.trainfinder.ns.model.Station;
import com.basdado.trainfinder.ns.model.StationInfoResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@Controller
@Configuration
public class TreinController {

    protected final Log logger = LogFactory.getLog(this.getClass());

    NSCommunicator nsCommunicator = null;

    @GetMapping("/trein")
    public String trein(HttpServletResponse response,
                          @RequestParam(name="naam", required=false, defaultValue="Deelstromer") String naam,
                          @RequestParam(name="stroomdelen", required=false, defaultValue="0") Integer stroomdelen,
                          Model model,
                          @Value("${ns.username}") String username,
                          @Value("${ns.password}") String password) throws IOException {
        DepartureInfoResponse delftResponse;
        DepartureInfoResponse rijswijkResponse;
        DepartureInfoResponse denhaagHSResponse;
        try {
//            StationInfoResponse stations = produceNSCommunicator(username, password).getStations();
//            for (Station stat : stations.getStations()) {
//                logger.error(stat.getStationNames().getShortName()+ " " + stat.getStationNames().getMediumName());
//            }
            delftResponse = produceNSCommunicator(username, password).getDepartures("delft");
            rijswijkResponse = produceNSCommunicator(username, password).getDepartures("rijswijk");
            denhaagHSResponse = produceNSCommunicator(username, password).getDepartures("Den%20Haag%20HS");

            Departure beste = null;
            for (Departure trein : delftResponse.getDepartures()) {
                beste = selecteerBeste(beste, trein);
            }
            for (Departure trein : rijswijkResponse.getDepartures()) {
                beste = selecteerBeste(beste, trein);
            }
            for (Departure trein : denhaagHSResponse.getDepartures()) {
                beste = selecteerBeste(beste, trein);
            }
            logger.info("gekozen "+beste.getRouteText()+" "+beste.getDepartureTime());
            model.addAttribute("trein", beste);
        } catch (NSException e) {
            logger.error("NSException while trying to load departure times: " + e.getMessage(), e);
        }

        return "trein";
    }

    private Departure selecteerBeste(Departure huidige, Departure kandidaat) {
        logger.info("test "+kandidaat.getRouteText()+" "+kandidaat.getDepartureTime());
        if (huidige == null) return kandidaat;
        if ((huidige.getRouteText().indexOf("Delft")<0)  && (kandidaat.getRouteText().indexOf("Delft")>=0))
            return kandidaat;
        return huidige;
    }

    private NSCommunicator produceNSCommunicator(String username, String pw) {

        if (nsCommunicator == null) {
            NSCommunicatorConfiguration nsCommunicatorConfig = new NSCommunicatorConfiguration() {

                @Override
                public String getUsername() {
                    return username;
                }

                @Override
                public String getPassword() {
                    return pw;
                }
            };
            nsCommunicator = new NSCommunicator(nsCommunicatorConfig);
        }
        return nsCommunicator;

    }


}
