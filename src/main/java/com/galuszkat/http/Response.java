package com.galuszkat.http;

import com.galuszkat.http.cookie.Cookie;
import lombok.Value;

import java.util.Set;

@Value
public class Response<T> {

    private T body;
    private Set<Cookie> cookies;

}
