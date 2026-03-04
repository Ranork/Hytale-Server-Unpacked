package com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.HasTargetCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.IsInStateCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.LineOfSightCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.NearbyCountCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.RandomiserCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.SelfStatAbsoluteCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.SelfStatPercentageCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.TargetDistanceCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.TargetMovementStateCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.TargetStatAbsoluteCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.TargetStatPercentageCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.TimeOfDayCondition;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.TimeSinceLastUsedCondition;
import com.hypixel.hytale.server.npc.role.Role;
import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;

public abstract class Condition implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, Condition>> {
   public static final double NO_TARGET = Double.MAX_VALUE;
   public static final int ALWAYS_TRUE_SIMPLICITY = 0;
   public static final int BOOLEAN_CHECK_SIMPLICITY = 10;
   public static final int NORMALISED_CURVE_SIMPLICITY = 20;
   public static final int SCALED_CURVE_SIMPLICITY = 30;
   public static final int HIGH_COST_SIMPLICITY = 40;
   @Nonnull
   public static final AssetCodecMapCodec<String, Condition> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   public static final BuilderCodec<Condition> BASE_CODEC = BuilderCodec.abstractBuilder(Condition.class)
      .afterDecode(condition -> condition.reference = new WeakReference<>(condition))
      .build();
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(Condition.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(Condition::getAssetStore));
   private static AssetStore<String, Condition, IndexedLookupTableAssetMap<String, Condition>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected WeakReference<Condition> reference;

   @Nonnull
   public static AssetStore<String, Condition, IndexedLookupTableAssetMap<String, Condition>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(Condition.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, Condition> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, Condition>)getAssetStore().getAssetMap();
   }

   public Condition(String id) {
      this.id = id;
   }

   protected Condition() {
   }

   public String getId() {
      return this.id;
   }

   public void setupNPC(Role role) {
   }

   public void setupNPC(Holder<EntityStore> holder) {
   }

   public WeakReference<Condition> getReference() {
      return this.reference;
   }

   public abstract double calculateUtility(
      int var1, ArchetypeChunk<EntityStore> var2, Ref<EntityStore> var3, CommandBuffer<EntityStore> var4, EvaluationContext var5
   );

   public abstract int getSimplicity();

   @Nonnull
   @Override
   public String toString() {
      return "Condition{id='" + this.id + "'}";
   }

   @Nonnull
   public static Condition getAlwaysTrueFor(String id) {
      return new Condition.AlwaysTrueCondition(id);
   }

   static {
      CODEC.register("OwnStatPercent", SelfStatPercentageCondition.class, SelfStatPercentageCondition.CODEC);
      CODEC.register("TargetStatPercent", TargetStatPercentageCondition.class, TargetStatPercentageCondition.CODEC);
      CODEC.register("OwnStatAbsolute", SelfStatAbsoluteCondition.class, SelfStatAbsoluteCondition.CODEC);
      CODEC.register("TargetStatAbsolute", TargetStatAbsoluteCondition.class, TargetStatAbsoluteCondition.CODEC);
      CODEC.register("HasTarget", HasTargetCondition.class, HasTargetCondition.CODEC);
      CODEC.register("TimeOfDay", TimeOfDayCondition.class, TimeOfDayCondition.CODEC);
      CODEC.register("IsInState", IsInStateCondition.class, IsInStateCondition.CODEC);
      CODEC.register("NearbyCount", NearbyCountCondition.class, NearbyCountCondition.CODEC);
      CODEC.register("TimeSinceLastUsed", TimeSinceLastUsedCondition.class, TimeSinceLastUsedCondition.CODEC);
      CODEC.register("TargetDistance", TargetDistanceCondition.class, TargetDistanceCondition.CODEC);
      CODEC.register("Randomiser", RandomiserCondition.class, RandomiserCondition.CODEC);
      CODEC.register("LineOfSight", LineOfSightCondition.class, LineOfSightCondition.CODEC);
      CODEC.register("TargetMovementState", TargetMovementStateCondition.class, TargetMovementStateCondition.CODEC);
   }

   private static class AlwaysTrueCondition extends Condition {
      private AlwaysTrueCondition(String id) {
         super(id);
      }

      @Override
      public double calculateUtility(
         int selfIndex,
         ArchetypeChunk<EntityStore> archetypeChunk,
         Ref<EntityStore> target,
         CommandBuffer<EntityStore> commandBuffer,
         EvaluationContext context
      ) {
         return 1.0;
      }

      @Override
      public int getSimplicity() {
         return 0;
      }
   }
}
