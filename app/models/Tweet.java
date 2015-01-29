
package models;

import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by fforbeck on 24/01/15.
 *
 *
 */
@Entity
public class Tweet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    public Long user_id;
    public String user_name;
    public String text;
    public String hash_tag;
    public String lang;
    public String sentiment;
    public Double sentiment_score;
    @Temporal(TemporalType.TIMESTAMP)
    public Date created_at;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("user_id", user_id)
                .add("user_name", user_name)
                .add("text", text)
                .add("hash_tag", hash_tag)
                .add("lang", lang)
                .add("sentiment", sentiment)
                .add("sentiment_score", sentiment_score)
                .add("created_at", created_at)
                .toString();
    }
}