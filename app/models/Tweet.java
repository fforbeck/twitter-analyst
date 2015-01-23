
package models;

import com.google.common.base.Objects;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Tweet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    public String user_id;
    public String data;
    public String tag;
    public Double sentiment;
    @Temporal(TemporalType.DATE)
    public Date created_on;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", user_id)
                .add("data", data)
                .add("tag", tag)
                .add("sentiment", sentiment)
                .add("createdOn", created_on)
                .toString();
    }
}