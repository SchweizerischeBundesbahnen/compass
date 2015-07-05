package ch.masen.compass;

import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by igor on 02.07.15.
 */
public class ConfigHelper {

    private static Properties configProperties;

    public static Properties getConfigProperties() {
        PropertiesHelper propertiesHelper = new PropertiesHelper("config.properties");
        configProperties = propertiesHelper.getProperties();
        return configProperties;
    }

}
