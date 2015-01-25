package controllers;

import models.Tweet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import repositories.TweetRepository;
import services.TwitterAnalysisService;

import java.util.Date;

@org.springframework.stereotype.Controller
public class Application extends Controller {

    @Autowired
    private TwitterAnalysisService twitterAnalysisService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Result index() {

        Iterable<Tweet> allTweets = twitterAnalysisService.findAllTweets();


        return  ok(views.html.index.render("Found one tweet: " + allTweets)).as("json");
    }
    
}