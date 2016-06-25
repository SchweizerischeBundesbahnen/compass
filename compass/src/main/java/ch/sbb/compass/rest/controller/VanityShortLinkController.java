package ch.sbb.compass.rest.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ch.sbb.compass.redis.ShortLinkDAO;
import ch.sbb.compass.redis.ShortLinkDTO;
import ch.sbb.compass.util.exception.KeyAlreadyExistsException;

/**
 * This controller is responsible for creating vanity URLs for short links of the form
 * <tt>compass.masen.ch/x/myShortUrl</tt> instead of having a hash as an identifier for the redirection.
 * 
 * @author Kerem Adigüzel
 * @since 25.06.16
 */
@RestController
public class VanityShortLinkController {

    private static final Logger LOG = Logger.getLogger(VanityShortLinkController.class);

    @Autowired
    private ShortLinkDAO shortLinkDAO;

    /**
     * Tries to create a vanity short link, if it not already exists in the database. Otherwise sends an error
     * response with the info why the request failed.
     * 
     * @param text The text to use as an ID for the short link.
     * @param url The destination URL for redirection. Needs to be a valid {@link URL} object.
     * @return a {@link ShortLinkDTO} with <tt>text</tt> as the ID and <tt>url</tt> as the destination if the
     * request could be handled successfully, otherwise an error response
     */
    @RequestMapping(value = "/rest/1.0/shortlink/createVanityUrl", method = RequestMethod.POST)
    public @ResponseBody ShortLinkDTO createVanityUrl(@RequestParam String text, @RequestParam String url,
            HttpServletResponse response) throws IOException {

        if (StringUtils.isEmpty(text) || StringUtils.isEmpty(url)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request: both parameters 'text' " +
                    "and 'url' cannot be empty!");
            return null; // don't need to continue, since we got a bad request
        }

        ShortLinkDTO shortLink = null;
        try {
            URL destinationUrl = new URL(url); // checks for validity of the destinationUrl

            shortLink = new ShortLinkDTO(text, destinationUrl);
            shortLinkDAO.addShortLink(shortLink);

        } catch (MalformedURLException e) {
            final String msg = "The destination URL seems to be " + "invalid: " + e.getMessage();
            LOG.warn(msg, e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
        } catch (KeyAlreadyExistsException e) {
            LOG.info(e);
            response.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        }

        return shortLink;
    }
}