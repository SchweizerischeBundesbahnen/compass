package ch.sbb.compass.redis;

import java.net.URL;

/**
 * The data transfer object for a short link.
 * 
 * @author Igor Masen
 * @since 08.07.15
 */
public class ShortLinkDTO {

    private String id;
    private URL destUrl;
    private int redirectCount;

    public ShortLinkDTO(String id, URL destUrl) {
        this.id = id;
        this.destUrl = destUrl;
    }

    public String getId() {
        return id;
    }

    public URL getDestUrl() {
        return destUrl;
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