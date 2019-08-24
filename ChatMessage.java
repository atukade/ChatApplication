
import java.io.Serializable;
/**
 * This is the ChatMessage class which creates new chat message type and the message and also to the recipient can be mentioned.
 *
 * @author Anuj Mohanbabu Tukade, atukade@purdue.edu
 * @version 11/24/2018
 */

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    private int type;
    private String message;
    private String recipient;

    public ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public ChatMessage(int type, String message, String recipient) {
        this.type = type;
        this.message = message;
        this.recipient = recipient;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getRecipient() {
        return recipient;
    }
}
