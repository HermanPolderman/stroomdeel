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
    String startDate = "2018-06-01";
    String endDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    String siteid = "715131";
    String api_key = "LNEPU21MZLCJSPTEDWPXAQZ09SMFXL8D";
    String siteidpolletjes = "564256";
    String api_key_polletjes = "NCFWGOTDJ225818AQJTFFT1W137XMCQO";
    HttpGet getRequest = new HttpGet("/site/"+siteid+"/timeFrameEnergy.json?startDate="+startDate+"&endDate="+endDate+"&api_key="+api_key);

    @GetMapping("/stroomdeel")
    public String greeting(@RequestParam(name="naam", required=false, defaultValue="Herman") String naam, Model model) {

        Integer stroomdelen = 1;
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
                model.addAttribute("kwh", String.format( "%.2f", kwh ));
                model.addAttribute("totaalkwh", String.format( "%.2f", totaalkwh));
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                System.out.println(model.toString());
                return "stroomdeel";
            }
        } catch (Exception e) {
            return "fout";
        }

        return "fout";

    }

}
