package com.utm.elsd.codecraft.api;

import com.utm.elsd.codecraft.context.MinecraftContext;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ActionRunner {
    private final MinecraftContext ctx;
    private final Deque<Action> queue = new ArrayDeque<>();
    private Action current = null;

    public ActionRunner(MinecraftContext ctx) {
        this.ctx = ctx;
    }

    public void run(ActionSequence sequence) {
        cancel();
        queue.addAll(sequence.actions());
    }

    public void cancel() {
        if (current != null) {
            current = null;
        }
        queue.clear();
    }

    public boolean isRunning() {
        return current != null || !queue.isEmpty();
    }

    /** Called every tick from ClientTickEvents */
    public void tick() {
        if (current == null) {
            current = queue.poll();
            if (current == null) return;
        }

        switch (current.tick(ctx)) {
            case DONE    -> current = null;
            case RUNNING -> {}
        }
    }
}
