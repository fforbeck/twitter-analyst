package actors;

import actors.messages.Read;
import actors.messages.Start;
import akka.actor.*;
import akka.actor.SupervisorStrategy.Directive;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.stop;

/**
 *
 */
public final class TweetSupervisor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private ActorRef tweetReceiverActor;
    private ActorRef tweetHarvesterActor;
    private ActorRef tweetAnalyzerActor;

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

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {
            //startSimpleTweetHarvester(((Start) message).getHashTag());
            startRealtimeTweetReceiver(((Start) message).getHashTag());
        } else if (message instanceof Read) {
            startTweetAnalyzer(((Read) message).getHashTag());
        } else {
            unhandled(message);
        }
    }

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
        tweetReceiverActor.tell(new Start(hashTag), self());

        log.info("Tweet stream receiver started for tag " + hashTag);
    }

    private void startTweetAnalyzer(String hashTag) {
        log.info("Starting tweet analyzer for " + hashTag);

        final String actorName = TweetAnalyzer.class.getSimpleName();
        if (tweetAnalyzerActor == null) {
            getContext().system().actorOf(Props.create(TweetAnalyzer.class), actorName);
        }
        tweetAnalyzerActor = getContext().system().actorFor("user/" + actorName);
        tweetAnalyzerActor.tell(new Read(hashTag), self());

        log.info("Tweet analyzer started for tag " + hashTag);
    }


}
