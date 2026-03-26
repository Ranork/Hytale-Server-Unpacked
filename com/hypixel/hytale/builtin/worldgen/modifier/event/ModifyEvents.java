package com.hypixel.hytale.builtin.worldgen.modifier.event;

import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.container.CoverContainer;
import com.hypixel.hytale.server.worldgen.container.EnvironmentContainer;
import com.hypixel.hytale.server.worldgen.container.LayerContainer;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.container.TintContainer;
import com.hypixel.hytale.server.worldgen.container.WaterContainer;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import com.hypixel.hytale.server.worldgen.loader.context.CaveFileContext;
import java.util.List;
import javax.annotation.Nonnull;

public interface ModifyEvents {
   public record BiomeCovers(
      @Nonnull BiomeFileContext file,
      @Nonnull List<CoverContainer.CoverContainerEntry> entries,
      @Nonnull ModifyEvent.ContentLoader<CoverContainer.CoverContainerEntry> loader
   ) implements ModifyEvent<CoverContainer.CoverContainerEntry> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Biome_Covers;
      }
   }

   public record BiomeDynamicLayers(
      @Nonnull BiomeFileContext file,
      @Nonnull List<LayerContainer.DynamicLayer> entries,
      @Nonnull ModifyEvent.ContentLoader<LayerContainer.DynamicLayer> loader
   ) implements ModifyEvent<LayerContainer.DynamicLayer> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Biome_Dynamic_Layers;
      }
   }

   public record BiomeEnvironments(
      @Nonnull BiomeFileContext file,
      @Nonnull List<EnvironmentContainer.EnvironmentContainerEntry> entries,
      @Nonnull ModifyEvent.ContentLoader<EnvironmentContainer.EnvironmentContainerEntry> loader
   ) implements ModifyEvent<EnvironmentContainer.EnvironmentContainerEntry> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Biome_Environments;
      }
   }

   public record BiomeFluids(
      @Nonnull BiomeFileContext file, @Nonnull List<WaterContainer.Entry> entries, @Nonnull ModifyEvent.ContentLoader<WaterContainer.Entry> loader
   ) implements ModifyEvent<WaterContainer.Entry> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Biome_Fluids;
      }
   }

   public record BiomePrefabs(
      @Nonnull BiomeFileContext file,
      @Nonnull List<PrefabContainer.PrefabContainerEntry> entries,
      @Nonnull ModifyEvent.ContentLoader<PrefabContainer.PrefabContainerEntry> loader
   ) implements ModifyEvent<PrefabContainer.PrefabContainerEntry> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Biome_Prefabs;
      }
   }

   public record BiomeStaticLayers(
      @Nonnull BiomeFileContext file, @Nonnull List<LayerContainer.StaticLayer> entries, @Nonnull ModifyEvent.ContentLoader<LayerContainer.StaticLayer> loader
   ) implements ModifyEvent<LayerContainer.StaticLayer> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Biome_Static_Layers;
      }
   }

   public record BiomeTints(
      @Nonnull BiomeFileContext file,
      @Nonnull List<TintContainer.TintContainerEntry> entries,
      @Nonnull ModifyEvent.ContentLoader<TintContainer.TintContainerEntry> loader
   ) implements ModifyEvent<TintContainer.TintContainerEntry> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Biome_Tints;
      }
   }

   public record CaveCovers(
      @Nonnull CaveFileContext file,
      @Nonnull List<CaveNodeType.CaveNodeCoverEntry> entries,
      @Nonnull ModifyEvent.ContentLoader<CaveNodeType.CaveNodeCoverEntry> loader
   ) implements ModifyEvent<CaveNodeType.CaveNodeCoverEntry> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Cave_Covers;
      }
   }

   public record CavePrefabs(
      @Nonnull CaveFileContext file,
      @Nonnull List<CavePrefabContainer.CavePrefabEntry> entries,
      @Nonnull ModifyEvent.ContentLoader<CavePrefabContainer.CavePrefabEntry> loader
   ) implements ModifyEvent<CavePrefabContainer.CavePrefabEntry> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Cave_Prefabs;
      }
   }

   public record CaveTypes(@Nonnull CaveFileContext file, @Nonnull List<CaveType> entries, @Nonnull ModifyEvent.ContentLoader<CaveType> loader)
      implements ModifyEvent<CaveType> {
      @Nonnull
      @Override
      public EventType type() {
         return EventType.Cave_Types;
      }
   }
}
