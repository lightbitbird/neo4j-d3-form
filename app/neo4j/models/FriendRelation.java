package neo4j.models;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="KNOWS")
public class FriendRelation {
	
	@GraphId
	private Long id;
	@StartNode
	Person person;
	@EndNode
	Person other;	
	int stars;
	String comment;

	public FriendRelation () {
		
	}

	public FriendRelation (Person person, Person other) {
		this.person = person;
		this.other = other;
	}

	public FriendRelation (Person person, Person other, int stars, String comment) {
		this.person = person;
		this.other = other;
		this.stars = stars;
		this.comment = comment;
	}

	public FriendRelation familiarWith (int stars, String comment) {
		this.stars = stars;
		this.comment = comment;
		return this;
	}

	public Long getId() {
		return id;
	}
	
	public int getStars() {
		return stars;
	}
	
	public void setStars(int stars) {
		this.stars = stars;
	}

	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	public Person getOther() {
		return other;
	}
	
	public void setOther(Person other) {
		this.other = other;
	}

	@Override
	public String toString() {
		return String.format("FriendRelation{stars='%d', comment=%s}", stars, comment);
	}

}
