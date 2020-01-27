package hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Controller
public class StroomdeelController {

    protected final Log logger = LogFactory.getLog(this.getClass());

    HttpClientBuilder clientbuilder = HttpClientBuilder.create();
    HttpHost target = new HttpHost("monitoringapi.solaredge.com", 443, "https");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    String siteid = "715131";
    String api_key = "LNEPU21MZLCJSPTEDWPXAQZ09SMFXL8D";
    String siteid_polletjes = "564256";
    String api_key_polletjes = "NCFWGOTDJ225818AQJTFFT1W137XMCQO";

    Integer totaalstroomdelen = 912;

    @GetMapping("/stroomdeel")
    public String greeting(@RequestParam(name="naam", required=false, defaultValue="Deelstromer") String naam,
                           @RequestParam(name="stroomdelen", required=false, defaultValue="0") Integer stroomdelen,
                           Model model) {
        try {
            readSolarEdge(model, naam, stroomdelen);
            if (stroomdelen >0) {
                return "stroomdeel";
            } else {
                return "stroomdeel-algemeen";
            }
        } catch (Exception e) {
            logger.error("", e);
            return "fout";
        }
    }

    @GetMapping("/counter2")
    public String counter(HttpServletResponse response,
                                    @RequestParam(name="naam", required=false, defaultValue="Deelstromer") String naam,
                                    @RequestParam(name="stroomdelen", required=false, defaultValue="0") Integer stroomdelen,
                                    Model model) throws IOException {
        SolarEdgeData data = readSolarEdge();
        Double totaalkwh = getTotalkWh(data);

        model.addAttribute("totaalkwh", String.format( "%.0f", totaalkwh));

        return "flipclock";
    }

    @GetMapping("/counterpolletjes")
    public String counterpolletjes(HttpServletResponse response,
                          @RequestParam(name="naam", required=false, defaultValue="Deelstromer") String naam,
                          @RequestParam(name="stroomdelen", required=false, defaultValue="0") Integer stroomdelen,
                          Model model) throws IOException {
        SolarEdgeData data = readSolarEdgePolletjes();
        Double totaalkwh = getTotalkWh(data);

        model.addAttribute("totaalkwh", String.format( "%.0f", totaalkwh));

        return "flipclock";
    }

    @GetMapping("/counter")
    public void getImageAsByteArray(HttpServletResponse response,
                                    @RequestParam(name="naam", required=false, defaultValue="Deelstromer") String naam,
                                    @RequestParam(name="stroomdelen", required=false, defaultValue="0") Integer stroomdelen,
                                    Model model) throws IOException {
        SolarEdgeData data = readSolarEdge();
        Double totaalkwh = getTotalkWh(data);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        Text2Jpg.writeJpg(String.format( "%.0f", totaalkwh)+ " kwh", response.getOutputStream());
    }

    private Double getTotalkWh(SolarEdgeData data) {
        int factor = 1;
        switch (data.timeFrameEnergy.endLifetimeEnergy.unit) {
            case "Wh":
                factor = 1000;
                break;
            default:
        }
        return data.timeFrameEnergy.endLifetimeEnergy.energy / factor;
    }

    private void readSolarEdge(Model model, String naam, Integer stroomdelen) {

        String startDate = getCurrentDateString(0);
        String endDate = getCurrentDateString(1);
        HttpGet getRequest = new HttpGet("/site/"+siteid+"/timeFrameEnergy.json?startDate="+startDate+"&endDate="+endDate+"&api_key="+api_key);

        model.addAttribute("naam", naam);
        model.addAttribute("stroomdelen", stroomdelen);
        model.addAttribute("totaalstroomdelen", totaalstroomdelen);
        try {
            SolarEdgeData data = readSolarEdge();
            Double totaalkwh = getTotalkWh(data);
            Double kwh = (data.timeFrameEnergy.energy/1000/totaalstroomdelen);

            model.addAttribute("unit", "kWh");
            model.addAttribute("kwh", String.format( "%.2f", kwh ));
            model.addAttribute("totaalkwh", String.format( "%.2f", totaalkwh));
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("opbrengst", String.format( "%.0f", (stroomdelen*kwh)));
            logger.info(model.toString());

        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private String getCurrentDateString(int add) {
        LocalDate tomorrow = LocalDate.now().plusDays(add);
        return tomorrow.format(formatter);
    }

    private SolarEdgeData readSolarEdge() {
        return _readSolarEdge(siteid, api_key);
    }

    private SolarEdgeData readSolarEdgePolletjes() {
        return _readSolarEdge(siteid_polletjes, api_key_polletjes);
    }
    private SolarEdgeData _readSolarEdge(String siteid, String api_key) {

        String startDate = getCurrentDateString(0);
        String endDate = getCurrentDateString(1);
        HttpGet getRequest = new HttpGet("/site/"+siteid+"/timeFrameEnergy.json?startDate="+startDate+"&endDate="+endDate+"&api_key="+api_key);

        try (CloseableHttpClient httpclient = clientbuilder.build()) {
            HttpResponse httpResponse = httpclient.execute(target, getRequest);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String json = EntityUtils.toString(entity);
                ObjectMapper m = new ObjectMapper();
                SolarEdgeData data = m.readValue(json, SolarEdgeData.class);
                logger.info(data.toString());
                return data;
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

}
