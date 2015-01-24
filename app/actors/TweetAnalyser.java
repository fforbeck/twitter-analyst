package actors;

import actors.messages.Read;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import twitter4j.Status;

import java.util.List;

/**
 * Created by fforbeck on 24/01/15.
 */
public class TweetAnalyser extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Read) {
            List<Status> tweets = read((Read) message);
            analyseSentimentFrom(tweets);
        } else {
            unhandled(message);
        }
    }

    private List<Status> read(Read msg) {
        log.info("Reading tweet from queue for tag " + msg.getHashTag());

        return null;
    }

    private void analyseSentimentFrom(List<Status> tweets) {
        log.info("Analysing sentiments from tweets");

    }

}
