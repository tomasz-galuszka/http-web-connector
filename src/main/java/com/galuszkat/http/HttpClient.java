package com.galuszkat.http;

import com.galuszkat.http.cookie.Cookie;
import com.galuszkat.http.cookie.CookieManager;
import com.galuszkat.http.exception.HttpClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Collections;
import java.util.Set;

import static com.galuszkat.http.cookie.CookieManager.getCookiesString;

@Slf4j
@RequiredArgsConstructor
public class HttpClient {

    private final Configuration configuration;

    public Response<String> readString(HttpMethod method, String url) throws HttpClientException {
        try {
            var connection = execute(method, url);

            sleep();

            Response<String> response = new Response<>(asString(connection), new CookieManager().readCookies(connection));

            connection.disconnect();

            return response;

        } catch (IOException e) {
            throw new HttpClientException("Error while downloading web page", e);
        }
    }

    public Response<Document> readDocument(HttpMethod method, String url) throws HttpClientException {
        try {
            var connection = execute(method, url);

            var response = asString(connection);
            var cookies = new CookieManager().readCookies(connection);
            var htmlContent = new String(response.getBytes(configuration.getEncoding()), configuration.getEncoding());

            var props = new CleanerProperties();
            props.setCharset(configuration.getEncoding());
            props.setAdvancedXmlEscape(false);
            props.setTranslateSpecialEntities(false);

            Document document = new DomSerializer(props, false).createDOM(new HtmlCleaner().clean(htmlContent));

            sleep();

            Response<Document> result = new Response<>(document, cookies);

            connection.disconnect();

            return result;

        } catch (IOException | ParserConfigurationException e) {
            throw new HttpClientException("Error while downloading web page", e);
        }
    }

    private HttpURLConnection execute(HttpMethod method, String url) throws IOException, HttpClientException {
        return execute(method, url, Collections.emptySet());
    }

    private HttpURLConnection execute(HttpMethod method, String url, Set<Cookie> cookies) throws IOException, HttpClientException {
        try {
            var proxy = readProxy();

            log.info("Opening connection {}", url);
            var connection = (HttpURLConnection) new URL(url).openConnection(proxy);
            appendProperties(method, cookies, connection);

            int responseCode = connection.getResponseCode();
            log.info("Response code {}", responseCode);

            while (isRedirect(responseCode)) {
                log.info("Redirect");
                connection = (HttpURLConnection) new URL(connection.getHeaderField("Location")).openConnection(proxy);
                appendProperties(method, cookies, connection);
                responseCode = connection.getResponseCode();
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Error while connection, http response code: " + responseCode);
            }

            return connection;

        } catch (MalformedURLException e) {
            throw new HttpClientException("Incorrect url address, check if address : " + url + " exists", e);
        }
    }

    private boolean isRedirect(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_MOVED_TEMP  || responseCode == HttpURLConnection.HTTP_MOVED_PERM  || responseCode == HttpURLConnection.HTTP_SEE_OTHER;
    }

    private void appendProperties(HttpMethod method, Set<Cookie> cookies, HttpURLConnection connection) throws ProtocolException {
        connection.setRequestProperty("Cookie", getCookiesString(cookies));
        connection.setRequestMethod(method.name());
        connection.setRequestProperty("User-Agent", configuration.getBrowserName());
        connection.setRequestProperty("Content-Type", "text/html; charset=" + configuration.getEncoding());
    }

    private Proxy readProxy() {
        if (!configuration.isUserProxy()) {
            return Proxy.NO_PROXY;
        }
        if ("SOCKS".equalsIgnoreCase(configuration.getProxyType())) {
            return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(configuration.getProxyIp(), configuration.getProxyPort()));
        } else {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(configuration.getProxyIp(), configuration.getProxyPort()));
        }
    }

    private void sleep() {
        try {
            Thread.sleep(configuration.getSleep());
        } catch (InterruptedException ex) {
            log.error(ex.getMessage());
        }
    }

    private String asString(HttpURLConnection connection) throws IOException {
        var bufferedIn = new BufferedReader(new InputStreamReader(connection.getInputStream(), configuration.getEncoding()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = bufferedIn.readLine()) != null) {
            response.append(line);
        }
        bufferedIn.close();
        return response.toString();
    }

}
