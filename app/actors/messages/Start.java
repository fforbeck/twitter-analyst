package actors.messages;


import twitter4j.Status;

import java.util.List;

/**
 * Messages to communicate with Twitter Harvest Actor
 */
public class Start {
    private final String hashTag;
    private List<Status> tweets;


    public Start(String hashTag) {
        this.hashTag = hashTag;
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
}
