package com.hypixel.hytale.builtin.worldgen.modifier.op;

import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.util.ListPool;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class RemoveOp implements Op {
   public static final String ID = "Remove";
   public static final BuilderCodec<RemoveOp> CODEC = BuilderCodec.builder(RemoveOp.class, RemoveOp::new)
      .documentation("Removes matching content from the target content list")
      .<String[]>append(new KeyedCodec<>("Rules", BuilderCodec.STRING_ARRAY), (instance, array) -> instance.rules = array, instance -> instance.rules)
      .documentation(
         "List of glob-matching rules to match entries. Rules are only implemented for Prefab paths and CaveType names, but can be set to '*' to remove all entries of any content type"
      )
      .add()
      .afterDecode(op -> op.isClearAll = ArrayUtil.contains(op.rules, "*"))
      .build();
   private String[] rules = ArrayUtil.EMPTY_STRING_ARRAY;
   private transient boolean isClearAll = false;

   @Override
   public <T> void apply(@Nonnull ModifyEvent<T> event) throws Error {
      if (this.rules.length != 0) {
         if (this.isClearAll) {
            event.entries().clear();
         } else {
            switch (event) {
               case ModifyEvents.BiomePrefabs prefabs:
                  removePrefabs(prefabs, this.rules, PrefabContainer.ENTRY_POOL, PrefabContainer.PrefabContainerEntry::getPrefabs);
                  break;
               case ModifyEvents.CaveTypes types:
                  removeContent(types, this.rules, CaveType.ENTRY_POOL, CaveType::getName);
                  break;
               case ModifyEvents.CavePrefabs prefabsx:
                  removePrefabs(prefabsx, this.rules, CavePrefabContainer.ENTRY_POOL, CavePrefabContainer.CavePrefabEntry::getPrefabs);
                  break;
               default:
            }
         }
      }
   }

   protected static <T> void removePrefabs(
      @Nonnull ModifyEvent<T> event,
      @Nonnull String[] rules,
      @Nonnull ListPool<T> pool,
      @Nonnull Function<T, IWeightedMap<WorldGenPrefabSupplier>> prefabGetter
   ) {
      try (ListPool.Resource<T> temp = pool.acquire(event.entries().size())) {
         label53:
         for (int i = 0; i < event.entries().size(); i++) {
            T entry = event.entries().get(i);
            IWeightedMap<WorldGenPrefabSupplier> prefabs = prefabGetter.apply(entry);

            for (WorldGenPrefabSupplier prefab : prefabs.internalKeys()) {
               String identity = prefab.getPrefabKey();

               for (String rule : rules) {
                  if (StringUtil.isGlobMatching(rule, identity)) {
                     continue label53;
                  }
               }
            }

            temp.add(entry);
         }

         modifyEventContent(temp, event);
      }
   }

   protected static <T> void removeContent(
      @Nonnull ModifyEvent<T> event, @Nonnull String[] rules, @Nonnull ListPool<T> pool, @Nonnull Function<T, String> identityGetter
   ) {
      try (ListPool.Resource<T> temp = pool.acquire(event.entries().size())) {
         label44:
         for (int i = 0; i < event.entries().size(); i++) {
            T entry = event.entries().get(i);
            String identity = identityGetter.apply(entry);

            for (String rule : rules) {
               if (StringUtil.isGlobMatching(rule, identity)) {
                  continue label44;
               }
            }

            temp.add(entry);
         }

         modifyEventContent(temp, event);
      }
   }

   protected static <T> void modifyEventContent(@Nonnull List<T> entries, @Nonnull ModifyEvent<T> event) {
      if (entries.size() < event.entries().size()) {
         int count = event.entries().size() - entries.size();
         event.entries().clear();
         event.entries().addAll(entries);
         LogUtil.getLogger().at(Level.FINE).log("[%s] Removed %d entries from: %s", event.type(), count, event.file().getContentPath());
      }
   }
}
