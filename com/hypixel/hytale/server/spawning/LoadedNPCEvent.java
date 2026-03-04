package com.hypixel.hytale.server.spawning;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import java.util.Objects;
import javax.annotation.Nonnull;

public class LoadedNPCEvent implements IEvent<Void> {
   private BuilderInfo builderInfo;

   public LoadedNPCEvent(@Nonnull BuilderInfo builderInfo) {
      Objects.requireNonNull(builderInfo, "builderInfo can't be null for event");
      if (!(builderInfo.getBuilder() instanceof ISpawnableWithModel)) {
         throw new IllegalArgumentException("BuilderInfo builder must be spawnable for event");
      } else {
         this.builderInfo = builderInfo;
      }
   }

   public BuilderInfo getBuilderInfo() {
      return this.builderInfo;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LoadedNPCEvent{builderInfo=" + this.builderInfo + "}";
   }
}
