package com.hypixel.hytale.server.core.modules.entitystats;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatTypePacketGenerator;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.AliveCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.ChargingCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.Condition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.EnvironmentCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.GlidingCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.LogicCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.NoDamageTakenCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.OutOfCombatCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.PlayerCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.RegenHealthCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.SprintingCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.StatCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.SuffocatingCondition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.WieldingCondition;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bouncycastle.util.Arrays;

public class EntityStatsModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(EntityStatsModule.class)
      .depends(EntityModule.class)
      .depends(InteractionModule.class)
      .build();
   private static EntityStatsModule instance;
   private ComponentType<EntityStore, EntityStatMap> entityStatMapComponentType;
   private SystemType<EntityStore, EntityStatsSystems.StatModifyingSystem> statModifyingSystemType;

   public static EntityStatsModule get() {
      return instance;
   }

   public EntityStatsModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      Modifier.CODEC.register("Boost", StaticModifier.class, StaticModifier.ENTITY_CODEC);
      Modifier.CODEC.register("Static", StaticModifier.class, StaticModifier.ENTITY_CODEC);
      Condition.CODEC.register("LogicCondition", LogicCondition.class, LogicCondition.CODEC);
      Condition.CODEC.register("RegenHealth", RegenHealthCondition.class, RegenHealthCondition.CODEC);
      Condition.CODEC.register("NoDamageTaken", NoDamageTakenCondition.class, NoDamageTakenCondition.CODEC);
      Condition.CODEC.register("Suffocating", SuffocatingCondition.class, SuffocatingCondition.CODEC);
      Condition.CODEC.register("Charging", ChargingCondition.class, ChargingCondition.CODEC);
      Condition.CODEC.register("Alive", AliveCondition.class, AliveCondition.CODEC);
      Condition.CODEC.register("Environment", EnvironmentCondition.class, EnvironmentCondition.CODEC);
      Condition.CODEC.register("Player", PlayerCondition.class, PlayerCondition.CODEC);
      Condition.CODEC.register("OutOfCombat", OutOfCombatCondition.class, OutOfCombatCondition.CODEC);
      Condition.CODEC.register("Wielding", WieldingCondition.class, WieldingCondition.CODEC);
      Condition.CODEC.register("Sprinting", SprintingCondition.class, SprintingCondition.CODEC);
      Condition.CODEC.register("Gliding", GlidingCondition.class, GlidingCondition.CODEC);
      Condition.CODEC.register("Stat", StatCondition.class, StatCondition.CODEC);
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                                 EntityStatType.class, new IndexedLookupTableAssetMap<>(EntityStatType[]::new)
                              )
                              .setPath("Entity/Stats"))
                           .setCodec(EntityStatType.CODEC))
                        .setKeyFunction(EntityStatType::getId))
                     .setPacketGenerator(new EntityStatTypePacketGenerator())
                     .setReplaceOnRemove(EntityStatType::getUnknownFor))
                  .preLoadAssets(Collections.singletonList(EntityStatType.UNKNOWN)))
               .loadsAfter(SoundEvent.class, ParticleSystem.class))
            .build()
      );
      this.getEventRegistry().register(LoadedAssetsEvent.class, EntityStatType.class, this::onLoadedAssetsEvent);
      this.statModifyingSystemType = this.getEntityStoreRegistry().registerSystemType(EntityStatsSystems.StatModifyingSystem.class);
      this.entityStatMapComponentType = this.getEntityStoreRegistry().registerComponent(EntityStatMap.class, "EntityStats", EntityStatMap.CODEC);
      this.getEntityStoreRegistry().registerSystem(new EntityStatsSystems.Setup(this.entityStatMapComponentType));
      this.getEntityStoreRegistry().registerSystem(new EntityStatsModule.PlayerRegenerateStatsSystem());
      this.getEntityStoreRegistry().registerSystem(new EntityStatsSystems.Recalculate(this.entityStatMapComponentType));
      this.getEntityStoreRegistry().registerSystem(new EntityStatsSystems.EntityTrackerUpdate(this.entityStatMapComponentType));
      this.getEntityStoreRegistry().registerSystem(new EntityStatsSystems.EntityTrackerRemove(this.entityStatMapComponentType));
      this.getEntityStoreRegistry().registerSystem(new EntityStatsSystems.Changes(this.entityStatMapComponentType));
      this.getEntityStoreRegistry().registerSystem(new EntityStatsSystems.ClearChanges(this.entityStatMapComponentType));
      this.getEventRegistry().register(LoadedAssetsEvent.class, Item.class, x$0 -> onLoadedAssetsInvalidate(x$0));
      this.getEventRegistry().register(LoadedAssetsEvent.class, EntityEffect.class, x$0 -> onLoadedAssetsInvalidate(x$0));
   }

   @Override
   protected void start() {
      DefaultEntityStatTypes.update();
      if (DefaultEntityStatTypes.getHealth() == Integer.MIN_VALUE
         || DefaultEntityStatTypes.getOxygen() == Integer.MIN_VALUE
         || DefaultEntityStatTypes.getMana() == Integer.MIN_VALUE
         || DefaultEntityStatTypes.getStamina() == Integer.MIN_VALUE
         || DefaultEntityStatTypes.getSignatureEnergy() == Integer.MIN_VALUE
         || DefaultEntityStatTypes.getAmmo() == Integer.MIN_VALUE) {
         throw new IllegalStateException("Missing default EntityStatType");
      }
   }

   @Nullable
   @Deprecated(forRemoval = true)
   public static EntityStatMap get(@Nonnull Entity entity) {
      Ref<EntityStore> ref = entity.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         return store.getComponent(ref, get().getEntityStatMapComponentType());
      } else {
         return null;
      }
   }

   private void onLoadedAssetsEvent(LoadedAssetsEvent<String, EntityStatType, IndexedLookupTableAssetMap<String, EntityStatType>> event) {
      DefaultEntityStatTypes.update();
      Universe.get()
         .getWorlds()
         .forEach(
            (s, world) -> world.execute(
               () -> {
                  Store<EntityStore> store = world.getEntityStore().getStore();
                  store.forEachEntityParallel(
                     EntityStatMap.getComponentType(),
                     (index, archetypeChunk, commandBuffer) -> archetypeChunk.getComponent(index, EntityStatMap.getComponentType()).update()
                  );
               }
            )
         );
   }

   private static <K, T extends JsonAsset<K>, M extends AssetMap<K, T>> void onLoadedAssetsInvalidate(LoadedAssetsEvent<K, T, M> event) {
      Universe.get().getWorlds().forEach((s, world) -> world.execute(() -> {
         Store<EntityStore> store = world.getEntityStore().getStore();
         store.forEachEntityParallel(AllLegacyLivingEntityTypesQuery.INSTANCE, (index, archetypeChunk, commandBuffer) -> {
            LivingEntity livingEntity = (LivingEntity)EntityUtils.getEntity(index, archetypeChunk);

            assert livingEntity != null;

            livingEntity.getStatModifiersManager().setRecalculate(true);
         });
      }));
   }

   @Nullable
   public static Int2FloatMap resolveEntityStats(@Nullable Object2FloatMap<String> raw) {
      if (raw != null && !raw.isEmpty()) {
         Int2FloatMap out = null;
         ObjectIterator var2 = raw.object2FloatEntrySet().iterator();

         while (var2.hasNext()) {
            Entry<String> entry = (Entry<String>)var2.next();
            int index = EntityStatType.getAssetMap().getIndex((String)entry.getKey());
            if (index != Integer.MIN_VALUE) {
               if (out == null) {
                  out = new Int2FloatOpenHashMap();
               }

               out.put(index, entry.getFloatValue());
            }
         }

         return out;
      } else {
         return null;
      }
   }

   @Nullable
   public static <T> Int2ObjectMap<T> resolveEntityStats(@Nullable Map<String, T> raw) {
      if (raw != null && !raw.isEmpty()) {
         Int2ObjectMap<T> out = null;

         for (java.util.Map.Entry<String, T> entry : raw.entrySet()) {
            int index = EntityStatType.getAssetMap().getIndex(entry.getKey());
            if (index != Integer.MIN_VALUE) {
               if (out == null) {
                  out = new Int2ObjectOpenHashMap();
               }

               out.put(index, entry.getValue());
            }
         }

         return out;
      } else {
         return null;
      }
   }

   @Nullable
   public static int[] resolveEntityStats(@Nullable String[] raw) {
      if (Arrays.isNullOrEmpty(raw)) {
         return null;
      } else {
         int[] out = new int[raw.length];
         int size = 0;

         for (int i = 0; i < raw.length; i++) {
            int index = EntityStatType.getAssetMap().getIndex(raw[i]);
            if (index != Integer.MIN_VALUE) {
               out[size++] = index;
            }
         }

         if (size != raw.length) {
            out = Arrays.copyOf(out, size);
         }

         return out;
      }
   }

   public ComponentType<EntityStore, EntityStatMap> getEntityStatMapComponentType() {
      return this.entityStatMapComponentType;
   }

   public SystemType<EntityStore, EntityStatsSystems.StatModifyingSystem> getStatModifyingSystemType() {
      return this.statModifyingSystemType;
   }

   public class PlayerRegenerateStatsSystem extends EntityStatsSystems.Regenerate<Player> {
      public PlayerRegenerateStatsSystem() {
         super(EntityStatsModule.this.entityStatMapComponentType, Player.getComponentType());
      }
   }
}
