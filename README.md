# spring-jpa-customer-contacts
This is a helloworld style Spring Data JPA implementation of a uni-directional OneToMany relationship between customer and contact entities. Embedded H2 DB is used for persistence.

## Build and Test
```
.\gradlew clean build

.\gradlew test
```

## Build and Run Swagger UI
All CRUD APIs for the two entities can be tested using the Swagger UI.
```
.\gradlew bootRun

http://localhost:8080/swagger-ui/index.html
```

## Browse DB Tables
```
.\gradlew bootRun

http://localhost:8080/h2-console
```

## Related Projects

ManyToMany relationships modeled using Bi-Directional OneToMany relationships:<br>
https://github.com/ns-code/spring-jpa-courseenrollments

Bi-Directional ManyToMany relationships modeled using compsite keys:<br>
https://github.com/ns-code/spring-jpa-courseenrollments-2
