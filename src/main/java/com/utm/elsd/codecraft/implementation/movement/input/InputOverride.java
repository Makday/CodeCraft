package com.utm.elsd.codecraft.implementation.movement.input;

import java.util.EnumSet;
import java.util.Set;

public final class InputOverride {

    public static final InputOverride INSTANCE = new InputOverride();

    public enum Key {
        FORWARD, BACK, LEFT, RIGHT, JUMP, SNEAK
    }

    private final Set<Key> forced = EnumSet.noneOf(Key.class);

    private InputOverride() {}

    public void force(Key key) {
        forced.add(key);
    }

    public void release(Key key) {
        forced.remove(key);
    }

    public void releaseAll() {
        forced.clear();
    }

    public boolean isForced(Key key) {
        return forced.contains(key);
    }
}