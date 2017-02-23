package ch.sbb.compass.rest.controller;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.sbb.compass.redis.ShortLinkDAO;
import ch.sbb.compass.redis.ShortLinkDTO;

/**
 * This controller is responsible for redirecting from the short links to the final destinations.
 * 
 * @author Igor Masen
 * @author Kerem Adigüzel
 * @since 24.06.16
 */
@RestController
public class RedirectFromShortLinkController {
    private static final Logger LOG = Logger.getLogger(RedirectFromShortLinkController.class.getSimpleName());

    @Autowired
    private ShortLinkDAO shortLinkDAO;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    void redirectShortLink(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws IOException {

        Long startTime = System.currentTimeMillis();
        String redirectKey = httpServletRequest.getRequestURI().substring(1);
        ShortLinkDTO redirect = shortLinkDAO.getShortLink(redirectKey);

        if (redirect != null) {
            String redirectUrl = redirect.getDestUrl().toString();
            httpServletResponse.setStatus(HttpServletResponse.SC_FOUND);
            httpServletResponse.setHeader("Location", redirectUrl);
            shortLinkDAO.incrementRedirectCounter(redirect);
            final long timeConsumed = System.currentTimeMillis() - startTime;
            LOG.info("Redirect: " + redirectKey + " " + redirectUrl + " took " + timeConsumed + " ms");
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Id is unknown.");
        }
    }
}