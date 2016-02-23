package neo4j.models;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;

@NodeEntity
public class Person extends AbstractNode {

	@Indexed
    private String email;

	private String password;

	private String name;

	private String nickname;

	@Fetch
	@RelatedTo(type = "KNOWS", direction = Direction.OUTGOING)
	public Set<Person> personKnows;

	@Fetch
	@RelatedToVia(type = "KNOWS")
	public Set<FriendRelation> friends;
	
	@Fetch
	@RelatedTo(type = "LIKES", direction = Direction.OUTGOING)
	public Set<Person> personLikes;

	@Fetch
	@RelatedToVia(type = "LIKES")
	public Set<RatingRelation> ratings;

	public Person() {}

	public Person(String name, String email, String password, String nickname) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.nickname = nickname;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return password;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getNickname() {
		return nickname;
	}

	public FriendRelation familiarWith (Person other, int stars, String comment) {
		FriendRelation familiar = new FriendRelation(this, other);
		friends.add(familiar.familiarWith(stars, comment));
System.out.println("........................................................................comment = " + familiar.comment);
//		Neo4jTemplate temlate = Neo4jTemplate;
		return familiar;
//		personKnows.add(other);
	}

	public void addToKnowsSet(Person other) {
		if (personKnows == null)
			personKnows = new HashSet<>();
		personKnows.add(other);
	}

	public RatingRelation likesRating(Person other, int stars, String comment) {
		RatingRelation likes = new RatingRelation(this, other);
		ratings.add(likes.likeRating(stars, comment));
		return likes;
//		personKnows.add(other);
	}

	public void addToLikesSet(Person other) {
		if ( personLikes== null)
			personLikes = new HashSet<>();
		personLikes.add(other);
	}

	@Override
	public String toString() {
		return String.format("Person{name='%s', nickname=%s}", name, nickname);
	}

	public enum RelTypes implements RelationshipType {
		KNOWS, LIKES
	}

}
