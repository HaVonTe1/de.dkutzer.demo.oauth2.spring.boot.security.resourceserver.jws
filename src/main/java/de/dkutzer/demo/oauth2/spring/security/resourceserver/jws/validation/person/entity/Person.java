package de.dkutzer.demo.oauth2.spring.security.resourceserver.jws.validation.person.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Person {
    private String firstname;
    private String lastname;
    private int age;

}
