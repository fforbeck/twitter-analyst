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
 *
 * This actor is reponsible for orchestrate all the calls and communications between other
 * actors in the system. It is a kind of manager which starts the TweetHarvester and TweetReceiver actors
 * and handle their messages.
 */
public final class TweetSupervisor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private ActorRef tweetReceiverActor;
    private ActorRef tweetHarvesterActor;
    private Long tweetAnalyzerActorId = 0L;

    /**
     * Define the failure strategy, in this case is OneForOne it means that
     * if one actor fails only it will be restarted, not the full actor system.
     */
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
           startRealtimeTweetReceiver(message.getHashTag(), message.getLang());

        } else if (objMsg instanceof Read) {
            startTweetAnalyzer(((Read) objMsg).getHashTag());
        } else {
            unhandled(objMsg);
        }
    }

    /**
     * Starts the Harvester Actor sending a start message
     * and schedule it to run every 15 minutes to search by
     * the parameters hashTag and lang.
     * @param hashTag
     * @param lang
     */
    private void startSimpleTweetHarvester(String hashTag, String lang) {
        log.info("Starting tweet harvester for tag " + hashTag+ ", lang: " + lang);

        final String actorName = TweetHarvester.class.getSimpleName();
        if (tweetReceiverActor == null) {
            getContext().system().actorOf(Props.create(TweetHarvester.class), actorName);
        }
        tweetHarvesterActor = getContext().system().actorFor("user/" + actorName);
        getContext().system().scheduler().schedule(
                FiniteDuration.apply(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                FiniteDuration.apply(15, TimeUnit.MINUTES),     //Frequency 15 minutes
                tweetHarvesterActor,
                new Start(hashTag, lang),
                getContext().dispatcher(),
                getSelf()
        );

        log.info("Tweet harvester started for tag " + hashTag);
    }

    /**
     * Starts the Receiver actor sending a start message
     * to listen the twitter stream API using the parameters hashTag and Lang.
     * @param hashTag
     * @param lang
     */
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

    /**
     * Starts the Analyzer sending a read message to grab the tweets from
     * redis queue and send each one of them to the sentiment analysis API.
     *
     * @param hashTag
     */
    private void startTweetAnalyzer(String hashTag) {
        log.info("Starting tweet analyzer for " + hashTag);

        final String actorName = TweetAnalyzer.class.getSimpleName();
        ActorRef tweetAnalyzerActor = getContext().system()
                .actorOf(Props.create(TweetAnalyzer.class), actorName + (++tweetAnalyzerActorId));

        tweetAnalyzerActor.tell(new Read(hashTag), self());

        log.info("Tweet analyzer started for tag " + hashTag);
    }


}
