import lombok.Data;

import java.util.List;

@Data
public class Message {
    private List<Object> text_entities;
    private String from;
}
