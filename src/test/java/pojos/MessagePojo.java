package pojos;

import lombok.Data;

@Data
public class MessagePojo {
    private int id;
    private String text;
    private boolean isImportant;
}
