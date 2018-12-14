package hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import hello.ns.ActueleVertrekTijden;
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
import java.util.Date;

@Controller
@Configuration
public class TreinController {

    protected final Log logger = LogFactory.getLog(this.getClass());

    HttpClientBuilder clientbuilder = HttpClientBuilder.create();

    @GetMapping("/trein")
    public String trein(HttpServletResponse response,
                          @RequestParam(name="naam", required=false, defaultValue="Deelstromer") String naam,
                          @RequestParam(name="stroomdelen", required=false, defaultValue="0") Integer stroomdelen,
                          Model model,
                          @Value("${ns.username}") String username,
                          @Value("${ns.password}") String password) throws IOException {
        ActueleVertrekTijden data = readTreinData(username, password);
        model.addAttribute("trein", data.vertrekkendeTrein[0]);
        return "trein";
    }

    private ActueleVertrekTijden readTreinData(String username, String password) {
        ActueleVertrekTijden data = new ActueleVertrekTijden();
        HttpHost target = new HttpHost("webservices.ns.nl", 80, "http");
        HttpGet getRequest = new HttpGet("/ns-api-avt?station=delft");

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);

        try (CloseableHttpClient httpclient = clientbuilder.setDefaultCredentialsProvider(provider).build()) {
            HttpResponse httpResponse = httpclient.execute(target, getRequest);
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String xml = EntityUtils.toString(entity);
                JacksonXmlModule xmlModule = new JacksonXmlModule();
                xmlModule.setDefaultUseWrapper(false);

                XmlMapper xmlMapper = new XmlMapper(xmlModule);
                data = xmlMapper.readValue(xml, ActueleVertrekTijden.class);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return data;
    }



}
