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

@Controller
public class GreetingController {

    HttpClientBuilder clientbuilder = HttpClientBuilder.create();
    HttpHost target = new HttpHost("monitoringapi.solaredge.com", 443, "https");
    HttpGet getRequest = new HttpGet("/site/564256/timeFrameEnergy.json?startDate=2018-01-01&endDate=2018-05-24&api_key=NCFWGOTDJ225818AQJTFFT1W137XMCQO");

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="herman") String name, Model model) {

        model.addAttribute("name", name);
        try (CloseableHttpClient httpclient = clientbuilder.build()) {
            HttpResponse httpResponse = httpclient.execute(target, getRequest);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String json = EntityUtils.toString(entity);
                ObjectMapper m = new ObjectMapper();
                SolarEdgeData data = m.readValue(json, SolarEdgeData.class);
                model.addAttribute("kwh", "123");
                model.addAttribute("totaalkwh", data.timeFrameEnergy.energy);
            }
            model.addAttribute("stroomdelen", "10");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "greeting";
    }

}
