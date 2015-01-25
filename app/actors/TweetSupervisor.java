package actors;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;

import actors.messages.Read;
import actors.messages.Start;
import akka.actor.*;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import akka.actor.SupervisorStrategy.Directive;
import akka.japi.Function;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;
import twitter4j.TwitterException;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public final class TweetSupervisor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private ActorRef tweetReceiverActor;
    private ActorRef tweetHarvesterActor;

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {
            startSimpleTweetHarvester(((Start) message).getHashTag());
            startRealtimeTweetReceiver(((Start) message).getHashTag());
        } else if (message instanceof Read) {
            readTweets(((Read) message).getHashTag());
        } else {
            unhandled(message);
        }
    }

    private static SupervisorStrategy strategy = new OneForOneStrategy(10,
            Duration.create("10 second"),
            new Function<Throwable, SupervisorStrategy.Directive>() {
                public Directive apply(Throwable t) {
                    if (t instanceof Exception) {
                        return stop();
                    }
                    return escalate();
                }
            }
    );

    private void startSimpleTweetHarvester(String hashTag) {
        log.info("Starting tweet harvester for tag " + hashTag);

        final String actorName = TweetHarvester.class.getSimpleName();
        if (tweetReceiverActor == null) {
            getContext().system().actorOf(Props.create(TweetHarvester.class), actorName);
        }
        tweetHarvesterActor = getContext().system().actorFor("user/" + actorName);
        tweetHarvesterActor.tell(new Start(hashTag), getSender());
        getContext().system().scheduler().schedule(
                FiniteDuration.apply(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                FiniteDuration.apply(5, TimeUnit.MINUTES),     //Frequency 15 minutes
                tweetHarvesterActor,
                new Start(hashTag),
                getContext().dispatcher(),
                getSelf()
        );

        log.info("Tweet harvester started for tag " + hashTag);
    }

    private void startRealtimeTweetReceiver(String hashTag) {
        log.info("Starting tweet stream receiver for tag " + hashTag);

        final String actorName = TweetReceiver.class.getSimpleName();
        if (tweetReceiverActor == null) {
            getContext().system().actorOf(Props.create(TweetReceiver.class), actorName);
        }
        tweetReceiverActor = getContext().system().actorFor("user/" + actorName);
        tweetReceiverActor.tell(new Start(hashTag), getSender());

        log.info("Tweet stream receiver started for tag " + hashTag);
    }

    private void readTweets(String hashTag) {
        log.info("Reading tweets from queue for hashTag " + hashTag);

    }

}
