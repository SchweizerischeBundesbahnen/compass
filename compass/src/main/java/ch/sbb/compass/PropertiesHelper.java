package ch.sbb.compass;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by igor on 02.07.15.
 */
public class PropertiesHelper {

    File propertiesFile;
    Properties properties;

    private Logger log = Logger.getLogger("PropertiesHelper");

    @Autowired
    public PropertiesHelper(String systemProperty) {
        properties = new Properties();
        String systemPropertyString = null;
        try {
            systemPropertyString = System.getProperty(systemProperty);
            InputStream in = new FileInputStream(propertiesFile = new File(systemPropertyString));
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            log.warning("Could not find property file " + systemProperty);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public File getPropertiesFile() {
        return propertiesFile;
    }
}
