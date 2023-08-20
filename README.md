# Spring Boot Multiple Database

### Description
As the development processes of today's applications become more intricate, the need for accessing different databases has also escalated. This is precisely where the "Spring Multiple Database" comes into play. This library facilitates the usage of various databases in your Spring Boot-based applications, enhancing flexibility and efficiency.

### Gradle

```gradle
repositories {
    maven{
        url = uri("https://repo.emirman.dev/repository/maven-public/")
    }
}

dependencies {
    implementation 'dev.emirman.util:spring-multiple-database:1.0.0'
}
```

### Enable Multiple Mongo

```java
@EnableMultipleMongo
@SpringBootApplication
public class MultipleDBDemo {
    public static void main(String[] args) {
        SpringApplication.run(MultipleDBDemo.class, args);
    }
}
```

#### Configuration
```yaml
spring:
  multiple:
    database:
      header:
        name: X-Data-Source
  data:
    mongodb:
      host: localhost
      port: 27017
      database: demo # Using default dababase 'demo' for mongodb
      username: root
      password: root
      authentication-database: admin
```

### Enable Multiple JPA

```java
@EnableMultipleJPA
@SpringBootApplication
public class MultipleDBDemo {
    public static void main(String[] args) {
        SpringApplication.run(MultipleDBDemo.class, args);
    }
}
```

#### Configuration
```yaml
spring:
  multiple:
    database:
      header:
        name: X-Data-Source
  datasource:
    username: root
    password: root
    url: jdbc:postgresql://localhost:5432/demo # Using default dababase 'demo' for jpa
  jpa:
    generate-ddl: true
    hibernate:
      ddl-auto: update
```

## Example cURL
```curl
curl --location --request POST 'http://localhost:4000/demo' \
--header 'X-Data-Source: demo-1'
```
