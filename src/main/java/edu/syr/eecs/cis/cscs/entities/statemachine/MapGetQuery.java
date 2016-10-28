package edu.syr.eecs.cis.cscs.entities.statemachine;

import io.atomix.copycat.Query;

public class MapGetQuery implements Query<Object> {
    private final Object key;

    public MapGetQuery(Object key) {
        this.key = key;
    }

    public Object key() {
        return key;
    }
}
