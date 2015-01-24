package actors;

import actors.messages.Read;
import actors.messages.Start;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import play.Configuration;
import play.Play;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.util.List;

/**
 *
 */
public class TweetHarvester extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private Twitter twitter;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {
            connect();
            harvest(((Start) message).getHashTag());
        } else {
            unhandled(message);
        }
    }

    private void connect() throws TwitterException {
        if (twitter != null && twitter.getAuthorization().isEnabled()) {
            return;
        }
        Configuration configuration = Play.application().configuration();
        twitter = TwitterFactory.getSingleton();

        AccessToken accessToken = new AccessToken(configuration.getString("twitter.oauth.accessToken"),
                configuration.getString("twitter.oauth.accessTokenSecret"));

        twitter.setOAuthConsumer(configuration.getString("twitter.oauth.consumerKey"),
                configuration.getString("twitter.oauth.consumerSecret"));

        twitter.setOAuthAccessToken(accessToken);
    }

    private void harvest(String hashTag) throws TwitterException {
        Query query = new Query(hashTag);
        QueryResult result;
        do {
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            if (tweets == null) {
                log.info("No tweets found.");
                break;
            }
            log.info("Found " + tweets.size() + " tweets.");
            //printTweets(tweets);
            //start.setTweets(tweets);
            //put it into rabbit mq
            sender().tell(new Read(hashTag), self());
        } while ((query = result.nextQuery()) != null);
    }

    private void printTweets(List<Status> tweets) {
        for (Status tweet : tweets) {
            log.info("@" + tweet.getUser().getScreenName() +
                    " - " + tweet.getText(),
                    " - Date " + tweet.getCreatedAt().toString());
        }
    }

}