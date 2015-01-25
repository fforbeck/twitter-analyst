package actors;

import actors.messages.Read;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import models.Tweet;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import play.Configuration;
import play.Play;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by fforbeck on 24/01/15.
 */
public class TweetAnalyzer extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private String requestURL;
    private String apiKey;

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Configuration configuration = Play.application().configuration();
        requestURL = configuration.getString("hp.idol.analyze.sentiment.uri");
        apiKey = configuration.getString("hp.idol.analyze.sentiment.uri");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Read) {
            List<JSONObject> tweets = read((Read) message);
            analyseSentimentFrom(tweets);
        } else {
            unhandled(message);
        }
    }

    private List<JSONObject> read(Read msg) {
        String tweetStr = null;
        log.info("Reading tweet from queue for tag " + msg.getHashTag());
        Jedis jedis = null;
        try {
            jedis = new Jedis(Play.application().configuration().getString("redis.host"));
            tweetStr = jedis.rpop("tweets-queue");
            if (tweetStr == null) {
                log.warning("Empty tweet found for tag " + msg.getHashTag());
                return null;
            }

            return Arrays.asList((JSONObject) new JSONParser().parse(tweetStr));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return null;
    }

    private void analyseSentimentFrom(List<JSONObject> tweetsList) {
        log.info("Analysing sentiments from tweets");

        for (JSONObject tweetJson : tweetsList) {
            Tweet tweet = executeSentimentAnalysis(tweetJson);
            log.info("new tweet -> " + tweet);
        }

        log.info("Sentiment analysis from tweets");
    }

    public Tweet executeSentimentAnalysis(JSONObject tweet) {
        try {
            String query = Play.application().configuration().getString("idol.analyze.sentiment.uri");
            query = query
                    .replaceAll("%TWEET%", URLEncoder.encode((String) tweet.get("text"), "UTF-8"))
                    .replaceAll("%API_KEY%", apiKey);

            String result = callApi(query);
            JSONObject obj = (JSONObject) JSONValue.parse(result);

            return buildTweet(tweet, (JSONObject) obj.get("aggregate"));
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Tweet buildTweet(JSONObject tweetJson, JSONObject aggregate) {
        Tweet tweet = new Tweet();
        tweet.user_id = (Long) tweetJson.get("user_id");
        tweet.text = (String) tweetJson.get("text");
        tweet.hash_tag = (String) tweetJson.get("hash_tag");
        try {
            tweet.created_at = new SimpleDateFormat("dd/MM/yyyy").parse((String) tweetJson.get("created_at"));
        } catch (ParseException e) {
            log.error(e.getMessage() + ", invalid date: "  + tweetJson.get("created_at"));
            tweet.created_at = new Date();
        }
        tweet.sentiment = (String) aggregate.get("sentiment");
        tweet.sentiment_score = (Double) aggregate.get("score");
        return tweet;
    }

    private String callApi(String query) {
        try {
           // F.Promise<Http.Response> result = WS.url("http://localhost:9001").post("content");

            log.info("Calling sentiment analysis: " + query);

            InputStream is = new URL(query).openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            return readToString(br);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    private static String readToString(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] data = new char[1024];
        int xf = 0;
        while((xf = rd.read(data, 0, 1024)) > 0) {
            sb.append(data, 0, xf);
        }
        return sb.toString();
    }

}
