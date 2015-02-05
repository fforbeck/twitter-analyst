package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import play.Configuration;
import play.Play;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by fforbeck on 24/01/15.
 *
 * This actor is responsible for creating store the actor reference called "out" which
 * is used to communicate with the client.
 * When this actor receives a String message "pump" it will subscribe a redis channel "live-tweets-channel"
 * in order to send to the client live tweets updates.
 * The redis channel is populated by the TweetReceiver Actor.
 */
public class TweetPublisher extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private String redisHost;
    private String tweetsChannel;
    private boolean stopPublisher;

    /**
     *  Static factory method with receives an 'out' instance that represents the websocket connection
     *  with the client.
     */
    public static Props props(ActorRef out) {
        return Props.create(TweetPublisher.class, out);
    }

    private final ActorRef out;

    public TweetPublisher(ActorRef out) {
        this.out = out;
    }

    /**
     * Loads the properties from application.conf file before start and receive messages.
     *
     * @throws Exception
     */
    @Override
    public void preStart() {
        Configuration configuration = Play.application().configuration();
        redisHost = configuration.getString("redis.host");
        tweetsChannel = configuration.getString("redis.tweets.channel");
    }

    /**
     * Unsubscribe the tweets channel after finishes the actor execution.
     *
     * @throws Exception
     */
    @Override
    public void postStop() throws Exception {
        jedisPubSub.unsubscribe(tweetsChannel);
    }

    /**
     * Each received message equals 'pump' we subscribe a redis channel
     * to listen to new tweets messages and publish them in the websocket connection.
     * We use one single instance of this actor for each websocker connection.
     *
     * @param message
     * @throws Exception
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String && "pump".equalsIgnoreCase((String)message)) {
            // out.tell("Alright Dude, I got your message: " + message, self());
            Jedis jedis = new Jedis(redisHost);
            // listening for msgs
            jedis.subscribe(jedisPubSub, tweetsChannel);
        } else {
            unhandled(message);
        }
    }

    /**
     * Creates a redis pubSub object which is responsible for listening all the messages
     * that are sent to the channel.
     */
    private final JedisPubSub jedisPubSub = new JedisPubSub() {

        /**
         * For each new message that comes from the channel we sent it to
         * the client directly. It is a tweet in json format.
         * @param channel
         * @param message
         */
        @Override
        public void onMessage(String channel, String message) {
            log.info("New tweet received");
            out.tell(message, self());
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {}

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {}

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {}

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {}

        @Override
        public void onPMessage(String pattern, String channel, String message) { }

    };

}