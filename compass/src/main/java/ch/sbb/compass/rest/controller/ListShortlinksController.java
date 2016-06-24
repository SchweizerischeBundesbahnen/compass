package ch.sbb.compass.rest.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import ch.sbb.compass.redis.ShortLinkDAO;

/**
 * This controller is responsible for returning a response in JSON format containing all short links from the
 * database.
 * 
 * @author Igor Masen
 * @author Kerem Adigüzel
 * @since 24.06.16
 */
@RestController
public class ListShortLinksController {

    @Autowired // Auto configured in Spring
    private Gson gson;
    @Autowired
    private ShortLinkDAO shortLinkDAO;

    @RequestMapping(value = "/rest/1.0/shortlink/getall", method = RequestMethod.GET)
    String getAll() throws IOException {
        return gson.toJson(shortLinkDAO.getAllShortLinks());
    }
}