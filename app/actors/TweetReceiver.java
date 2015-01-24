package actors;

import actors.messages.Start;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import play.Configuration;
import play.Play;
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

    private Void start(Start objStart) {
        Start start = objStart;
        if (twitterStream == null || !twitterStream.getAuthorization().isEnabled()) {
            authenticate();
        }

        twitterStream.addListener(buildListener());

        // filter() method internally creates a thread which manipulates TwitterStream
        // and calls these adequate listener methods continuously.
        twitterStream.filter(new FilterQuery().track(new String[] {start.getHashTag()}));

        return null;
    }

    private void authenticate() {
        Configuration configuration = Play.application().configuration();
        twitterStream = TwitterStreamFactory.getSingleton();
        AccessToken accessToken = new AccessToken(configuration.getString("twitter.oauth.accessToken"),
                configuration.getString("twitter.oauth.accessTokenSecret"));

        twitterStream.setOAuthConsumer(configuration.getString("twitter.oauth.consumerKey"),
                configuration.getString("twitter.oauth.consumerSecret"));

        twitterStream.setOAuthAccessToken(accessToken);
    }

    private StatusListener buildListener() {
        return new StatusListener() {
            @Override
            public void onStatus(Status status) {
                log.info("@" + status.getUser().getScreenName() + " - " + status.getText());
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
