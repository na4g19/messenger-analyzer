import java.util.ArrayList;
import java.util.List;

/**
 * Represents a message read from JSON message file
 */
public class MessageFormat {

    private String sender;
    private String content;
    private String type;

    private long timestamp;
    private long creationTimestamp;

    private List<Reaction> reactions = new ArrayList<>();
    private List<String> users = new ArrayList<>();
    private List<String> photos = new ArrayList<>();

    /*
     * Getters and Setters
     */
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addUser(String user) {
        users.add(user);
    }

    public void addPhoto(String photo) {
        photos.add(photo);
    }

    public void addReaction(String reaction, String sender) {
        reactions.add(new Reaction(reaction, sender));
    }

    public List<Reaction> getReactions() {
        return reactions;
    }

    public class Reaction {

        String reaction;
        String sender;

        public Reaction(String reaction, String sender) {
            this.reaction = reaction;
            this.sender = sender;
        }

        public String getSender() {
            return sender;
        }
    }
}
