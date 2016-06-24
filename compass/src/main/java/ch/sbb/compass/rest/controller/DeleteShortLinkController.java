package ch.sbb.compass.rest.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.sbb.compass.redis.ShortLinkDAO;

/**
 * This controller is responsible for deleting short links from the database.
 * 
 * @author Igor Masen
 * @author Kerem Adigüzel
 * @since 24.06.16
 */
@RestController
public class DeleteShortLinkController {

    @Autowired
    private ShortLinkDAO shortLinkDAO;

    /**
     * This method expects a parameter named "id". If this ID is not empty, it tries to delete the short link
     * from the database.
     *
     * @param httpServletRequest The request object containing the required parameters
     * @param httpServletResponse The response object to use when an unexpected situation occurs
     */
    @RequestMapping(value = "/rest/1.0/shortlink/delete", method = RequestMethod.DELETE)
    void delete(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws IOException {

        String id = httpServletRequest.getParameter("id");
        if (StringUtils.isEmpty(id)) {
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            shortLinkDAO.deleteShortLink(id);
        }
    }
}