# http-web-connector
Simmple library to read the web pages

```xml
    <bean id="http-connector-config" class="com.galuszkat.http.Configuration">
        <property name="browserName" value="${browserName}"/>
        <property name="encoding" value="UTF-8"/>
        <property name="userProxy" value="${useProxy}"/>
        <property name="sleep" value="${sleep}"/>
        <property name="proxyIp" value="${ip}"/>
        <property name="proxyPort" value="${port}"/>
        <property name="proxyType" value="${type}"/>
    </bean>

    <bean id="HttpConnector" class="com.galuszkat.http.HttpConnector">
        <constructor-arg name="configuration" ref="http-connector-config"/>
    </bean>
```
