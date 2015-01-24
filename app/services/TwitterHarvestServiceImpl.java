package services;

import actors.messages.Start;
import actors.TweetReceiver;
import actors.TweetHarvester;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.springframework.stereotype.Service;
import play.libs.Akka;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by fforbeck on 24/01/15.
 */
@Service
public class TwitterHarvestServiceImpl implements TwitterHarvestService {

    private ActorSystem actorSystem;

    @Override
    @PostConstruct
    public void postConstruct() {
        actorSystem = ActorSystem.create("tweetAnalystSystem");
        // Create our local actors
        final String hashTag = "#UFCStockholm";
        startSimpleTweetHarvester(hashTag);
        startRealtimeTweetReceiver(hashTag);

    }

    private void startSimpleTweetHarvester(String hashTag) {
        actorSystem.actorOf( Props.create( TweetHarvester.class ), "TweetHarvester" );
        ActorRef twitterHarvesterActor = actorSystem.actorFor("user/TweetHarvester");
        Akka.system().scheduler().schedule(
                FiniteDuration.apply(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
                FiniteDuration.apply(15, TimeUnit.MINUTES),     //Frequency 15 minutes
                twitterHarvesterActor,
                new Start(hashTag),
                Akka.system().dispatcher(),
                null
        );
    }

    private void startRealtimeTweetReceiver(String hashTag) {
        actorSystem.actorOf( Props.create(TweetReceiver.class), "TweetReceiver" );
        ActorRef twitterHarvesterActor = actorSystem.actorFor("user/TweetReceiver");
        Akka.system().scheduler().scheduleOnce(
                FiniteDuration.apply(5, TimeUnit.MILLISECONDS),
                twitterHarvesterActor,
                new Start(hashTag),
                Akka.system().dispatcher(),
                null
        );
    }

}
