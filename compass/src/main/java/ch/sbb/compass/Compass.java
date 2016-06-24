package ch.sbb.compass;

import com.google.gson.Gson;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

@SpringBootApplication
@RestController
@EnableScheduling
public class Compass {

    private Logger log = Logger.getLogger("Compass");
    private ShortLinkDAO shortLinkDAO = new ShortLinkDAO();
    private SubdomainRedirectDAO subdomainRedirectDAO = new SubdomainRedirectDAO();
    private Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Compass.class, args);
    }

    // Redirect Domain
    @RequestMapping(value = "/", method = RequestMethod.GET)
    void redirectSubdomain(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, URISyntaxException {
        Long startTime = System.currentTimeMillis();
        URI requestUri = new URI(httpServletRequest.getRequestURI());
        String domain = requestUri.getHost();
        SubdomainRedirectDTO redirect = subdomainRedirectDAO.getRedirect(domain);

        if(redirect != null) {
            String redirectUrl = String.valueOf(redirect.getDestUrl());
            httpServletResponse.setStatus(302);
            httpServletResponse.setHeader("Location", redirectUrl);
            subdomainRedirectDAO.incrementRedirectCounter(redirect);
            log.info("Subdomain Redirect: " + domain + " " + redirectUrl + " took " + (System.currentTimeMillis() - startTime) + " ms");
        } else {
            httpServletResponse.sendError(404, "Not found");
        }

    }

    @RequestMapping(value = "/rest/1.0/subdomainredirect/create", method = RequestMethod.POST)
    String createDomainRedirect(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Long startTime = System.currentTimeMillis();
        String src = httpServletRequest.getParameter("src");
        String dest = httpServletRequest.getParameter("dest");
        SubdomainRedirectDTO subdomainRedirect = null;

        if (dest != null && src != null) {
            try {
                // check if dest/src is a valid url
                URL destUrl = new URL(dest);
                URL srcUrl = new URL(src);
                subdomainRedirect = new SubdomainRedirectDTO(srcUrl, destUrl);
                subdomainRedirectDAO.addShortLink(subdomainRedirect);
                log.info("Created: " + subdomainRedirect.getSrcUrl() + " " + subdomainRedirect.getDestUrl() + " took " + (System.currentTimeMillis() - startTime) + " ms");

            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("already exists")) {
                    log.info(e.getMessage());
                }

            } catch (MalformedURLException e) {
                log.info("Could not create " + dest + ", message was " + e.getMessage());
                httpServletResponse.sendError(400, "Bad request: " + e.getMessage());
            }
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }
        String json = gson.toJson(subdomainRedirect);

        return json;
    }

    // Redirect shortlinks
    @RequestMapping(value = "/x/**", method = RequestMethod.GET)
    void redirectShortLink(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Long startTime = System.currentTimeMillis();
        String requestUri = httpServletRequest.getRequestURI();
        String redirectKey = requestUri.split("/")[2];
        ShortLinkDTO redirect = shortLinkDAO.getShortLink(redirectKey);

        if (redirect != null) {
            String redirectUrl = redirect.getDestUrl().toString();
            httpServletResponse.setStatus(302);
            httpServletResponse.setHeader("Location", redirectUrl);
            shortLinkDAO.incrementRedirectCounter(redirect);
            log.info("Redirect: " + redirectKey + " " + redirectUrl + " took " + (System.currentTimeMillis() - startTime) + " ms");
        } else {
            httpServletResponse.sendError(404, "Not found");
        }

    }

    @RequestMapping(value = "/rest/1.0/shortlink/create", method = RequestMethod.POST)
    String create(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Long startTime = System.currentTimeMillis();
        String dest = httpServletRequest.getParameter("dest");
        String id = null;
        ShortLinkDTO shortLink = null;

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
                shortLink = new ShortLinkDTO(id, new URL(dest));
                shortLinkDAO.addShortLink(shortLink);
                log.info("Created: " + shortLink.getId() + " " + shortLink.getDestUrl() + " took " + (System.currentTimeMillis() - startTime) + " ms");

            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("already exists")) {
                    log.info(e.getMessage());
                }

            } catch (NoSuchAlgorithmException e) {
                log.info("Could not find Algorithm " + e.getMessage());
            } catch (MalformedURLException e) {
                log.info("Could not create " + dest + ", message was " + e.getMessage());
                httpServletResponse.sendError(400, "Bad request: " + e.getMessage());
            }
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }
        String json = gson.toJson(shortLink);

        return json;
    }

    @RequestMapping(value = "/rest/1.0/shortlink/delete", method = RequestMethod.DELETE)
    void delete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        String id = httpServletRequest.getParameter("id");
        if (id != null) {
            shortLinkDAO.deleteShortLink(id);
        } else {
            httpServletResponse.sendError(400, "Bad request");
        }
    }

    @RequestMapping(value = "/rest/1.0/shortlink/getall", method = RequestMethod.GET)
    String getAll() throws IOException {
        String json = gson.toJson(shortLinkDAO.getAllShortLinks());

        return json;
    }

    /**
    @RequestMapping(value = "/rest/1.0/shortlink/flushall", method = RequestMethod.POST)
    void flushall() {
        shortLinkDAO.flushDb();
    }
    **/
}
