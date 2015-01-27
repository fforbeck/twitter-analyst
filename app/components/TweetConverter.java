package components;

import models.Tweet;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import twitter4j.Status;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Tweet converter is responsible for convert the twitter4j.Status to JSONObject to make
 * possible to extract the content as String and place in the queue. Also convert the
 * simple.JSONObject to models.Tweet in order to create the entity that will be saved in db.
 *
 */
public class TweetConverter {

    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String TEXT = "text";
    private static final String LANG = "lang";
    private static final String CREATED_AT = "created_at";
    private static final String HASH_TAG = "hash_tag";
    private static final String SENTIMENT = "sentiment";
    private static final String SCORE = "score";
    private static final String DD_MM_YYYY = "dd/MM/yyyy";

    private static final Map<String, String> twitterIdolsLangMap = new HashMap<String, String>();

    static {
        // twitter lang in production: https://dev.twitter.com/rest/reference/get/help/languages
        // idol lang in production: https://www.idolondemand.com/developer/apis/analyzesentiment#try
        // key: tweet4j lang - value: idol api lang
        twitterIdolsLangMap.put("en", "eng");
        twitterIdolsLangMap.put("es", "spa");
        twitterIdolsLangMap.put("pt", "por");
        twitterIdolsLangMap.put("fr", "fre");
        twitterIdolsLangMap.put("de", "ger");
        twitterIdolsLangMap.put("it", "ita");
    }

    private final Logger log = Logger.getLogger(TweetConverter.class.getSimpleName());


    /**
     * Convert an twitter4j Status type to simple.JSONObject.
     * Grabs only the relevant data from status which are: userId, userName,
     * tweet, creationDate and hashTag.
     *
     * @param status
     * @param hashTag
     * @return Tweet in JSON format
     */
    public JSONObject convert(Status status, String hashTag) {
        JSONObject tweet = new JSONObject();
        tweet.put(USER_ID, status.getUser().getId());
        tweet.put(USER_NAME, status.getUser().getScreenName());
        tweet.put(TEXT, status.getText());
        tweet.put(LANG, getLang(status.getLang()));
        tweet.put(CREATED_AT, getDate(status));
        tweet.put(HASH_TAG, hashTag);
        return tweet;
    }

    private String getDate(Status status) {
        return new SimpleDateFormat(DD_MM_YYYY).format(status.getCreatedAt());
    }

    /**
     * Converts a tweetStr into Tweet application model.
     * Grabs only the relevant data from status which are: userId, userName,
     * tweet, creationDate and hashTag. If the jsonParse fails then we just
     * ignore the tweet and return null.
     *
     * @param tweetStr
     * @return tweet
     */
    public Tweet convert(String tweetStr) {

        JSONObject tweetJson = null;
        try {
            tweetJson = (JSONObject) new JSONParser().parse(tweetStr);
        } catch (org.json.simple.parser.ParseException e) {
            log.severe(e.getMessage());
            return null;
        }

        Tweet tweet = new Tweet();
        tweet.user_id = (Long) tweetJson.get(USER_ID);
        tweet.user_name = (String) tweetJson.get(USER_NAME);
        tweet.text = (String) tweetJson.get(TEXT);
        tweet.hash_tag = (String) tweetJson.get(HASH_TAG);
        tweet.lang = (String) tweetJson.get(LANG);
        tweet.created_at = getDate(tweetJson);
        tweet.sentiment = (String) tweetJson.get(SENTIMENT);
        tweet.sentiment_score = getDouble(tweetJson.get(SCORE));
        return tweet;
    }

    private Date getDate(JSONObject tweetJson) {
        try {
            return new SimpleDateFormat(DD_MM_YYYY).parse((String) tweetJson.get(CREATED_AT));
        } catch (ParseException e) {
            log.severe(e.getMessage() + ", invalid date: " + tweetJson.get(CREATED_AT));
            return new Date();
        }
    }

    private String getLang(Object o) {
        String twitterLang = (String) o;
        return twitterIdolsLangMap.get(twitterLang);
    }

    private Double getDouble(Object obj) {
        if (obj instanceof Long)
            return new Double((Long)obj);

        return (Double) obj;
    }

}