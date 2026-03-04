package com.hypixel.hytale.server.npc.asset.builder;

import java.util.EnumSet;
import java.util.function.Supplier;

public enum InstructionType implements Supplier<String> {
   Default("the default behaviour instruction"),
   Interaction("the interaction instruction"),
   Death("the death instruction"),
   Component("a component"),
   StateTransitions("state transition actions");

   private final String description;
   public static final EnumSet<InstructionType> Any = EnumSet.allOf(InstructionType.class);
   public static final EnumSet<InstructionType> MotionAllowedInstructions = EnumSet.of(Default);
   public static final EnumSet<InstructionType> StateChangeAllowedInstructions = EnumSet.of(Default, Interaction, Death, Component);

   private InstructionType(String description) {
      this.description = description;
   }

   public String get() {
      return this.description;
   }
}
