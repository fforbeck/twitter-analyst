package actors;

import actors.messages.Read;
import actors.messages.Start;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.json.simple.JSONObject;
import play.Configuration;
import play.Play;
import redis.clients.jedis.Jedis;
import twitter4j.*;
import twitter4j.auth.AccessToken;

/**
 * Created by fforbeck on 24/01/15.
 */
public class TweetReceiver extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private TwitterStream twitterStream;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {
            start((Start) message);
        } else {
            unhandled(message);
        }
    }

    private void start(Start objStart) {
        Start start = objStart;
        if (twitterStream == null || !twitterStream.getAuthorization().isEnabled()) {
            authenticate();
        }
        twitterStream.addListener(buildListener(start.getHashTag()));

        FilterQuery filterQuery = new FilterQuery()
        .track(start.getHashTag().split(","));

        // internally creates a thread which manipulates TwitterStream
        // and calls these adequate listener methods continuously.
        twitterStream.filter(filterQuery);
    }

    private void authenticate() {
        Configuration configuration = Play.application().configuration();
        twitterStream = TwitterStreamFactory.getSingleton();
        AccessToken accessToken = new AccessToken(configuration.getString("twitter.oauth.accessToken"),
                configuration.getString("twitter.oauth.accessTokenSecret"));

        try {
            twitterStream.setOAuthConsumer(configuration.getString("twitter.oauth.consumerKey"),
                    configuration.getString("twitter.oauth.consumerSecret"));

            twitterStream.setOAuthAccessToken(accessToken);
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
        }
    }

    private StatusListener buildListener(final String hashTag) {
        return new StatusListener() {
            @Override
            public void onStatus(Status status) {
                JSONObject tweet = buildTweet(status, hashTag);

                try {
                    Jedis jedis = new Jedis(Play.application().configuration().getString("redis.host"));
                    jedis.lpush("tweets-queue", tweet.toString());
                    jedis.close();
                    getContext().system().actorFor("user/" + TweetSupervisor.class.getSimpleName()).tell(new Read(hashTag), getSelf());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            private JSONObject buildTweet(Status status, String hashTag) {
                JSONObject tweet = new JSONObject();
                tweet.put("user_id", status.getUser().getId());
                tweet.put("user_name", status.getUser().getScreenName());
                tweet.put("text", status.getText());
                tweet.put("created_at", status.getCreatedAt().toString());
                tweet.put("hash_tag", hashTag);
                return tweet;
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                log.info("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                log.info("Got stall warning:" + warning);
            }

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
