package ch.masen.compass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

@SpringBootApplication
@RestController
@EnableScheduling
public class Compass {

    private Logger log = Logger.getLogger("ch.masen.compass.Compass");
    private RedirectDAO rdao = new RedirectDAO();

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Compass.class, args);
    }

    @RequestMapping(value = "/x/**", method = RequestMethod.GET)
    void redirectShortLink(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Long startTime = System.currentTimeMillis();
        String requestUri = httpServletRequest.getRequestURI();
        String redirectKey = requestUri.split("/")[2];
        RedirectDTO redirect = rdao.getRedirect(redirectKey);

        if (redirect != null) {
            String redirectUrl = redirect.getDestUrl().toString();
            httpServletResponse.setStatus(302);
            httpServletResponse.setHeader("Location", redirectUrl);
            rdao.incrementRedirectCounter(redirect);
            log.info("Redirect: " + redirectKey + " " + redirectUrl + " took " + (System.currentTimeMillis() - startTime) + " ms");
        } else {
            httpServletResponse.sendError(404, "Not found");
        }

    }

    @RequestMapping(value = "/rest/1.0/create", method = RequestMethod.POST)
    String create(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Long startTime = System.currentTimeMillis();
        String dest = httpServletRequest.getParameter("dest");
        String id = null;
        if (dest != null) {
            try {
                // check if dest is a valid url
                URL url = new URL(dest);

                //create short link
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.reset();
                md.update(dest.getBytes());
                byte[] digest = md.digest();
                BigInteger bigInteger = new BigInteger(1, digest);
                String idLong = bigInteger.toString(16);
                //take only the first 8 chars
                id = idLong.substring(0, 8);

                // set the short link and dest into db
                RedirectDTO redirect = new RedirectDTO(id, new URL(dest));
                rdao.addRedirect(redirect);
                log.info("Created: " + redirect.getId() + " " + redirect.getDestUrl() + " took " + (System.currentTimeMillis() - startTime) + " ms");

            } catch (NoSuchAlgorithmException e) {
                log.info("Could not find Algorithm " + e.getMessage());
            } catch (MalformedURLException e) {
                log.info("Could not create " + dest + ", message was " + e.getMessage());
                httpServletResponse.sendError(400, "Bad request: " + e.getMessage());
            }
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }
        return id;
    }

    @RequestMapping(value = "/rest/1.0/delete", method = RequestMethod.POST)
    void delete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        String id = httpServletRequest.getParameter("id");
        if (id != null) {
            rdao.deleteRedirect(id);
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }
    }

    @RequestMapping(value = "/rest/1.0/getall", method = RequestMethod.GET)
    ArrayList<RedirectDTO> getAll() throws IOException {
        return rdao.getAllRedirects();
    }

    @RequestMapping(value = "/rest/1.0/flushall", method = RequestMethod.GET)
    void flushall() {
        rdao.flushDb();
    }
}
