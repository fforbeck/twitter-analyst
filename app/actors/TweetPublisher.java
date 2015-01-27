package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import play.Play;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class TweetPublisher extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private String redisHost;
    private String redisChannelName;
 
    public static Props props(ActorRef out) {
        return Props.create(TweetPublisher.class, out);
    }

    private final ActorRef out;

    public TweetPublisher(ActorRef out) {
        this.out = out;
    }

    @Override
    public void preStart() {
        redisHost = Play.application().configuration().getString("redis.host");
        redisChannelName = "live-tweets-channel";
        setupPublisher();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            out.tell("I received your message: " + message, self());
            Jedis jedis = new Jedis(redisHost);
            jedis.subscribe(jedisPubSub, redisChannelName);
            jedis.quit();
        }
    }

    public static class RegistrationMessage {
        public RegistrationMessage() {
        }
    }

    public static class UnregistrationMessage {
        public String id;

        public UnregistrationMessage(String id) {
            super();
            this.id = id;
        }
    }

    private final JedisPubSub jedisPubSub = new JedisPubSub() {
        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            log.info("onUnsubscribe");
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            log.info("onSubscribe");
        }

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
        }

        @Override
        public void onMessage(String channel, String message) {
            log.info("New tweet received");
            out.tell(message, self());
        }
    };

    private void setupPublisher() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        System.out.println("Connecting");
                        Jedis jedis = new Jedis(redisHost);
                        System.out.println("Waiting to publish");
                        System.out.println("Ready to publish, waiting one sec");
                        Thread.sleep(1000);
                        System.out.println("publishing");
                        double y = Math.random() * (Math.random() > 0.5 ? 10 : -10);
                        long x = System.currentTimeMillis();
                        jedis.publish(redisChannelName, "{x: " + x + ", y:" + y + ", tweet:\"Yay!  Living Play is out for Early Release!\"}");
                        System.out.println("published, closing publishing connection");
                        jedis.quit();
                        System.out.println("publishing connection closed");
                    }
                } catch (Exception e) {
                    System.out.println(">>> OH NOES Pub, " + e.getMessage());
// e.printStackTrace();
                }
            }
        }, "publisherThread").start();
    }

}