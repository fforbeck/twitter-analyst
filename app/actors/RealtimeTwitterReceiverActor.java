package actors;

import akka.actor.UntypedActor;
import play.Configuration;
import play.Play;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fforbeck on 24/01/15.
 */
public class RealtimeTwitterReceiverActor extends UntypedActor  {

    private Twitter twitter;

    @Override
    public void onReceive(Object objMessage) throws Exception {

        if (objMessage instanceof Message) {
            Message message = (Message) objMessage;
            if (twitter != null && twitter.getAuthorization().isEnabled()) {
                return;
            }

            Configuration configuration = Play.application().configuration();
            TwitterStream twitterStream = TwitterStreamFactory.getSingleton();
            AccessToken accessToken = new AccessToken(configuration.getString("twitter.oauth.accessToken"),
                    configuration.getString("twitter.oauth.accessTokenSecret"));

            twitterStream.setOAuthConsumer(configuration.getString("twitter.oauth.consumerKey"),
                    configuration.getString("twitter.oauth.consumerSecret"));

            twitterStream.setOAuthAccessToken(accessToken);
            StatusListener startListener = buildListener();
            twitterStream.addListener(startListener);

            // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
            twitterStream.filter(new FilterQuery().track(new String[] {message.getHashTag()}));
        }

    }

    private StatusListener buildListener() {
        return new StatusListener() {
            @Override
            public void onStatus(Status status) {
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
    }
}
