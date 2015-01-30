package actors;

import actors.messages.Read;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import play.Configuration;
import play.Play;
import redis.clients.jedis.Jedis;
import repositories.TweetRepository;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Created by fforbeck on 24/01/15.
 *
 * Read tweets from redis queue and call the HP IDOL api to do the sentiment analysis on tweet.
 * Each tweet is returned with a score and it`s saved and put in a redis queue to be persisted later in the
 * Vertica DB by the TweetServiceImpl scheduled job. Beside that, the same tweet is published in
 * a redis channel in order to send it to all subscribers of this channel. In this case, web-clients using
 * socket connections to update the real-time tweet analysis chart.
 *
 */
public class TweetAnalyzer extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private String processingQueue;
    private String persistQueue;
    private String tweetsChannel;
    private String redisHost;
    private String sentimentRequestQuery;

    private TweetRepository tweetRepository;

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
        persistQueue = configuration.getString("redis.persist.queue");
        tweetsChannel = configuration.getString("redis.tweets.channel");
        redisHost = configuration.getString("redis.host");
        sentimentRequestQuery = configuration.getString("hp.idol.analyze.sentiment.uri");
    }

    @Override
    public void onReceive(Object objMessage) throws Exception {
        if (objMessage instanceof Read) {
            JSONObject tweetJson = read((Read) objMessage);
            tweetJson = executeSentimentAnalysis(tweetJson);
            sendToPersistQueue(tweetJson);
            sendToLiveTweetsChannel(tweetJson);
        } else {
            unhandled(objMessage);
        }
    }

    /**
     * Reads the next available tweet in the redis queue and convert it
     * into simple.JSONObject.
     *
     * @param msg
     * @return tweetJson
     * @throws ParseException
     */
    private JSONObject read(Read msg) {
        log.info("Reading tweet from queue for tag " + msg.getHashTag());

        String tweetStr = popTweet();
        if (StringUtils.isBlank(tweetStr)) {
            log.warning("Empty tweet found for tag " + msg.getHashTag());
            return null;
        }
        try {
            return (JSONObject) new JSONParser().parse(tweetStr);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * Open a new connection with Redis to pop the tweet from the queue.
     *
     * @return tweetString
     */
    private String popTweet() {
        Jedis jedis = null;
        try {
            jedis = new Jedis(redisHost);
            return jedis.lpop(processingQueue);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return null;
    }

    /**
     * Performs the sentiment analysis on tweet content.
     * Builds the request URL to call the IDOL API.
     * It uses the sync request format and the result will appended into the tweetJson parameter.
     *
     * @param tweetJson
     * @return models.Tweet
     */
    public JSONObject executeSentimentAnalysis(JSONObject tweetJson) {
        if (tweetJson == null) {
            return null;
        }

        try {
            String query = sentimentRequestQuery
                    .replaceAll("%TWEET%", URLEncoder.encode((String) tweetJson.get("text"), "UTF-8"))
                    .replaceAll("%LANG%", (String) tweetJson.get("lang"));

            String result = callApi(query);
            if (result != null) {
                JSONObject obj = (JSONObject) JSONValue.parse(result);
                JSONObject aggregate = (JSONObject) obj.get("aggregate");
                tweetJson.put("sentiment", aggregate.get("sentiment"));
                tweetJson.put("score", aggregate.get("score"));
                return tweetJson;
            }
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Calls the HP IDOL API the perform the sentiment analyses.
     * @param query
     * @return sentimentAnalysisResponseString
     */
    private String callApi(String query) {
        try {
           // F.Promise<Http.Response> result = WS.url("http://localhost:9001").post("content");
            log.info("Requesting sentiment analysis: " + query);
            InputStream is = new URL(query).openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            return readToString(br);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Reads the stream to convert it into string to be parsed later.
     *
     * @param bufferedReader
     * @return sentimentAnalysisResponseString
     * @throws IOException
     */
    private static String readToString(Reader bufferedReader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] data = new char[1024];
        int xf = 0;
        while((xf = bufferedReader.read(data, 0, 1024)) > 0) {
            sb.append(data, 0, xf);
        }
        return sb.toString();
    }

    /**
     * Sends the tweet to redis queue to be picked up later for another job and
     * save it in the DB.
     * @param tweetJson
     */
    private void sendToPersistQueue(JSONObject tweetJson) {
        if (tweetJson == null) {
            return;
        }
        Jedis jedis = null;
        try {
            jedis = new Jedis(redisHost);
            jedis.rpush(persistQueue, tweetJson.toJSONString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    /**
     * Sends the tweet to redis channel to be published to all subscribers
     * of this channel. In this case they are web-clients.
     *
     * @param tweetJson
     */
    private void sendToLiveTweetsChannel(JSONObject tweetJson) {
        if (tweetJson == null) {
            return;
        }
        Jedis jedis = null;
        try {
            jedis = new Jedis(redisHost);
            jedis.publish(tweetsChannel, tweetJson.toJSONString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

}
