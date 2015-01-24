package controllers;

import models.Tweet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.TweetRepository;
import services.TwitterHarvestService;

import java.util.Date;

@org.springframework.stereotype.Controller
public class Application extends Controller {

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private TwitterHarvestService twitterHarvestService;

    public Application() {
    }

    public Application(final TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result index() {

        final Tweet tweet = new Tweet();
        tweet.user_id = "homer";
        tweet.data = "I really like this beer!! :)";
        tweet.tag = "duffysBeer";
        tweet.created_on = new Date();
        tweet.sentiment = 2.0;

        final Tweet savedTweet = tweetRepository.save(tweet);

        final Tweet retrievedTweet  = tweetRepository.findOne(savedTweet.id);

        return  ok(views.html.index.render("Found one tweet: " + retrievedTweet));
    }
    
}