package repositories;

import models.Tweet;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Created by fforbeck on 24/01/15.
 *
 *
 */
@Repository
public interface TweetRepository extends CrudRepository<Tweet, Long> {

    List<Tweet> findBySentiment(String sentiment, Sort sort);

}