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
 * Created by fforbeck on 24/01/15.
 */
public final class TweetSupervisor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private ActorRef tweetReceiverActor;
    private ActorRef tweetHarvesterActor;
    private Long tweetAnalyzerActorId = 0L;

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
    public void onReceive(Object objMsg) throws Exception {
        if (objMsg instanceof Start) {
            Start message = (Start) objMsg;
           // startSimpleTweetHarvester(message.getHashTag(), message.getLang());
           // startRealtimeTweetReceiver(message.getHashTag(), message.getLang());

        } else if (objMsg instanceof Read) {
            // startTweetAnalyzer(((Read) objMsg).getHashTag());
        } else {
            unhandled(objMsg);
        }
    }

    private void startSimpleTweetHarvester(String hashTag, String lang) {
        log.info("Starting tweet harvester for tag " + hashTag+ ", lang: " + lang);

        final String actorName = TweetHarvester.class.getSimpleName();
        if (tweetReceiverActor == null) {
            getContext().system().actorOf(Props.create(TweetHarvester.class), actorName);
        }
        tweetHarvesterActor = getContext().system().actorFor("user/" + actorName);
        getContext().system().scheduler().schedule(
                FiniteDuration.apply(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                FiniteDuration.apply(5, TimeUnit.MINUTES),     //Frequency 15 minutes
                tweetHarvesterActor,
                new Start(hashTag, lang),
                getContext().dispatcher(),
                getSelf()
        );

        log.info("Tweet harvester started for tag " + hashTag);
    }

    private void startRealtimeTweetReceiver(String hashTag, String lang) {
        log.info("Starting tweet stream receiver for tag " + hashTag + ", lang: " + lang);

        final String actorName = TweetReceiver.class.getSimpleName();
        if (tweetReceiverActor == null) {
            getContext().system().actorOf(Props.create(TweetReceiver.class), actorName);
        }
        tweetReceiverActor = getContext().system().actorFor("user/" + actorName);
        tweetReceiverActor.tell(new Start(hashTag, lang), self());

        log.info("Tweet stream receiver started for tag " + hashTag);
    }

    private void startTweetAnalyzer(String hashTag) {
        log.info("Starting tweet analyzer for " + hashTag);

        final String actorName = TweetAnalyzer.class.getSimpleName();
        ActorRef tweetAnalyzerActor = getContext().system()
                .actorOf(Props.create(TweetAnalyzer.class), actorName + (++tweetAnalyzerActorId));

        tweetAnalyzerActor.tell(new Read(hashTag), self());

        log.info("Tweet analyzer started for tag " + hashTag);
    }


}
