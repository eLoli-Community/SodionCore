package com.eloli.sodioncore.config;

public abstract class Migrater<F extends Configure, T extends Configure> {
    public abstract void migrate(F from, T to);
}
