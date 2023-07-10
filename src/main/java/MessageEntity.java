import lombok.Data;


public class MessageEntity {
    private String type;
    private String text;
    private String from;

    public MessageEntity(String type, String text, String from) {
        this.type = type;
        this.text = text;
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }


    public MessageEntity() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "MessageEntity{" +
                "type='" + type + '\'' +
                ", text='" + text + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}
