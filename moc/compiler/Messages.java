package moc.compiler;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "moc.compiler.messages";

    private static final ResourceBundle RESOURCE_BUNDLE
        = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {}

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getString(String key, Object... replacement) {
        String result = getString(key);
        for(int i = 0; i < replacement.length; ++i) {
            result = result.replaceAll("\\{" + i + "\\}", replacement[i].toString());
        }
        return result;
    }
}
