package controllers;


import actors.Message;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import play.libs.Akka;
import play.libs.F.Promise;
import play.libs.F.Function;
import play.mvc.*;
import scala.concurrent.duration.FiniteDuration;
import views.html.*;
import static akka.pattern.Patterns.ask;

import actors.TwitterHarvestActor;

import java.util.concurrent.TimeUnit;

public class TwitterActorSystem extends Controller {

    static ActorSystem actorSystem = ActorSystem.create( "play" );

    static {
        // Create our local actors
        actorSystem.actorOf( Props.create( TwitterHarvestActor.class ), "TwitterHarvestActor" );
        ActorRef twitterHarvesterActor =
                actorSystem.actorFor("user/TwitterHarvestActor");
        Akka.system().scheduler().schedule(
                FiniteDuration.apply(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                FiniteDuration.apply(15, TimeUnit.MINUTES),     //Frequency 15 minutes
                twitterHarvesterActor,
                new Message("$HPQ"),
                Akka.system().dispatcher(),
                null
        );
    }

    public static Result index() {
        return ok(index.render("Your actor system is ready."));
    }

    /**
     * Controller action that constructs a MyMessage and sends it to our local
     * Hello, World actor
     *
     * @param hashTag          The hash tag to search on twitter
     * @return               The promise of a Result
     */
    public static Promise<Result> search(String hashTag) {
        // Look up the actor
        ActorSelection twitterHarvesterActor =
                actorSystem.actorSelection( "user/TwitterHarvestActor" );

        // Connstruct our message
        Message message = new Message(hashTag);

        // As the actor for a response to the message (and a 30 second timeout);
        // ask returns an Akka Future, so we wrap it with a Play Promise
        return Promise.wrap(
                ask(twitterHarvesterActor, message, 30000)).map(
                    new Function<Object, Result>() {
                        public Result apply(Object response) {
                            if( response instanceof Message ) {
                                Message message = ( Message )response;
                                return ok(play.libs.Json.toJson(message.getTweets())).as("json");
                            }
                            return notFound( "Message is not of type MyMessage" );
                        }
                    }
        );
    }
}