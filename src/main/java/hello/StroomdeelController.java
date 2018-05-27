package hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class StroomdeelController {

    HttpClientBuilder clientbuilder = HttpClientBuilder.create();
    HttpHost target = new HttpHost("monitoringapi.solaredge.com", 443, "https");
    String startDate = "2018-01-01";
    String endDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    HttpGet getRequest = new HttpGet("/site/564256/timeFrameEnergy.json?startDate="+startDate+"&endDate="+endDate+"&api_key=NCFWGOTDJ225818AQJTFFT1W137XMCQO");

    @GetMapping("/stroomdeel")
    public String greeting(@RequestParam(name="naam", required=false, defaultValue="Herman") String naam, Model model) {

        Integer stroomdelen = 10;
        Integer totaalstroomdelen = 912;

        model.addAttribute("naam", naam);
        model.addAttribute("stroomdelen", stroomdelen);
        model.addAttribute("totaalstroomdelen", totaalstroomdelen);
        try (CloseableHttpClient httpclient = clientbuilder.build()) {
            HttpResponse httpResponse = httpclient.execute(target, getRequest);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String json = EntityUtils.toString(entity);
                ObjectMapper m = new ObjectMapper();
                SolarEdgeData data = m.readValue(json, SolarEdgeData.class);
                int factor =1;
                switch (data.timeFrameEnergy.unit) {
                    case "Wh":
                        factor = 1000;
                        break;
                    default:
                }
                Double kwh = (data.timeFrameEnergy.energy/factor/totaalstroomdelen*stroomdelen);
                Double totaalkwh = (data.timeFrameEnergy.energy/factor);
                model.addAttribute("unit", "kWh");

                model.addAttribute("kwh", kwh.intValue());
                model.addAttribute("totaalkwh", totaalkwh.intValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "stroomdeel";
    }

}
