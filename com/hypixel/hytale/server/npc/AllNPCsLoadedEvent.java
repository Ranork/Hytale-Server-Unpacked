package com.hypixel.hytale.server.npc;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.Objects;
import javax.annotation.Nonnull;

public class AllNPCsLoadedEvent implements IEvent<Void> {
   @Nonnull
   private final Int2ObjectMap<BuilderInfo> allNPCs;
   @Nonnull
   private final Int2ObjectMap<BuilderInfo> loadedNPCs;

   public AllNPCsLoadedEvent(@Nonnull Int2ObjectMap<BuilderInfo> allNPCs, @Nonnull Int2ObjectMap<BuilderInfo> loadedNPCs) {
      Objects.requireNonNull(allNPCs, "Map of all NPCs must not be empty in AllNPCsLoadedEvent");
      Objects.requireNonNull(loadedNPCs, "Map of loaded NPCs must not be empty in AllNPCsLoadedEvent");
      this.allNPCs = Int2ObjectMaps.unmodifiable(allNPCs);
      this.loadedNPCs = Int2ObjectMaps.unmodifiable(loadedNPCs);
   }

   @Nonnull
   public Int2ObjectMap<BuilderInfo> getAllNPCs() {
      return this.allNPCs;
   }

   @Nonnull
   public Int2ObjectMap<BuilderInfo> getLoadedNPCs() {
      return this.loadedNPCs;
   }

   @Nonnull
   @Override
   public String toString() {
      return "AllNPCsLoadedEvent{allNPCs=" + this.allNPCs + ", loadedNPCs=" + this.loadedNPCs + "}";
   }
}
