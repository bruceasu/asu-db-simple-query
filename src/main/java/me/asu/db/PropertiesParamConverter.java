package me.asu.db;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class PropertiesParamConverter extends BaseConverter<Properties> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    public PropertiesParamConverter(String optionName) {
        super(optionName);
    }

    public Properties convert(String value) {
        Properties prop = new Properties();
        try {
            Path path = Paths.get(value);
            if (Files.isRegularFile(path)) {
                try (InputStream is = Files.newInputStream(path)) {
                    prop.load(is);
                }
            }
        } catch (IOException var3) {
            throw new ParameterException(this.getErrorString(value, String.format(
                    "an trade date formatted date (%s)", DATE_FORMAT.toPattern())));
        }

        return prop;
    }
}
