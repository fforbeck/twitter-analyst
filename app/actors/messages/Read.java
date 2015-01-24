package actors.messages;


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

}
