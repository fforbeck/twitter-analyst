package actors.messages;


import com.google.common.base.Objects;
import twitter4j.Status;

import java.util.List;

/**
 * Messages to communicate with Tweet Actors.
 */
public class Start {
    private final String hashTag;
    private final String lang;
    private List<Status> tweets;

    public Start(String hashTag, String lang) {
        this.hashTag = hashTag;
        this.lang = lang;
    }

    public String getHashTag() {
        return hashTag;
    }

    public List<Status> getTweets() {
        return tweets;
    }

    public void setTweets(List<Status> tweets) {
        this.tweets = tweets;
    }

    public String getLang() {
        return lang;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("hashTag", hashTag)
                .add("lang", lang)
                .add("tweets", tweets)
                .toString();
    }
}
