package neo4j.models;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.GraphProperty;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class AbstractNode {
	
	@GraphId
	private Long id;

	@CreatedDate
	@GraphProperty(propertyType = Long.class)
	private Date createDate;

	@LastModifiedDate
	@GraphProperty(propertyType = Long.class)
	private Date lastModifiedDate;
	
	public Long getId() {
		return id;
	}

}
