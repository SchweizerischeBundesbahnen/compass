package ch.masen.compass;

import java.net.URL;

/**
 * Created by igor on 08.07.15.
 */
public class RedirectDTO {

    private String id;
    private URL destUrl;
    private int redirectCount;

    public RedirectDTO() {

    }

    public RedirectDTO(String id, URL destUrl) {
        this.id=id;
        this.destUrl=destUrl;
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
        return "RedirectDTO{" +
                "id='" + id + '\'' +
                ", destUrl='" + destUrl + '\'' +
                ", redirectCount=" + redirectCount +
                '}';
    }
}
