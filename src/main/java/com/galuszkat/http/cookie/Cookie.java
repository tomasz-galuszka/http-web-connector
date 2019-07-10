package com.galuszkat.http.cookie;

import lombok.Getter;
import lombok.ToString;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ToString
public class Cookie {

    private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";

    @Getter
    private final String name;
    private final String value;
    private final Map<String, String> properties = new HashMap<>();

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void add(String name, String value) {
        properties.put(name, value);
    }

    public boolean isNotExpired() {
        var expires = properties.get("expires");
        var sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            return (new Date().compareTo(sdf.parse(expires))) <= 0;
        } catch (java.text.ParseException pe) {
            return false;
        }
    }

    public String convertToStringHeader() {
        var cookieString = new StringBuilder()
                .append(getName())
                .append("=")
                .append(value)
                .append(";");

        properties.keySet().forEach(key -> cookieString.append(key).append("=").append(properties.get(key)).append(";"));

        if (cookieString.length() > 0) {
            cookieString.deleteCharAt(cookieString.length() - 1);
        }

        return cookieString.toString();
    }
}
