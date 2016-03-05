package com.galuszkat.http.cookie;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class CookieManager {

    private static final char DOT = '.';
    private static final String HEADER_SET_COOKIE = "Set-Cookie";
    private static final String COOKIE_VALUE_DELIMITER = ";";
    private static final String COOKIE_NAME_VALUE_SEPARATOR = "=";

    private final HttpURLConnection connection;
    private Set<Cookie> cookies;

    public CookieManager(HttpURLConnection connection) {
        this.connection = connection;
        this.cookies = new HashSet<Cookie>();
    }

    public Set<Cookie> readCookies() {
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
                String token = tokenizer.nextToken();
                String name = token.substring(0, token.indexOf(COOKIE_NAME_VALUE_SEPARATOR));
                String value = token.substring(token.indexOf(COOKIE_NAME_VALUE_SEPARATOR) + 1, token.length());
                cookie = new Cookie(name, value);

            }
            ArrayList<Cookie> cookiesForDomain = new ArrayList<Cookie>();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (!token.contains(COOKIE_NAME_VALUE_SEPARATOR)) {
                    continue;
                }
                String name = token.substring(0, token.indexOf(COOKIE_NAME_VALUE_SEPARATOR)).toLowerCase();
                String value = token.substring(token.indexOf(COOKIE_NAME_VALUE_SEPARATOR) + 1, token.length());
                cookie.add(name, value);
            }
            cookies.add(cookie);
        }
        return this.cookies;
    }
}

