package com.piper.valley.models.command;

/** An executable request whose receiver and arguments are supplied at construction. */
public interface Command<R> {
    R execute();
}
