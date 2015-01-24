package actors;

import actors.messages.Read;
import actors.messages.Start;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Created by fforbeck on 24/01/15.
 */
public class TweetSupervisor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {

        } else if (message instanceof Read) {

        } else {
            unhandled(message);
        }
    }

    private void read(Read msg) {

    }
}
