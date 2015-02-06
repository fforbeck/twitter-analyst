package actors;

import actors.messages.Read;
import actors.messages.Start;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import components.TweetConverter;
import org.json.simple.JSONObject;
import play.Configuration;
import play.Play;
import redis.clients.jedis.Jedis;
import twitter4j.*;
import twitter4j.auth.AccessToken;

/**
 * Created by fforbeck on 24/01/15.
 *
 * Connects to the twitter API using twitter4j driver and open a stream listener to receive twitter status
 * related to the filters: hashTag and language.
 * Each new status is parsed to extract the relevant content and after that it is sent to the redis queue, where
 * it will be consumed by TweetAnalyzer later. The supervisor is advised that there is a new tweet in the queue.
 */
public class TweetReceiver extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private TwitterStream twitterStream;

    private String processingQueue;
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
        processingQueue = configuration.getString("redis.processing.queue");
        redisHost = configuration.getString("redis.host");
        oauthAccessToken = configuration.getString("twitter.oauth.accessToken");
        oauthAccessTokenSecret = configuration.getString("twitter.oauth.accessTokenSecret");
        consumerKey = configuration.getString("twitter.oauth.consumerKey");
        consumerSecret = configuration.getString("twitter.oauth.consumerSecret");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {
            start((Start) message);
        } else {
            unhandled(message);
        }
    }

    /**
     * Authenticates when there is no stream or authorization.
     * Builds the stream and listener based on the message that holds the hashTag
     * and lang to perform the filter.
     *
     * @param message
     */
    private void start(Start message) {
        if (twitterStream == null || !twitterStream.getAuthorization().isEnabled()) {
            authenticate();
        }
        twitterStream.addListener(buildListener(message));

        FilterQuery filterQuery = new FilterQuery()
        .track(message.getHashTag().split(","))
        .language(message.getLang().split(","));

        // internally creates a thread which manipulates TwitterStream
        // and calls these adequate listener methods continuously.
        twitterStream.filter(filterQuery);
    }

    /**
     * Open a stream connection with twitter api and if the access token had already been configured
     * will catch the exception and continue with the execution.
     */
    private void authenticate() {
        log.info("Authenticate Tweet Receiver.");
        twitterStream = TwitterStreamFactory.getSingleton();
        try {
            AccessToken accessToken = new AccessToken(oauthAccessToken, oauthAccessTokenSecret);
            twitterStream.setOAuthConsumer(consumerKey, consumerSecret);
            twitterStream.setOAuthAccessToken(accessToken);
            log.info("Tweet Receiver authenticated with success.");
        } catch (IllegalStateException e) {
            log.error("Something went wrong or Already authenticated: " + e.getMessage());
        }
    }

    /**
     * Defines the listener that will listen to the twitter stream API. Will be called when
     * some status matches with our filter.
     * @param message
     * @return statusListener
     */
    private StatusListener buildListener(final Start message) {
        return new StatusListener() {
            /**
             * Receives an status based on the filter previously defined.
             *
             * @param status
             */
            @Override
            public void onStatus(Status status) {
                JSONObject tweetJson = new TweetConverter().convert(status, message.getHashTag());
                push(tweetJson);
                tellSupervisor();
            }

            /**
             * Open a connection with Jedis to push the message in Redis queue.
             * @param tweetJson
             */
            private void push(JSONObject tweetJson) {
                Jedis jedis = null;
                try {
                    jedis = new Jedis(redisHost);
                    jedis.rpush(processingQueue, tweetJson.toJSONString());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            }

            /**
             * Advises the TweetSupervisor that there is new tweet in the queue.
             */
            private void tellSupervisor() {
                getContext().system()
                        .actorFor("user/" + TweetSupervisor.class.getSimpleName())
                        .tell(new Read(message.getHashTag()), getSelf());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                log.info("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onStallWarning(StallWarning warning) {}

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {}

            @Override
            public void onException(Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        };
    }


}
