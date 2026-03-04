package com.hypixel.hytale.builtin.tagset;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TagSetPlugin extends JavaPlugin {
   private static TagSetPlugin instance;
   private final Map<Class<? extends TagSet>, TagSetPlugin.TagSetLookup> lookups = new ConcurrentHashMap<>();

   public static TagSetPlugin get() {
      return instance;
   }

   public TagSetPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              NPCGroup.class, new IndexedLookupTableAssetMap<>(NPCGroup[]::new)
                           )
                           .setPath("NPC/Groups"))
                        .setCodec(NPCGroup.CODEC))
                     .setKeyFunction(NPCGroup::getId))
                  .setReplaceOnRemove(NPCGroup::new))
               .loadsBefore(Interaction.class))
            .build()
      );
      this.registerTagSetType(NPCGroup.class);
   }

   public <T extends TagSet> void registerTagSetType(Class<T> clazz) {
      if (!this.isDisabled()) {
         this.lookups.computeIfAbsent(clazz, c -> new TagSetPlugin.TagSetLookup());
      }
   }

   @Nonnull
   public static <T extends TagSet> TagSetPlugin.TagSetLookup get(Class<T> clazz) {
      return Objects.requireNonNull(instance.lookups.get(clazz), "Class is not registered with the TagSet module!");
   }

   public static class TagSetLookup {
      @Nonnull
      private Int2ObjectMap<IntSet> flattenedSets = Int2ObjectMaps.unmodifiable(new Int2ObjectOpenHashMap());

      public <T extends TagSet> void putAssetSets(
         @Nonnull Map<String, T> tagSetAssets, @Nonnull Object2IntMap<String> tagSetIndexMap, @Nonnull Object2IntMap<String> tagIndexMap
      ) {
         TagSetLookupTable<T> lookupTable = new TagSetLookupTable<>(tagSetAssets, tagSetIndexMap, tagIndexMap);
         this.flattenedSets = Int2ObjectMaps.unmodifiable(lookupTable.getFlattenedSet());
      }

      public boolean tagInSet(int tagSet, int tagIndex) {
         IntSet set = (IntSet)this.flattenedSets.get(tagSet);
         if (set == null) {
            throw new IllegalArgumentException("Attempting to access a tagset which does not exist!");
         } else {
            return set.contains(tagIndex);
         }
      }

      @Nullable
      public IntSet getSet(int tagSet) {
         return (IntSet)this.flattenedSets.get(tagSet);
      }
   }
}
