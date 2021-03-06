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
 * Main controller which receives the HTTP requests, handle and return a Result.
 * That can be a simple html page, a WebSocket connection or JSON response.
 *
 */
@org.springframework.stereotype.Controller
public class Application extends Controller {

    @Autowired
    private TweetService tweetService;

    /**
     * @return live_tweets.scala.html
     */
    public static Result liveTweets() {
        return  ok(views.html.live_tweets.render("Live Tweets Analyst Ready!"));
    }

    /**
     * @return pie_tweets.scala.html
     */
    public static Result pieTweets() {
        return  ok(views.html.pie_tweets.render("Pie Tweets Ready!"));
    }

    /**
     * @return timeline_tweets.scala.html
     */
    public static Result timelineTweets() {
        return  ok(views.html.timeline_tweets.render("Timeline Tweets Ready!"));
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
            tweetJson.put("x", tweet.createdAt.getTime());
            tweetJson.put("y", tweet.sentimentScore);
            tweetJson.put("tweet", tweet.text);
            tweetJson.put("user", tweet.userName);
            tweets.add(tweetJson);
        }
        return ok(Json.toJson(tweets));
    }

    /**
     * Builds the percentage of tweets that are negative, positive and neutral.
     *
     * @return Tweets statistics
     */
    @Transactional(readOnly = true)
    public Result statistics() {
        long total = 0l;
        long negatives = 0l;
        long neutral = 0l;
        long positives = 0l;

        //TODO move it to be done in service and count during the query
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
        total = Math.max(total, 1l);
        return ok(Json.toJson(buildStatistics(total, negatives, neutral, positives)));
    }

    private JSONArray buildStatistics(long total, long negatives, long neutral, long positives) {
        JSONArray statistics = new JSONArray();
        statistics.add(buildPositiveStats(total, positives));
        statistics.add(buildNegativeStats(total, negatives));
        statistics.add(buildNeutralStats(total, neutral));
        return statistics;
    }

    private JSONObject buildPositiveStats(long total, long positives) {
        JSONObject positiveJson = new JSONObject();
        positiveJson.put("name", "Positive");
        positiveJson.put("y", positives * 100.0/total);
        positiveJson.put("sliced", true);
        positiveJson.put("selected", true);
        positiveJson.put("count", positives);
        return positiveJson;
    }

    private JSONObject buildNegativeStats(long total, long negatives) {
        JSONObject negativeJson = new JSONObject();
        negativeJson.put("name", "Negative");
        negativeJson.put("y", negatives * 100.0/total);
        negativeJson.put("sliced", false);
        negativeJson.put("selected", false);
        negativeJson.put("count", negatives);
        return negativeJson;
    }

    private JSONObject buildNeutralStats(long total, long neutral) {
        JSONObject neutralJson = new JSONObject();
        neutralJson.put("name", "Neutral");
        neutralJson.put("y", neutral * 100.0/total);
        neutralJson.put("sliced", false);
        neutralJson.put("selected", false);
        neutralJson.put("count", neutral);
        return neutralJson;
    }

}