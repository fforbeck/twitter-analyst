package actors;

import actors.messages.Read;
import actors.messages.Start;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import components.TweetConverter;
import play.Configuration;
import play.Play;
import redis.clients.jedis.Jedis;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.util.List;

/**
 * Created by fforbeck on 24/01/15.
 *
 * Call the Twitter API using twitter4j driver to perform some queries using the hashTag and language.
 * It receives as response different chunks of twitter status where we extract the relevant content
 * and send it the redis queue. Later this tweets in the queue will be consumed by TweetAnalyzer.
 * The supervisor is advised that there is a new tweet in the queue.
 */
public class TweetHarvester extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private Twitter twitter;

    private String queueName;
    private String redisHost;
    private String oauthAccessToken;
    private String oauthAccessTokenSecret;
    private String consumerKey;
    private String consumerSecret;

    /**
     * Loads the properties from application.conf file before start and receive messages.
     *
     * @throws Exception
     */
    @Override
    public void preStart() throws Exception {
        super.preStart();
        Configuration configuration = Play.application().configuration();
        queueName = configuration.getString("redis.processing.queue");
        redisHost = configuration.getString("redis.host");
        oauthAccessToken = configuration.getString("twitter.oauth.accessToken");
        oauthAccessTokenSecret = configuration.getString("twitter.oauth.accessTokenSecret");
        consumerKey = configuration.getString("twitter.oauth.consumerKey");
        consumerSecret = configuration.getString("twitter.oauth.consumerSecret");
    }

    @Override
    public void onReceive(Object objMsg) throws Exception {
        if (objMsg instanceof Start) {
            connect();
            Start message = (Start) objMsg;
            harvest(message);
        } else {
            unhandled(objMsg);
        }
    }

    /**
     * Authenticates when there is no twitter instance or authorization.
     * If the access token had already been configured it catches the exception, logs
     * and continue with the flow.
     *
     * @throws Exception
     */
    private void connect() throws Exception {
        log.info("Authenticate Tweet Harvester.");

        if (twitter != null && twitter.getAuthorization().isEnabled()) {
            log.warning("Already connected to twitter.");
            return;
        }

        try {
            twitter = TwitterFactory.getSingleton();
            AccessToken accessToken = new AccessToken(oauthAccessToken, oauthAccessTokenSecret);
            twitter.setOAuthConsumer(consumerKey, consumerSecret);
            twitter.setOAuthAccessToken(accessToken);
            log.info("Tweet Harvester authenticated with success.");
        } catch (IllegalStateException e) {
            log.error("Something went wrong or Already authenticated: " + e.getMessage());
        }
    }

    /**
     * Performs the query based on the message that holds the hashTag and language.
     * If there are no results, it will be stopped.
     * When results are found, they will be sent to the redis queue one by one.
     *
     * @param message
     * @throws TwitterException
     */
    private void harvest(Start message) throws TwitterException {

        Query query = buildQuery(message);

        QueryResult result;
        do {
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            if (tweets == null) {
                log.info("No tweets found.");
                break;
            }
            log.info("Found " + tweets.size() + " tweets for " + message.getHashTag());
            enqueue(tweets, message.getHashTag());
        } while ((query = result.nextQuery()) != null);
    }

    /**
     * Builds the twitter api query.
     *
     * @param message
     * @return query
     */
    private Query buildQuery(Start message) {
        Query query = new Query();
        query.setQuery(message.getHashTag().replaceAll(",", " OR "));
        query.setLang(message.getLang());
        return query;
    }

    /**
     * Push the tweets one by one into the redis queue after convert the statuses
     * into JSONObject to extract the relevant data.
     * If the conversion fails, the status will be ignored and we will continue with the next one.
     * The TweetSupervisor is advised that there are new tweets in the queue.
     *
     * @param tweets
     * @param hashTag
     */
    private void enqueue(List<Status> tweets, String hashTag) {
        TweetConverter tweetConverter = new TweetConverter();
        Jedis jedis = null;
        try {
            jedis = new Jedis(redisHost);
            for (Status status : tweets) {
                org.json.simple.JSONObject tweetJson = convert(tweetConverter, status, hashTag);
                if (tweetJson != null) {
                    jedis.rpush(queueName, tweetJson.toJSONString());
                    tellSupervisor(hashTag);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * Convert the tweet4j status to simple.JSONObject.
     *
     * @param tweetConverter
     * @param status
     * @param hashTag
     * @return tweetJson
     */
    private org.json.simple.JSONObject convert(TweetConverter tweetConverter, Status status, String hashTag) {
        org.json.simple.JSONObject tweetJson;
        try {
            tweetJson = tweetConverter.convert(status, hashTag);
        } catch (Exception e) {
            log.error("Conversion error - tweet will be ignored: " + e.getMessage(), e);
            tweetJson = null;
        }
        return tweetJson;
    }

    /**
     * Advises the TweetSupervisor that there is new tweet in the queue.
     *
     * @param hashTag
     */
    private void tellSupervisor(String hashTag) {
        getContext().system()
                .actorFor("user/" + TweetSupervisor.class.getSimpleName())
                .tell(new Read(hashTag), getSelf());
    }

}
