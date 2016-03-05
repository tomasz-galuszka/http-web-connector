package com.galuszkat.http;

import com.galuszkat.http.cookie.Cookie;
import com.galuszkat.http.cookie.CookieManager;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.*;
import java.util.Set;

public class HttpConnector {

    public class HttpConnectorException extends Exception {

        public HttpConnectorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private Configuration configuration;
    private Set<Cookie> cookies;
    private boolean afterFirstConnection = false; // uzywane do odczytu cookie za pierwszym razem

    public HttpConnector(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getString(String www) throws HttpConnectorException {
        String response;
        try {
            HttpURLConnection connection = connect(www);
            cookies = new CookieManager(connection).readCookies();
            response = getResponseAsString(connection);
        } catch (IOException e) {
            throw new HttpConnectorException("Error while downloading web page", e);
        }
        return response;
    }

    public Document getDocument(String www) throws HttpConnectorException {
        Document document;
        try {
            HttpURLConnection connection = connect(www);
            cookies = new CookieManager(connection).readCookies();
            String response = getResponseAsString(connection);
            byte[] bytes = response.getBytes(configuration.getEncoding());
            String htmlContent = new String(bytes, configuration.getEncoding());
            TagNode cleanedHtml = new HtmlCleaner().clean(htmlContent);
            CleanerProperties props = new CleanerProperties();
            props.setCharset(configuration.getEncoding());
            props.setAdvancedXmlEscape(false);
            props.setTranslateSpecialEntities(false);
            document = new DomSerializer(props, false).createDOM(cleanedHtml);

            try {
                Thread.sleep(configuration.getSleep());
            } catch (InterruptedException ignored) {
            }

        } catch (IOException e) {
            throw new HttpConnectorException("Error while downloading web page", e);
        } catch (ParserConfigurationException e) {
            throw new HttpConnectorException("Error while downloading web page", e);
        }
        return document;
    }

    public Document createFromString(String scriptContent) throws ParserConfigurationException {
        TagNode cleanedHtml = new HtmlCleaner().clean(scriptContent);
        CleanerProperties props = new CleanerProperties();
        props.setCharset(configuration.getEncoding());
        props.setAdvancedXmlEscape(true);
        props.setTranslateSpecialEntities(false);
        props.setAllowHtmlInsideAttributes(true);
        return new DomSerializer(props, false).createDOM(cleanedHtml);
    }

    //method to convert Document to String
    public static String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String getCookiesString(Set<Cookie> cookies) throws HttpConnectorException {
        if (cookies == null || cookies.isEmpty()) {
            return "";
        }

        StringBuilder cookieBuilder = new StringBuilder();
        for (Cookie c : cookies) {
            cookieBuilder.append(c.convertToStringHeader()).append(";");
        }
        cookieBuilder.deleteCharAt(cookieBuilder.length() - 1);
        return cookieBuilder.toString();
    }

    private String getResponseAsString(HttpURLConnection connection) throws IOException, HttpConnectorException {
        BufferedReader bufferedIn = new BufferedReader(new InputStreamReader(connection.getInputStream(), configuration.getEncoding()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = bufferedIn.readLine()) != null) {
            response.append(line);
        }
        bufferedIn.close();
        return response.toString();
    }

    private HttpURLConnection connect(String www) throws IOException, HttpConnectorException {
        HttpURLConnection connection;
        try {
            URL url = new URL(www);
            if (configuration.isUserProxy()) {
                if (configuration.getProxyType().equalsIgnoreCase("SOCKS")) {
                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(configuration.getProxyIp(), configuration.getProxyPort()));
                    connection = (HttpURLConnection) url.openConnection(proxy);
                } else {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(configuration.getProxyIp(), configuration.getProxyPort()));
                    connection = (HttpURLConnection) url.openConnection(proxy);
                }

            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            if (afterFirstConnection) {
                connection.setRequestProperty("Cookie", getCookiesString(cookies));
            }
            afterFirstConnection = true;
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", configuration.getBrowserName());
            connection.setRequestProperty("Content-Type", "text/html; charset=UTF-8 ");
            boolean redirect = false;
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                        || responseCode == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            if (redirect) {

                // get redirect url from "location" header field
                String newUrl = connection.getHeaderField("Location");

                // get the cookie if need, for login
                cookies = new CookieManager(connection).readCookies();

                // open the new connnection again
                if (configuration.isUserProxy()) {
                    if (configuration.getProxyType().equalsIgnoreCase("SOCKS")) {
                        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(configuration.getProxyIp(), configuration.getProxyPort()));
                        connection = (HttpURLConnection) new URL(newUrl).openConnection(proxy);
                    } else {
                        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(configuration.getProxyIp(), configuration.getProxyPort()));
                        connection = (HttpURLConnection) new URL(newUrl).openConnection(proxy);
                    }

                } else {
                    connection = (HttpURLConnection) new URL(newUrl).openConnection();
                }
                if (afterFirstConnection) {
                    connection.setRequestProperty("Cookie", getCookiesString(cookies));
                }
                afterFirstConnection = true;
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", configuration.getBrowserName());
                connection.setRequestProperty("Content-Type", "text/html; charset=UTF-8 ");
            }

            responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Error while connection, http response code: " + responseCode);
            }

        } catch (MalformedURLException e) {
            throw new HttpConnectorException("Incorrect url address, check if address : " + www + " exists", e);
        }
        return connection;
    }

}
