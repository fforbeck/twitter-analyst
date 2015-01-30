package controllers;

import actors.TweetPublisher;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.Tweet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.TweetService;

import java.util.List;

/**
 * Created by fforbeck on 24/01/15.
 *
 */
@org.springframework.stereotype.Controller
public class Application extends Controller {

    @Autowired
    private TweetService tweetService;

    public static Result index() {
        return  ok(views.html.index.render("Ready."));
    }

    public Result searchBy(String hashTag, String lang) {
        tweetService.startHarvestingBy(hashTag, lang);
        return ok("I got! tks");
    }

    @Transactional(readOnly = true)
    public Result tweets(String sentiment) {
        if (StringUtils.isBlank(sentiment)) {
            return badRequest("invalid parameter " + sentiment);
        }
        List<Tweet> tweetsBySentiment = tweetService.findBy(sentiment.toLowerCase());
        return ok(Json.toJson(tweetsBySentiment));
    }

    public static WebSocket<String> wsTweets() {
        return WebSocket.withActor(new F.Function<ActorRef, Props>() {
            public Props apply(ActorRef out) throws Throwable {
                return TweetPublisher.props(out);
            }
        });
    }

}