package cl.intelidata.security.util;

import java.util.regex.Pattern;

public class Regex {
    public static  final Pattern VALID_PASSWORD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
}
