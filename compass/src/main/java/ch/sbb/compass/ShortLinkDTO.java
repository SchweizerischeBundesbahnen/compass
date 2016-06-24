package ch.sbb.compass;

import java.net.URL;

import org.springframework.stereotype.Component;

/**
 * Created by igor on 08.07.15.
 */
@Component
public class ShortLinkDTO {

    private String id;
    private URL destUrl;
    private int redirectCount;

    public ShortLinkDTO() { // Spring constructor
    }

    public ShortLinkDTO(String id, URL destUrl) {
        this.id = id;
        this.destUrl = destUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URL getDestUrl() {
        return destUrl;
    }

    public void setDestUrl(URL destUrl) {
        this.destUrl = destUrl;
    }

    public int getRedirectCount() {
        return redirectCount;
    }

    public void setRedirectCount(int redirectCount) {
        this.redirectCount = redirectCount;
    }

    @Override
    public String toString() {
        return "ShortLinkDTO{" +
                "id='" + id + '\'' +
                ", destUrl='" + destUrl + '\'' +
                ", redirectCount=" + redirectCount +
                '}';
    }
}
