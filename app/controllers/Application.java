package controllers;

import actors.TweetPublisher;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.Tweet;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.TweetService;

/**
 * Created by fforbeck on 24/01/15.
 *
 * Main controller
 *
 */
@org.springframework.stereotype.Controller
public class Application extends Controller {

    @Autowired
    private TweetService tweetService;

    public static Result liveTweets() {
        return  ok(views.html.live_tweets.render("Live Tweets Analyst Ready!"));
    }

    public static Result timelineTweets() {
        return  ok(views.html.timeline_tweets.render("Timeline Tweets Ready!"));
    }

    public static Result pieTweets() {
        return  ok(views.html.pie_tweets.render("Pie Tweets Ready!"));
    }

    /**
     * Starts a new websocket connection per request.
     * It uses an akka actor instance to communicate with
     * client.
     *
     * @return WebSocket
     */
    public static WebSocket<String> wsTweets() {
        return WebSocket.withActor(new F.Function<ActorRef, Props>() {
            public Props apply(ActorRef out) throws Throwable {
                return TweetPublisher.props(out);
            }
        });
    }

    /**
     * Starts a read only transaction to query the DB
     * and filter by sentiment type.
     *
     * @param sentiment
     * @return List of tweets by sentiment in json.
     */
    @Transactional(readOnly = true)
    public Result tweets(String sentiment) {
        if (StringUtils.isBlank(sentiment)) {
            return badRequest("invalid parameter " + sentiment);
        }

        JSONArray tweets = new JSONArray();
        for (Tweet tweet : tweetService.findBySentiment(sentiment)) {
            JSONObject tweetJson = new JSONObject();
            tweetJson.put("x", tweet.created_at.getTime());
            tweetJson.put("y", tweet.sentiment_score);
            tweetJson.put("tweet", tweet.text);
            tweetJson.put("user", tweet.user_name);
            tweets.add(tweetJson);
        }
        return ok(Json.toJson(tweets));
    }

    @Transactional(readOnly = true)
    public Result statistics() {
        long total = 0l;
        long negatives = 0l;
        long neutral = 0l;
        long positives = 0l;

        for (Tweet tweet : tweetService.findAll()) {
            total++;
            if ("negative".equals(tweet.sentiment)) {
                negatives++;
            } else if ("neutral".equals(tweet.sentiment)) {
                neutral++;
            } else if ("positive".equals(tweet.sentiment)) {
                positives++;
            }
        }

        JSONArray statistics = new JSONArray();

        total = Math.max(total, 1l);

        JSONObject positiveJson = new JSONObject();
        positiveJson.put("name", "Positive");
        positiveJson.put("y", positives * 100.0/total);
        positiveJson.put("sliced", true);
        positiveJson.put("selected", true);
        statistics.add(positiveJson);

        JSONObject negativeJson = new JSONObject();
        negativeJson.put("name", "Negative");
        negativeJson.put("y", negatives * 100.0/total);
        negativeJson.put("sliced", false);
        negativeJson.put("selected", false);
        statistics.add(negativeJson);

        JSONObject neutralJson = new JSONObject();
        neutralJson.put("name", "Neutral");
        neutralJson.put("y", neutral * 100.0/total);
        neutralJson.put("sliced", false);
        neutralJson.put("selected", false);
        statistics.add(neutralJson);

        return ok(Json.toJson(statistics));
    }

    /**
     * Start a new search in twitter API filtering by
     * hashTag and lang. The result of this search will
     * be archived in DB.
     *
     * @param hashTag
     * @param lang
     * @return Status 200 (ok msg)
     */
    public Result searchBy(String hashTag, String lang) {
        tweetService.startHarvestingBy(hashTag, lang);
        return ok("I got! tks");
    }
}