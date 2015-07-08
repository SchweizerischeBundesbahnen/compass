package ch.masen.compass;

/**
 * Created by igor on 08.07.15.
 */
public class RedirectDTO {

    private String id;
    private String destUrl;
    private int redirectCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDestUrl() {
        return destUrl;
    }

    public void setDestUrl(String destUrl) {
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
