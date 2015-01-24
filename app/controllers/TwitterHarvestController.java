package controllers;

import org.springframework.beans.factory.annotation.Autowired;
import play.mvc.*;
import play.mvc.Controller;
import services.TwitterHarvestService;
import views.html.*;


import java.util.concurrent.TimeUnit;

@org.springframework.stereotype.Controller
public class TwitterHarvestController extends Controller {

    @Autowired
    private TwitterHarvestService twitterHarvestService;

    public static Result index() {
        return ok(index.render("Your twitter harvest controller is ready."));
    }


    /**
     * Controller action that constructs a MyMessage and sends it to our local
     * Hello, World actor
     *
     * @param hashTag          The hash tag to search on twitter
     * @return               The promise of a Result
     */
//    public static F.Promise<Result> search(String hashTag) {
//        // Look up the actor
//        ActorSelection twitterHarvesterActor =
//                actorSystem.actorSelection( "user/SimpleTwitterHarvestActor" );
//
//        // Connstruct our message
//        Message message = new Message(hashTag);
//
//        // As the actor for a response to the message (and a 30 second timeout);
//        // ask returns an Akka Future, so we wrap it with a Play Promise
//        return F.Promise.wrap(
//                ask(twitterHarvesterActor, message, 30000)).map(
//                new F.Function<Object, Result>() {
//                    public Result apply(Object response) {
//                        if( response instanceof Message ) {
//                            Message message = ( Message )response;
//                            return ok(play.libs.Json.toJson(message.getTweets())).as("json");
//                        }
//                        return notFound( "Message is not of type MyMessage" );
//                    }
//                }
//        );
//    }
}