package com.hypixel.hytale.server.core.modules.collision;

@FunctionalInterface
public interface CollisionFilter<D, T> {
   boolean test(T var1, int var2, D var3, CollisionConfig var4);
}
