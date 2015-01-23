package repositories;

import models.Tweet;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TweetRepository extends CrudRepository<Tweet, Long> {
}