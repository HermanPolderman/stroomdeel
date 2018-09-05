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
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class StroomdeelController {

    protected final Log logger = LogFactory.getLog(this.getClass());

    HttpClientBuilder clientbuilder = HttpClientBuilder.create();
    HttpHost target = new HttpHost("monitoringapi.solaredge.com", 443, "https");
    String startDate = "2018-06-01";

    String siteid = "715131";
    String api_key = "LNEPU21MZLCJSPTEDWPXAQZ09SMFXL8D";
    String siteidpolletjes = "564256";
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
        int factor =1;
        switch (data.timeFrameEnergy.unit) {
            case "Wh":
                factor = 1000;
                break;
            default:
        }
        Double totaalkwh = (data.timeFrameEnergy.energy/factor);
        model.addAttribute("totaalkwh", String.format( "%.0f", totaalkwh));

        return "flipclock";
    }

    @GetMapping("/counter")
    public void getImageAsByteArray(HttpServletResponse response,
                                    @RequestParam(name="naam", required=false, defaultValue="Deelstromer") String naam,
                                    @RequestParam(name="stroomdelen", required=false, defaultValue="0") Integer stroomdelen,
                                    Model model) throws IOException {
        SolarEdgeData data = readSolarEdge();
        int factor =1;
        switch (data.timeFrameEnergy.unit) {
            case "Wh":
                factor = 1000;
                break;
            default:
        }
        Double totaalkwh = (data.timeFrameEnergy.energy/factor);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        Text2Jpg.writeJpg(String.format( "%.0f", totaalkwh)+ " kwh", response.getOutputStream());
    }

    private void readSolarEdge(Model model, String naam, Integer stroomdelen) {

        String endDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        HttpGet getRequest = new HttpGet("/site/"+siteid+"/timeFrameEnergy.json?startDate="+startDate+"&endDate="+endDate+"&api_key="+api_key);

        model.addAttribute("naam", naam);
        model.addAttribute("stroomdelen", stroomdelen);
        model.addAttribute("totaalstroomdelen", totaalstroomdelen);
        try {
            SolarEdgeData data = readSolarEdge();
            int factor =1;
            switch (data.timeFrameEnergy.unit) {
                case "Wh":
                    factor = 1000;
                    break;
                default:
            }
            Double kwh = (data.timeFrameEnergy.energy/factor/totaalstroomdelen);
            Double totaalkwh = (data.timeFrameEnergy.energy/factor);
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

    private SolarEdgeData readSolarEdge() {
        String endDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
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
