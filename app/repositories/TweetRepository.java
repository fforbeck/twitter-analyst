package repositories;

import models.Tweet;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Created by fforbeck on 24/01/15.
 *
 * Spring Data Repository which provide a set of actions to be executed on Tweet model.
 *
 */
@Repository
public interface TweetRepository extends CrudRepository<Tweet, Long> {

    List<Tweet> findBySentimentOrderByCreatedAtAsc(String sentiment);

}