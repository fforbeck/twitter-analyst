package services;

import actors.TweetSupervisor;
import actors.messages.Start;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import models.Tweet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import repositories.TweetRepository;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Service
public class TwitterAnalysisServiceImpl implements TwitterAnalysisService {

    @Autowired
    private TweetRepository tweetRepository;

    private ActorSystem actorSystem;

    private ActorRef tweetSupervisor;

    @Async
    @Override
    @PostConstruct
    public void postConstruct() {
        actorSystem = ActorSystem.create("tweetAnalysisSystem");

        final String hashTag = "#UFC";
        final String actorName = TweetSupervisor.class.getSimpleName();
        tweetSupervisor = actorSystem.actorOf(Props.create(TweetSupervisor.class), actorName);

        System.out.println("Sending hashTag " + hashTag + " to be harvested!");

        actorSystem.scheduler().scheduleOnce(
                FiniteDuration.apply(7, TimeUnit.SECONDS),
                tweetSupervisor,
                new Start(hashTag),
                actorSystem.dispatcher(),
                null
        );
    }

    @Override
    public Iterable<Tweet> findAllTweets() {
        return tweetRepository.findAll();
    }

}
