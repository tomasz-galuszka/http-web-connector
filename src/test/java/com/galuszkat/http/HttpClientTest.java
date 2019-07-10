package com.galuszkat.http;

import com.galuszkat.http.exception.HttpClientException;
import org.junit.jupiter.api.Test;

class HttpClientTest {

    private final HttpClient client = new HttpClient(Configuration.builder()
            .encoding("UTF-8")
            .browserName("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20120427 Firefox/15.0a1")
            .build());
    private static final String TEST_URL = "https://www.otodom.pl/sprzedaz/mieszkanie/krakow/?search%5Bfilter_float_price%3Afrom%5D=350000&search%5Bfilter_float_price%3Ato%5D=450000&search%5Bfilter_float_m%3Afrom%5D=25&search%5Bfilter_float_m%3Ato%5D=150&search%5Bfilter_enum_market%5D%5B0%5D=primary&search%5Bfilter_enum_market%5D%5B1%5D=secondary&search%5Bdescription%5D=1&search%5Bdistrict_id%5D=126939&search%5Bcity_id%5D=38&search%5Bsubregion_id%5D=410&search%5Bregion_id%5D=6";

    @Test
    void getString() throws HttpClientException {
        System.out.println(client.readString(HttpMethod.GET, TEST_URL));
    }

    @Test
    void getDocument() throws HttpClientException {
        System.out.println(client.readDocument(HttpMethod.GET, TEST_URL).getBody().toString());
    }
}