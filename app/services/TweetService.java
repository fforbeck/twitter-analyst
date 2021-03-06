package services;

import models.Tweet;

import java.util.List;


/**
 * Created by fforbeck on 24/01/15.
 */
public interface TweetService {

    void postConstruct();

    Iterable<Tweet> findAll();

    List<Tweet> findBySentiment(String sentiment);

}
