package services;

import models.Tweet;


/**
 * Created by fforbeck on 24/01/15.
 */
public interface TweetService {

    void postConstruct();

    Iterable<Tweet> findAllTweets();

    void searchBy(String hashTag, String lang);

}
