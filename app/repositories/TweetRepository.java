package repositories;

import models.Tweet;
import org.springframework.context.annotation.Scope;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


/**
 * Created by fforbeck on 24/01/15.
 *
 *
 */
@Repository
@Scope("prototype")
public interface TweetRepository extends CrudRepository<Tweet, Long> {
}