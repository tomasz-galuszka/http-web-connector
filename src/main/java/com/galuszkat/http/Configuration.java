package com.galuszkat.http;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Configuration {

    @Builder.Default
    private String browserName = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20120427 Firefox/15.0a1";
    @Builder.Default
    private String encoding = "UTF-8";
    // Proxy settings
    @Builder.Default
    private boolean userProxy = false;
    private String proxyType;
    private String proxyIp;
    private int proxyPort;
    // Sleep time between each request
    @Builder.Default
    private int sleep = 100;
}
