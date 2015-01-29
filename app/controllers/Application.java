package controllers;

import actors.TweetPublisher;
import akka.actor.ActorRef;
import akka.actor.Props;
import org.springframework.beans.factory.annotation.Autowired;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.TweetService;

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
        tweetService.searchBy(hashTag, lang);
        return ok("I got! tks");
    }

    public static WebSocket<String> wsTweets() {
        return WebSocket.withActor(new F.Function<ActorRef, Props>() {
            public Props apply(ActorRef out) throws Throwable {
                return TweetPublisher.props(out);
            }
        });
    }

}