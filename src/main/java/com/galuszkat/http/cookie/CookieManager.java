package com.galuszkat.http.cookie;

import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class CookieManager {

    private static final String HEADER_SET_COOKIE = "Set-Cookie";
    private static final String COOKIE_VALUE_DELIMITER = ";";
    private static final String COOKIE_NAME_VALUE_SEPARATOR = "=";

    public Set<Cookie> readCookies(HttpURLConnection connection) {
        Set<Cookie> cookies = new HashSet<>();
        String headerName;
        for (int i = 1; (headerName = connection.getHeaderFieldKey(i)) != null; i++) {
            if (!headerName.equalsIgnoreCase(HEADER_SET_COOKIE)) {
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(connection.getHeaderField(i), COOKIE_VALUE_DELIMITER);

            // the specification dictates that the first name/value pair
            // in the string is the cookie name and value, so let's handle
            // them as a special case:

            Cookie cookie = null;
            if (tokenizer.hasMoreTokens()) {
                var token = tokenizer.nextToken();
                var name = token.substring(0, token.indexOf(COOKIE_NAME_VALUE_SEPARATOR));
                var value = token.substring(token.indexOf(COOKIE_NAME_VALUE_SEPARATOR) + 1);
                cookie = new Cookie(name, value);

            }
            while (cookie != null && tokenizer.hasMoreTokens()) {
                var token = tokenizer.nextToken();
                if (!token.contains(COOKIE_NAME_VALUE_SEPARATOR)) {
                    continue;
                }
                var name = token.substring(0, token.indexOf(COOKIE_NAME_VALUE_SEPARATOR)).toLowerCase();
                var value = token.substring(token.indexOf(COOKIE_NAME_VALUE_SEPARATOR) + 1);
                cookie.add(name, value);
            }
            cookies.add(cookie);
        }
        return cookies;
    }

    public static String getCookiesString(Set<Cookie> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return "";
        }

        var cookieBuilder = new StringBuilder();
        for (Cookie c : cookies) {
            cookieBuilder.append(c.convertToStringHeader()).append(";");
        }
        cookieBuilder.deleteCharAt(cookieBuilder.length() - 1);
        return cookieBuilder.toString();
    }
}

