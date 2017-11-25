package Subway;

import model.modeling.content;
import model.modeling.message;

import java.util.UUID;
import java.util.stream.Stream;

public class MessageFilterer {
    public static Stream<content> getRelevantContent(message m, UUID id) {
        return m.stream().filter(c -> ((IWithUUID) ((content) c).getValue()).getID() == id);
    }
}
