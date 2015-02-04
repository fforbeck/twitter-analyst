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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by fforbeck on 24/01/15.
 *
 * Main service to start the akka actor system and run a scheduled job in order to persist
 * the harvested and analyzed tweets.
 *
 */
@Service
@EnableScheduling
public class TweetServiceImpl implements TweetService  {

    private final Logger log = Logger.getLogger(TweetServiceImpl.class.getSimpleName());

    @Autowired
    private TweetRepository tweetRepository;

    private ActorSystem actorSystem;

    private ActorRef tweetSupervisor;

    private String persistQueue;

    private String redisHost;

    /**
     * Starts the actor system to receive and harvest the tweets using
     * twitter4j api.
     *
     */
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
    public void startHarvestingBy(String hashTag, String lang) {
        tweetSupervisor.tell(new Start(hashTag, lang), null);
    }

    /**
     * Find all tweets in DB
     */
    @Override
    public Iterable<Tweet> findAll() {
        return tweetRepository.findAll();
    }

    /**
     * Find all tweets in DB filtering by sentiment
     */
    @Override
    public List<Tweet> findBySentiment(String sentiment) {
        if ("positive".equalsIgnoreCase(sentiment)
                || "negative".equalsIgnoreCase(sentiment)
                || "neutral".equalsIgnoreCase(sentiment)) {

            return tweetRepository.findBySentiment(sentiment.toLowerCase());
        }

        return Collections.emptyList();
    }

    /**
     * Scheduled job to load the tweets from redis queue to be persisted
     * in the Vertica DB.
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
     * Open a new connection with Redis to popup the tweets from the persist queue in chunks of 25.
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
