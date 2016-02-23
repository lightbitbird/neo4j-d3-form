package neo4j.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;

import neo4j.models.Person;

public interface PersonRepository extends GraphRepository<Person> {
	
	Person findByEmail(String email); 
}
