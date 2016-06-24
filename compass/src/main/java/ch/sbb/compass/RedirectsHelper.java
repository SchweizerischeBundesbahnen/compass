package ch.sbb.compass;

import java.io.File;
import java.util.Properties;

/**
 * Created by igor on 02.07.15.
 */
public class RedirectsHelper {

    static Properties redirects;
    static PropertiesHelper propertiesHelper;

    public static Properties getRedirects() {
        if (propertiesHelper == null) {
            initPropertiesHelper();
        }
        redirects = propertiesHelper.getProperties();

        return redirects;
    }

    public static File getRedirectsFile() {
        if (propertiesHelper == null) {
            initPropertiesHelper();
        }
        File redirectsFile = propertiesHelper.getPropertiesFile();

        return redirectsFile;
    }

    private static void initPropertiesHelper() {
        propertiesHelper = new PropertiesHelper("redirects.file");
    }
}
