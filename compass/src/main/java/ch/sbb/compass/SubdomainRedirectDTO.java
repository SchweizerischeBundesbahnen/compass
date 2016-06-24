package ch.sbb.compass;

import java.net.URL;

/**
 * Created by igor on 08.07.15.
 */
public class SubdomainRedirectDTO {

    private URL srcUrl;
    private URL destUrl;
    private int redirectCount;

    public SubdomainRedirectDTO() {

    }

    public SubdomainRedirectDTO(URL srcUrl, URL destUrl) {
        this.srcUrl = srcUrl;
        this.destUrl = destUrl;
    }

    public URL getSrcUrl() {
        return srcUrl;
    }

    public void setSrcUrl(URL srcUrl) {
        this.srcUrl = srcUrl;
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
        return "SubdomainRedirectDTO{" +
                "srcUrl='" + srcUrl + '\'' +
                ", destUrl='" + destUrl + '\'' +
                ", redirectCount=" + redirectCount +
                '}';
    }
}
