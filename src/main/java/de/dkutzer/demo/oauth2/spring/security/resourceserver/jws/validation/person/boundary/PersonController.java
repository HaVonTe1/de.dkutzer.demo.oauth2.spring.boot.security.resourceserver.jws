package de.dkutzer.demo.oauth2.spring.security.resourceserver.jws.validation.person.boundary;

import de.dkutzer.demo.oauth2.spring.security.resourceserver.jws.validation.person.entity.Person;
import java.util.Arrays;
import java.util.Collection;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class PersonController {

    @RequestMapping(value = "/person", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<Person> getPersons(){
        Collection<Person> bikes = Arrays.asList(Person.builder().firstname("John").lastname("Lennon").age(123).build());
        return bikes;
    }
}
