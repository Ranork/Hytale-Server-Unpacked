package com.hypixel.hytale.server.core.asset.type.blocktype.config.bench;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.ObjectCodecMapCodec;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Bench implements NetworkSerializable<com.hypixel.hytale.protocol.Bench> {
   public static final ObjectCodecMapCodec<BenchType, Bench> CODEC = new ObjectCodecMapCodec<>("Type", new EnumCodec<>(BenchType.class));
   public static final BuilderCodec<Bench> BASE_CODEC = BuilderCodec.abstractBuilder(Bench.class)
      .addField(new KeyedCodec<>("Id", Codec.STRING), (bench, s) -> bench.id = s, bench -> bench.id)
      .addField(new KeyedCodec<>("DescriptiveLabel", Codec.STRING), (bench, s) -> bench.descriptiveLabel = s, bench -> bench.descriptiveLabel)
      .appendInherited(
         new KeyedCodec<>("TierLevels", new ArrayCodec<>(BenchTierLevel.CODEC, BenchTierLevel[]::new)),
         (bench, u) -> bench.tierLevels = u,
         bench -> bench.tierLevels,
         (bench, parent) -> bench.tierLevels = parent.tierLevels
      )
      .add()
      .<String>append(
         new KeyedCodec<>("LocalOpenSoundEventId", Codec.STRING), (bench, s) -> bench.localOpenSoundEventId = s, bench -> bench.localOpenSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.ONESHOT)
      .add()
      .<String>append(
         new KeyedCodec<>("LocalCloseSoundEventId", Codec.STRING), (bench, s) -> bench.localCloseSoundEventId = s, bench -> bench.localCloseSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.ONESHOT)
      .add()
      .<String>append(
         new KeyedCodec<>("CompletedSoundEventId", Codec.STRING), (bench, s) -> bench.completedSoundEventId = s, bench -> bench.completedSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .addValidator(SoundEventValidators.ONESHOT)
      .add()
      .<String>append(new KeyedCodec<>("FailedSoundEventId", Codec.STRING), (bench, s) -> bench.failedSoundEventId = s, bench -> bench.failedSoundEventId)
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .addValidator(SoundEventValidators.ONESHOT)
      .add()
      .<String>append(
         new KeyedCodec<>("BenchUpgradeSoundEventId", Codec.STRING), (bench, s) -> bench.benchUpgradeSoundEventId = s, bench -> bench.benchUpgradeSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .addValidator(SoundEventValidators.ONESHOT)
      .add()
      .<String>append(
         new KeyedCodec<>("BenchUpgradeCompletedSoundEventId", Codec.STRING),
         (bench, s) -> bench.benchUpgradeCompletedSoundEventId = s,
         bench -> bench.benchUpgradeCompletedSoundEventId
      )
      .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
      .addValidator(SoundEventValidators.MONO)
      .addValidator(SoundEventValidators.ONESHOT)
      .add()
      .afterDecode(bench -> {
         bench.type = CODEC.getIdFor((Class<? extends Bench>)bench.getClass());
         if (bench.localOpenSoundEventId != null) {
            bench.localOpenSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.localOpenSoundEventId);
         }

         if (bench.localCloseSoundEventId != null) {
            bench.localCloseSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.localCloseSoundEventId);
         }

         if (bench.completedSoundEventId != null) {
            bench.completedSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.completedSoundEventId);
         }

         if (bench.benchUpgradeSoundEventId != null) {
            bench.benchUpgradeSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.benchUpgradeSoundEventId);
         }

         if (bench.benchUpgradeCompletedSoundEventId != null) {
            bench.benchUpgradeCompletedSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.benchUpgradeCompletedSoundEventId);
         }

         if (bench.failedSoundEventId != null) {
            bench.failedSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.failedSoundEventId);
         }
      })
      .build();
   @Deprecated(forRemoval = true)
   protected static final Map<BenchType, RootInteraction> BENCH_INTERACTIONS = new EnumMap<>(BenchType.class);
   @Nonnull
   protected BenchType type = BenchType.Crafting;
   protected String id;
   protected String descriptiveLabel;
   protected BenchTierLevel[] tierLevels;
   @Nullable
   protected String localOpenSoundEventId = null;
   protected transient int localOpenSoundEventIndex = 0;
   @Nullable
   protected String localCloseSoundEventId = null;
   protected transient int localCloseSoundEventIndex = 0;
   @Nullable
   protected String completedSoundEventId = null;
   protected transient int completedSoundEventIndex = 0;
   @Nullable
   protected String failedSoundEventId = null;
   protected transient int failedSoundEventIndex = 0;
   @Nullable
   protected String benchUpgradeSoundEventId = null;
   protected transient int benchUpgradeSoundEventIndex = 0;
   @Nullable
   protected String benchUpgradeCompletedSoundEventId = null;
   protected transient int benchUpgradeCompletedSoundEventIndex = 0;

   public BenchType getType() {
      return this.type;
   }

   public String getId() {
      return this.id;
   }

   public String getDescriptiveLabel() {
      return this.descriptiveLabel;
   }

   public BenchTierLevel getTierLevel(int tierLevel) {
      return this.tierLevels != null && tierLevel >= 1 && tierLevel <= this.tierLevels.length ? this.tierLevels[tierLevel - 1] : null;
   }

   public BenchUpgradeRequirement getUpgradeRequirement(int tierLevel) {
      BenchTierLevel currentTierLevel = this.getTierLevel(tierLevel);
      return currentTierLevel == null ? null : currentTierLevel.upgradeRequirement;
   }

   @Nullable
   public String getLocalOpenSoundEventId() {
      return this.localOpenSoundEventId;
   }

   public int getLocalOpenSoundEventIndex() {
      return this.localOpenSoundEventIndex;
   }

   @Nullable
   public String getLocalCloseSoundEventId() {
      return this.localCloseSoundEventId;
   }

   public int getLocalCloseSoundEventIndex() {
      return this.localCloseSoundEventIndex;
   }

   @Nullable
   public String getCompletedSoundEventId() {
      return this.completedSoundEventId;
   }

   public int getCompletedSoundEventIndex() {
      return this.completedSoundEventIndex;
   }

   @Nullable
   public String getFailedSoundEventId() {
      return this.failedSoundEventId;
   }

   public int getFailedSoundEventIndex() {
      return this.failedSoundEventIndex;
   }

   @Nullable
   public String getBenchUpgradeSoundEventId() {
      return this.benchUpgradeSoundEventId;
   }

   public int getBenchUpgradeSoundEventIndex() {
      return this.benchUpgradeSoundEventIndex;
   }

   @Nullable
   public String getBenchUpgradeCompletedSoundEventId() {
      return this.benchUpgradeCompletedSoundEventId;
   }

   public int getBenchUpgradeCompletedSoundEventIndex() {
      return this.benchUpgradeCompletedSoundEventIndex;
   }

   @Nullable
   public RootInteraction getRootInteraction() {
      return BENCH_INTERACTIONS.get(this.type);
   }

   public com.hypixel.hytale.protocol.Bench toPacket() {
      com.hypixel.hytale.protocol.Bench packet = new com.hypixel.hytale.protocol.Bench();
      if (this.tierLevels != null && this.tierLevels.length > 0) {
         packet.benchTierLevels = new com.hypixel.hytale.protocol.BenchTierLevel[this.tierLevels.length];

         for (int i = 0; i < this.tierLevels.length; i++) {
            packet.benchTierLevels[i] = this.tierLevels[i].toPacket();
         }
      }

      return packet;
   }

   @Override
   public boolean equals(Object o) {
      if (o != null && this.getClass() == o.getClass()) {
         Bench bench = (Bench)o;
         return this.localOpenSoundEventIndex == bench.localOpenSoundEventIndex
            && this.localCloseSoundEventIndex == bench.localCloseSoundEventIndex
            && this.completedSoundEventIndex == bench.completedSoundEventIndex
            && this.benchUpgradeSoundEventIndex == bench.benchUpgradeSoundEventIndex
            && this.benchUpgradeCompletedSoundEventIndex == bench.benchUpgradeCompletedSoundEventIndex
            && this.type == bench.type
            && Objects.equals(this.id, bench.id)
            && Objects.equals(this.descriptiveLabel, bench.descriptiveLabel)
            && Objects.deepEquals(this.tierLevels, bench.tierLevels)
            && Objects.equals(this.localOpenSoundEventId, bench.localOpenSoundEventId)
            && Objects.equals(this.localCloseSoundEventId, bench.localCloseSoundEventId)
            && Objects.equals(this.completedSoundEventId, bench.completedSoundEventId)
            && Objects.equals(this.failedSoundEventId, bench.failedSoundEventId)
            && Objects.equals(this.benchUpgradeSoundEventId, bench.benchUpgradeSoundEventId)
            && Objects.equals(this.benchUpgradeCompletedSoundEventId, bench.benchUpgradeCompletedSoundEventId);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.type,
         this.id,
         this.descriptiveLabel,
         Arrays.hashCode((Object[])this.tierLevels),
         this.localOpenSoundEventId,
         this.localOpenSoundEventIndex,
         this.localCloseSoundEventId,
         this.localCloseSoundEventIndex,
         this.completedSoundEventId,
         this.completedSoundEventIndex,
         this.failedSoundEventId,
         this.failedSoundEventIndex,
         this.benchUpgradeSoundEventId,
         this.benchUpgradeSoundEventIndex,
         this.benchUpgradeCompletedSoundEventId,
         this.benchUpgradeCompletedSoundEventIndex
      );
   }

   @Override
   public String toString() {
      return "Bench{type="
         + this.type
         + ", id='"
         + this.id
         + "', descriptiveLabel='"
         + this.descriptiveLabel
         + "', tierLevels="
         + Arrays.toString((Object[])this.tierLevels)
         + ", localOpenSoundEventId='"
         + this.localOpenSoundEventId
         + "', localOpenSoundEventIndex="
         + this.localOpenSoundEventIndex
         + ", localCloseSoundEventId='"
         + this.localCloseSoundEventId
         + "', localCloseSoundEventIndex="
         + this.localCloseSoundEventIndex
         + ", completedSoundEventId='"
         + this.completedSoundEventId
         + "', completedSoundEventIndex="
         + this.completedSoundEventIndex
         + ", failedSoundEventId='"
         + this.failedSoundEventId
         + "', failedSoundEventIndex="
         + this.failedSoundEventIndex
         + ", benchUpgradeSoundEventId='"
         + this.benchUpgradeSoundEventId
         + "', benchUpgradeSoundEventIndex="
         + this.benchUpgradeSoundEventIndex
         + ", benchUpgradeCompletedSoundEventId='"
         + this.benchUpgradeCompletedSoundEventId
         + "', benchUpgradeCompletedSoundEventIndex="
         + this.benchUpgradeCompletedSoundEventIndex
         + "}";
   }

   @Deprecated(forRemoval = true)
   public static void registerRootInteraction(BenchType benchType, RootInteraction interaction) {
      BENCH_INTERACTIONS.put(benchType, interaction);
   }

   public static class BenchSlot {
      public static final BuilderCodec<Bench.BenchSlot> CODEC = BuilderCodec.builder(Bench.BenchSlot.class, Bench.BenchSlot::new)
         .addField(new KeyedCodec<>("Icon", Codec.STRING), (benchSlot, s) -> benchSlot.icon = s, benchSlot -> benchSlot.icon)
         .build();
      protected String icon;

      protected BenchSlot() {
      }

      public String getIcon() {
         return this.icon;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Bench.BenchSlot benchSlot = (Bench.BenchSlot)o;
            return this.icon != null ? this.icon.equals(benchSlot.icon) : benchSlot.icon == null;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.icon != null ? this.icon.hashCode() : 0;
      }

      @Nonnull
      @Override
      public String toString() {
         return "BenchSlot{icon='" + this.icon + "'}";
      }
   }
}
