package neo4j.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import neo4j.models.FriendRelation;
import neo4j.models.Person;
import neo4j.models.Person.RelTypes;
import neo4j.repositories.FriendRelationRepository;
import neo4j.repositories.PersonRepository;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Service;

import play.Logger;

@Service
public class Neo4jD3Service {
	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private FriendRelationRepository friendRelationRepository;

	private Neo4jTemplate template;
	
//	@Autowired(required=true)
//	private void setPersonRepository(PersonRepository personRepository) {
//		this.personRepository = personRepository;
//	}

	public PersonRepository getPersonRepository() {
		return personRepository;
	}

	public FriendRelationRepository getFriendRelationRepository() {
		return friendRelationRepository;
	}

	public long getNumberOfPersons() {
		return personRepository.count();
	}

	public List<Person> getAllPersons() {
		return new ArrayList<Person>(IteratorUtil.asCollection(personRepository.findAll()));
	}

	public Person findByEmail(String email) {
		return personRepository.findByEmail(email);
	}

	public List<Person> getPersonPath(final Person personA, final Person personB) {
		Path path = GraphAlgoFactory.shortestPath(Traversal.expanderForTypes(Person.RelTypes.KNOWS, Direction.OUTGOING).add(Person.RelTypes.KNOWS), 100)
					.findSinglePath(Neo4jServiceProviderImpl.get().template.getNode(personA.getId()), Neo4jServiceProviderImpl.get().template.getNode(personB.getId()));
		if (path == null) {
			return Collections.emptyList();
		}
		return convertNodesToPersons(path);
	}

	private List<Person> convertNodesToPersons(final Path list) {
		final List<Person> result = new LinkedList<Person>();
		for (Node node : list.nodes()) {
			result.add(Neo4jServiceProviderImpl.get().template.load(node, Person.class));
		}
		return result;
	}

	public List<Person> makeSomePersonsAndRelations() {
		Logger.debug("Creating test data set in database.");

		template = Neo4jServiceProviderImpl.get().template;
		List<Person> persons = new ArrayList<Person>();
		persons.add(createPerson("Mercury", "a@test.com", "qwer", "Merc"));
		persons.add(createPerson("Venus", "bb@test.com", "qwer", "en"));
		persons.add(createPerson("Earth", "cc@test.com", "qwer", "Ear"));
		persons.add(createPerson("Mars", "dd@test.com", "qwer", "Mars"));
		persons.add(createPerson("Jupiter", "ee@test.com", "qwer", "Jup"));
		persons.add(createPerson("Saturn", "ff@test.com", "qwer", "Sat"));
		persons.add(createPerson("Uranus", "gg@test.com", "qwer", "Uran"));
		persons.add(createPerson("Neptune", "hh@test.com", "qwer", "Nep"));
		persons.add(createPerson("Alfheimr", "ii@test.com", "qwer", "Alf"));
		persons.add(createPerson("Midgard", "jj@test.com", "qwer", "Mid"));
		persons.add(createPerson("Muspellheim", "kk@test.com", "qwer", "Muspel"));
		persons.add(createPerson("Asgard", "ll@test.com", "qwer", "Asg"));
		persons.add(createPerson("Hel", "mm@test.com", "qwer", "hel"));
System.out.println("person.size() = " + persons.size());

		// Add relations
		for (int i = 0; i < persons.size() - 1; i++) {
			Person person = persons.get(i);
			Person other = persons.get(i + 1);
			FriendRelation friend = template.createRelationshipBetween(person, other, FriendRelation.class, RelTypes.KNOWS.name(), false);
			template.save(friend.familiarWith(2, "so so."));
	}
		Logger.debug("Creating test data done, have fun with it :).");
		return persons;
	}

	private Person createPerson(String email, String password, String name, String nickname) {
//		return new Person(email, password, name, nickname);
		return template.save(new Person(email, password, name, nickname));
//		return personRepository.save(new Person(email, password, name, nickname));
	}

	public FriendRelation createRelation(Person person, Person friend, int stars, String comment) {
        FriendRelation relation = new FriendRelation(person, friend, stars, comment);
        friendRelationRepository.save(relation);
        template = Neo4jServiceProviderImpl.get().template;
		return template.save(relation.familiarWith(stars, comment));
//		return template.save(new FriendRelation(person, friend, stars, comment));
	}
	
}
