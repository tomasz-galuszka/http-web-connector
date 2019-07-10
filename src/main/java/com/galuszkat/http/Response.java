package com.galuszkat.http;

import com.galuszkat.http.cookie.Cookie;
import lombok.ToString;
import lombok.Value;

import java.util.Set;

@Value
@ToString
public class Response<T> {

    private T body;
    private Set<Cookie> cookies;

}
