
package models;

import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by fforbeck on 24/01/15.
 *
 */
@Entity
public class Tweet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    @Column(name = "user_id")
    public Long userId;
    @Column(name = "user_name")
    public String userName;
    public String text;
    @Column(name = "hash_tag")
    public String hashTag;
    public String lang;
    public String sentiment;
    @Column(name = "sentiment_score")
    public Double sentimentScore;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    public Date createdAt;
    public Double lat;
    public Double lon;
    public Long retweets;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("user_id", userId)
                .add("user_name", userName)
                .add("text", text)
                .add("hash_tag", hashTag)
                .add("lang", lang)
                .add("sentiment", sentiment)
                .add("sentiment_score", sentimentScore)
                .add("created_at", createdAt)
                .add("lat", lat)
                .add("lon", lon)
                .add("retweets", retweets)
                .toString();
    }
}