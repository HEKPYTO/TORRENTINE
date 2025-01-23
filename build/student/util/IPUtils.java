package util;

import java.util.regex.Pattern;

public class IPUtils {
    public static String validateIP(String ip) {
        // Define the IPv4 regex pattern
        String regex = "^(25[0-5]|2[0-4][0-9]|1[0-9]{1,2}|[1-9]?[0-9])"
                + "(\\.(25[0-5]|2[0-4][0-9]|1[0-9]{1,2}|[1-9]?[0-9])){3}$";

        // Compile the pattern
        Pattern pattern = Pattern.compile(regex);

        // Throw an exception if the input does not match the pattern
        if (!pattern.matcher(ip).matches()) {
            throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
        }

        return ip;
    }
}
