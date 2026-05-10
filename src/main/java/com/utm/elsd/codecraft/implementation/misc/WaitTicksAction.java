package com.utm.elsd.codecraft.implementation.misc;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.api.ActionStatus;
import com.utm.elsd.codecraft.context.MinecraftContext;

public class WaitTicksAction implements Action {

    private final int ticks;
    private int elapsed = 0;

    public WaitTicksAction(int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("ticks must be greater than zero");
        }
        this.ticks = ticks;
    }

    @Override
    public ActionStatus tick(MinecraftContext ctx) {
        return ++elapsed >= ticks ? ActionStatus.DONE : ActionStatus.RUNNING;
    }
}