package services;

import actors.Message;
import actors.RealtimeTwitterReceiverActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.springframework.stereotype.Service;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Result;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by fforbeck on 24/01/15.
 */
@Service
public class TwitterHarvestServiceImpl implements TwitterHarvestService {

    private ActorSystem actorSystem;

    @PostConstruct
    @Override
    public void postConstruct() {
        actorSystem = ActorSystem.create( "play" );
        // Create our local actors
        //actorSystem.actorOf( Props.create( SimpleTwitterHarvestActor.class ), "SimpleTwitterHarvestActor" );
        //ActorRef twitterHarvesterActor = actorSystem.actorFor("user/SimpleTwitterHarvestActor");
//        Akka.system().scheduler().schedule(
//                FiniteDuration.apply(0, TimeUnit.MILLISECONDS), //Initial delay 0 milliseconds
//                FiniteDuration.apply(15, TimeUnit.MINUTES),     //Frequency 15 minutes
//                twitterHarvesterActor,
//                new Message("$HPQ"),
//                Akka.system().dispatcher(),
//                null
//        );

        actorSystem.actorOf( Props.create(RealtimeTwitterReceiverActor.class), "RealtimeTwitterReceiverActor" );
        ActorRef twitterHarvesterActor = actorSystem.actorFor("user/RealtimeTwitterReceiverActor");
        Akka.system().scheduler().scheduleOnce(
                FiniteDuration.apply(5, TimeUnit.MILLISECONDS),
                twitterHarvesterActor,
                new Message("$HPQ"),
                Akka.system().dispatcher(),
                null
        );

    }

}
