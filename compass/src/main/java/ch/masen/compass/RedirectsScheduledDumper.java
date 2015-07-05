package ch.masen.compass;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by igor on 04.07.15.
 */
@Component
public class RedirectsScheduledDumper {

    @Scheduled(fixedRate = 10000)
    public void dumpRedirects() throws MalformedURLException {
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(ConfigHelper.getConfigProperties().getProperty("BASEURL").toString());
        baseUrl.append("/dump");

        try {
            URL dumpUrl = new URL(baseUrl.toString());
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) dumpUrl.openConnection();
            connection.setRequestMethod("GET");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
