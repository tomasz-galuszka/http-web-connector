# http-web-connector
Simple java based library http client library to feed web crawlers with a data.

```xml
    <bean id="http-client-config" class="com.galuszkat.http.Configuration">
        <property name="browserName" value="${browserName}"/>
        <property name="encoding" value="UTF-8"/>
        <property name="userProxy" value="${useProxy}"/>
        <property name="sleep" value="${sleep}"/>
        <property name="proxyIp" value="${ip}"/>
        <property name="proxyPort" value="${port}"/>
        <property name="proxyType" value="${type}"/>
    </bean>

    <bean id="http-client" class="com.galuszkat.http.HttpClient">
        <constructor-arg name="configuration" ref="http-client-config"/>
    </bean>
```
Usage : 
```Java
public class GenericMdt {

    @Autowired
    private HttpClient client;

    public Document getDocument(HttpMethod method, String url) throws HttpClientException {
        return client.getDocument(method, url);
    }

}
```
