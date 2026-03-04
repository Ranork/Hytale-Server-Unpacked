package com.hypixel.hytale.server.npc.asset.builder;

public class StatePair {
   private final String fullStateName;
   private final int state;
   private final int subState;

   public StatePair(String fullStateName, int state, int subState) {
      this.fullStateName = fullStateName;
      this.state = state;
      this.subState = subState;
   }

   public String getFullStateName() {
      return this.fullStateName;
   }

   public int getState() {
      return this.state;
   }

   public int getSubState() {
      return this.subState;
   }
}
