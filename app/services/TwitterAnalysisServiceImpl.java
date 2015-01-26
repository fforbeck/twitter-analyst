package services;

import actors.TweetSupervisor;
import actors.messages.Start;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import components.TweetConverter;
import models.Tweet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import play.Configuration;
import play.Play;
import redis.clients.jedis.Jedis;
import repositories.TweetRepository;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by fforbeck on 24/01/15.
 */
@Service
@EnableScheduling
public class TwitterAnalysisServiceImpl implements TwitterAnalysisService {

    private final Logger log = Logger.getLogger(TwitterAnalysisServiceImpl.class.getSimpleName());

    @Autowired
    private TweetRepository tweetRepository;

    private ActorSystem actorSystem;

    private ActorRef tweetSupervisor;

    private String persistQueue;

    private String redisHost;

    @Async
    @Override
    @PostConstruct
    public void postConstruct() {
        actorSystem = ActorSystem.create("tweetAnalysisSystem");

        Configuration configuration = Play.application().configuration();

        final String hashTag = configuration.getString("tweet.analyst.tags");
        final String lang = configuration.getString("tweet.analyst.langs");

        startTweetSupervisorActor(hashTag, lang);

        persistQueue = configuration.getString("redis.persist.queue");
        redisHost = configuration.getString("redis.host");
    }

    /**
     * Starts the TweetSupervisorActor to orchestrate the tweet harvest process.
     * It is scheduled to start running once after 7 seconds that app starts.
     *
     * @param hashTag
     * @param lang
     */
    private void startTweetSupervisorActor(String hashTag, String lang) {
        final String actorName = TweetSupervisor.class.getSimpleName();
        tweetSupervisor = actorSystem.actorOf(Props.create(TweetSupervisor.class), actorName);

        log.info("Starting Tweet Supervisor for " + hashTag);

        actorSystem.scheduler().scheduleOnce(
                FiniteDuration.apply(7, TimeUnit.SECONDS),
                tweetSupervisor,
                new Start(hashTag, lang),
                actorSystem.dispatcher(),
                null
        );
    }

    @Override
    public Iterable<Tweet> findAllTweets() {
        return tweetRepository.findAll();
    }


    /**
     *
     */
    @Scheduled(fixedRate = 30000l)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistAnalyzedTweets() {
        log.info("Starting scheduled task to persist analyzed tweets...");
        List<String> tweetsStrList = popTweets();
        if (tweetsStrList == null || tweetsStrList.isEmpty()) {
            log.info("There are no tweets to be persisted.");
            return;
        }

        TweetConverter tweetConverter = new TweetConverter();
        for (String tweetStr : tweetsStrList) {
            Tweet tweet = tweetConverter.convert(tweetStr);
            if (tweet == null) {
                continue;
            }
            try {
                tweetRepository.save(tweet);
            } catch (Exception e) {
                log.severe("Error while save tweet " + tweetStr);
            }
        }

    }

    /**
     * Open a new connection with Redis to pop the tweet from the persist queue in chunks of 25 items.
     *
     * @return List<String> tweetList
     */
    private List<String> popTweets() {
        Jedis jedis = null;
        try {
            jedis = new Jedis(redisHost);
            // to read the chunk on 25 tweets
            List<String> tweets = jedis.lrange(persistQueue, 0, 25);
            if (!tweets.isEmpty()) {
                // to remove the chunk from queue
                jedis.ltrim(persistQueue, tweets.size(), -1);
            }
            return tweets;
        } catch (Exception e) {
            log.severe(e.getMessage());
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return null;
    }


}
