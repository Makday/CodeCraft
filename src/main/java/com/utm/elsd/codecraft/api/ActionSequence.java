package com.utm.elsd.codecraft.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ActionSequence {
    private final List<Action> actions = new ArrayList<>();

    private ActionSequence() {}

    public static ActionSequence start(Action first) {
        return new ActionSequence().then(first);
    }

    public ActionSequence then(Action action) {
        actions.add(action);
        return this;
    }

    List<Action> actions() {
        return Collections.unmodifiableList(actions);
    }
}
