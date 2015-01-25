package services;

import models.Tweet;


/**
 * Created by fforbeck on 24/01/15.
 */
public interface TwitterAnalysisService {

    void postConstruct();

    Iterable<Tweet> findAllTweets();
}
