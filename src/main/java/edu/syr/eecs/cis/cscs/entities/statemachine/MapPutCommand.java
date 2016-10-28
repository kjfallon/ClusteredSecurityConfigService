package edu.syr.eecs.cis.cscs.entities.statemachine;

import io.atomix.copycat.Command;

public class MapPutCommand implements Command<Object> {
    private final Object key;
    private final Object value;

    public MapPutCommand(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Object key() {
        return key;
    }

    public Object value() {
        return value;
    }
    
}
