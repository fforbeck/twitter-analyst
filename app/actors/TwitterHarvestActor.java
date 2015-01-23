package actors;

import akka.actor.UntypedActor;
import play.Configuration;
import play.Play;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.util.List;

/**
 *
 */
public class TwitterHarvestActor extends UntypedActor {

    private Twitter twitter;

    @Override
    public void onReceive(Object msgObj) throws Exception {
        if (msgObj instanceof  Message) {
            Message message = (Message) msgObj;
            connect(message);
            harvest(message); ;
        } else {
            unhandled(msgObj);
        }
    }

    private void connect(Message message) throws TwitterException {

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

    private void harvest(Message message) {
        try {
            Query query = new Query(message.getHashTag());
            QueryResult result;
            do {
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                for (Status tweet : tweets) {
                    System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                }
                message.setTweets(tweets);
                sender().tell(message, self());
            } while ((query = result.nextQuery()) != null);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
        }
    }

}
