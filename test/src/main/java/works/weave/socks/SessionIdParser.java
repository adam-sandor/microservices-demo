package works.weave.socks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionIdParser {

    public static String parseSessionId(String sessionCookie) {
        Matcher matcher = Pattern.compile("logged_in=(.+?);").matcher(sessionCookie);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
