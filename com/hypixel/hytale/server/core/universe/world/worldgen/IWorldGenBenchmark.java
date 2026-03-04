package com.hypixel.hytale.server.core.universe.world.worldgen;

import java.util.concurrent.CompletableFuture;

public interface IWorldGenBenchmark {
   void start();

   void stop();

   CompletableFuture<String> buildReport();
}
