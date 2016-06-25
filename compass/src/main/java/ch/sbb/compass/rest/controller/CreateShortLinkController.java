package ch.sbb.compass.rest.controller;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import ch.sbb.compass.redis.ShortLinkDAO;
import ch.sbb.compass.redis.ShortLinkDTO;
import ch.sbb.compass.util.exception.KeyAlreadyExistsException;

/**
 * This controller handles the case, where a new short link has to be generated.
 * 
 * @author Igor Masen
 * @author Kerem Adigüzel
 * @since 24.06.16
 */
@RestController
public class CreateShortLinkController {
    private static final Logger log = Logger.getLogger(CreateShortLinkController.class.getSimpleName());

    private static final String PARAMETER_NAME_DESTINATION = "dest";

    @Autowired
    private Gson gson; // Auto configured in Spring
    @Autowired
    private ShortLinkDAO shortLinkDAO;

    /**
     * This method expects a parameter named <tt>dest</tt> with a valid URL pointing to any location and then
     * generates an ID for that destination and tries to store it in the database. If the key already exists
     * and is equal to the destination, the ID is returned, as if the key was newly generated.
     * <p>
     * A sample JSON response may look like the following example:
     * <center>{"id":"75a9ce29","destUrl":"http://www.sbb.ch","redirectCount":0}</center>
     *
     * @param httpServletRequest The request object containing the required parameters
     * @param httpServletResponse The response object to use when an unexpected situation occurs
     * @return a JSON response consisting of the parameters <tt>id</tt>, <tt>destUrl</tt> and
     *         <tt>redirectCount</tt>
     */
    @RequestMapping(value = "/rest/1.0/shortlink/create", method = RequestMethod.POST)
    String create(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws IOException {

        long startTime = System.currentTimeMillis();
        String destination = httpServletRequest.getParameter(PARAMETER_NAME_DESTINATION);
        ShortLinkDTO shortLink = null;

        try {
            URL url = new URL(destination); // checks if destination is a valid url
            String id = createShortLinkId(destination);

            shortLink = new ShortLinkDTO(id, url);
            shortLinkDAO.addShortLink(shortLink);

            final long timeConsumed = System.currentTimeMillis() - startTime;
            log.info("Created: " + id + " " + url + " took " + timeConsumed + " ms");

        } catch (MalformedURLException e) {
            log.info("Could not create " + destination +
                    ". Please check your URL, it seems to be malformed. " + e.getMessage());
            httpServletResponse.sendError(SC_BAD_REQUEST, "Bad request: " + e.getMessage());
        } catch (KeyAlreadyExistsException e) {
            final String id = shortLink.getId();
            if (id.equals(shortLinkDAO.getShortLink(id).getId())) {
                log.info(e.getMessage() + " " + shortLink.getDestUrl() + " took " +
                        (System.currentTimeMillis() - startTime) + " ms");
            } else {
                log.warning(e.getMessage() + " collision for key " + id +
                        ". RedirectLink " + shortLink.getDestUrl() + " is not equal to " +
                        shortLinkDAO.getShortLink(id).getDestUrl());
            }
        }

        return gson.toJson(shortLink);
    }

    private String createShortLinkId(String dest) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(dest.getBytes());
            byte[] digest = md.digest();
            BigInteger bigInteger = new BigInteger(1, digest);
            String idLong = bigInteger.toString(16);
            return idLong.substring(0, 8); // take only the first 8 chars
        } catch (NoSuchAlgorithmException e) {
            log.info("Could not find Algorithm " + e.getMessage());
            return null;
        }
    }
}