package actors.messages;


import com.google.common.base.Objects;

/**
 * Message read indicates that consumer should read tweets from queue.
 */
public class Read {
    private final String hashTag;

    public Read(String hashTag) {
        this.hashTag = hashTag;
    }

    public String getHashTag() {
        return hashTag;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("hashTag", hashTag)
                .toString();
    }
}
