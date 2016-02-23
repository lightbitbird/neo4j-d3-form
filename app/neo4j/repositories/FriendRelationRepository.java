package neo4j.repositories;

import neo4j.models.FriendRelation;

import org.springframework.data.neo4j.repository.GraphRepository;

public interface FriendRelationRepository extends GraphRepository<FriendRelation> {
}
