package neo4j.models;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="LIKES")
public class RatingRelation {
	
	@GraphId
	private Long id;
	@StartNode
	Person person;
	@EndNode
	Person other;	
	int stars;
	String comment;

	public RatingRelation () {
		
	}

	public RatingRelation (Person person, Person other) {
		this.person = person;
		this.other = other;
	}

	public RatingRelation likeRating (int stars, String comment) {
		this.stars = stars;
		this.comment = comment;
		return this;
	}
	
}
