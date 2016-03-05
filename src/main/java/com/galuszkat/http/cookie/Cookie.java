package com.galuszkat.http.cookie;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Cookie {

    private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
    private String name;
    private String value;
    private Map<String, String> properties;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
        this.properties = new HashMap<String, String>();
    }

    public void add(String name, String value) {
        properties.put(name, value);
    }

    public String getName() {
        return name;
    }

    public boolean isNotExpired() {
        String expires = properties.get("expires");
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            return (new Date().compareTo(sdf.parse(expires))) <= 0;
        } catch (java.text.ParseException pe) {
            pe.printStackTrace();
            return false;
        }
    }

    public String convertToStringHeader() {
        StringBuilder cookieString = new StringBuilder();
        cookieString.append(getName()).append("=").append(value).append(";");

        Set<String> keys = properties.keySet();
        for (String key : keys) {
            String value = properties.get(key);
            cookieString.append(key).append("=").append(value).append(";");
        }
        if (cookieString.length() > 0) {
            cookieString.deleteCharAt(cookieString.length() - 1);
        }

        return cookieString.toString();
    }


    @Override
    public String toString() {
        return "Cookie{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", properties=" + properties +
                '}';
    }
}
