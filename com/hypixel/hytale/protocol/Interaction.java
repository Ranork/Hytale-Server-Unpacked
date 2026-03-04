package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Interaction {
   public static final int MAX_SIZE = 1677721605;
   @Nonnull
   public WaitForDataFrom waitForDataFrom = WaitForDataFrom.Client;
   @Nullable
   public InteractionEffects effects;
   public float horizontalSpeedMultiplier;
   public float runTime;
   public boolean cancelOnItemChange;
   @Nullable
   public Map<GameMode, InteractionSettings> settings;
   @Nullable
   public InteractionRules rules;
   @Nullable
   public int[] tags;
   @Nullable
   public InteractionCameraSettings camera;

   @Nonnull
   public static Interaction deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return (Interaction)(switch (typeId) {
         case 0 -> SimpleBlockInteraction.deserialize(buf, offset + typeIdLen);
         case 1 -> SimpleInteraction.deserialize(buf, offset + typeIdLen);
         case 2 -> PlaceBlockInteraction.deserialize(buf, offset + typeIdLen);
         case 3 -> BreakBlockInteraction.deserialize(buf, offset + typeIdLen);
         case 4 -> PickBlockInteraction.deserialize(buf, offset + typeIdLen);
         case 5 -> UseBlockInteraction.deserialize(buf, offset + typeIdLen);
         case 6 -> UseEntityInteraction.deserialize(buf, offset + typeIdLen);
         case 7 -> BuilderToolInteraction.deserialize(buf, offset + typeIdLen);
         case 8 -> ModifyInventoryInteraction.deserialize(buf, offset + typeIdLen);
         case 9 -> ChargingInteraction.deserialize(buf, offset + typeIdLen);
         case 10 -> WieldingInteraction.deserialize(buf, offset + typeIdLen);
         case 11 -> ChainingInteraction.deserialize(buf, offset + typeIdLen);
         case 12 -> ConditionInteraction.deserialize(buf, offset + typeIdLen);
         case 13 -> StatsConditionInteraction.deserialize(buf, offset + typeIdLen);
         case 14 -> BlockConditionInteraction.deserialize(buf, offset + typeIdLen);
         case 15 -> ReplaceInteraction.deserialize(buf, offset + typeIdLen);
         case 16 -> ChangeBlockInteraction.deserialize(buf, offset + typeIdLen);
         case 17 -> ChangeStateInteraction.deserialize(buf, offset + typeIdLen);
         case 18 -> FirstClickInteraction.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("Interaction", typeId);
         case 20 -> SelectInteraction.deserialize(buf, offset + typeIdLen);
         case 21 -> DamageEntityInteraction.deserialize(buf, offset + typeIdLen);
         case 22 -> RepeatInteraction.deserialize(buf, offset + typeIdLen);
         case 23 -> ParallelInteraction.deserialize(buf, offset + typeIdLen);
         case 24 -> ChangeActiveSlotInteraction.deserialize(buf, offset + typeIdLen);
         case 25 -> EffectConditionInteraction.deserialize(buf, offset + typeIdLen);
         case 26 -> ApplyForceInteraction.deserialize(buf, offset + typeIdLen);
         case 27 -> ApplyEffectInteraction.deserialize(buf, offset + typeIdLen);
         case 28 -> ClearEntityEffectInteraction.deserialize(buf, offset + typeIdLen);
         case 29 -> SerialInteraction.deserialize(buf, offset + typeIdLen);
         case 30 -> ChangeStatInteraction.deserialize(buf, offset + typeIdLen);
         case 31 -> MovementConditionInteraction.deserialize(buf, offset + typeIdLen);
         case 32 -> ProjectileInteraction.deserialize(buf, offset + typeIdLen);
         case 33 -> RemoveEntityInteraction.deserialize(buf, offset + typeIdLen);
         case 34 -> ResetCooldownInteraction.deserialize(buf, offset + typeIdLen);
         case 35 -> TriggerCooldownInteraction.deserialize(buf, offset + typeIdLen);
         case 36 -> CooldownConditionInteraction.deserialize(buf, offset + typeIdLen);
         case 37 -> ChainFlagInteraction.deserialize(buf, offset + typeIdLen);
         case 38 -> IncrementCooldownInteraction.deserialize(buf, offset + typeIdLen);
         case 39 -> CancelChainInteraction.deserialize(buf, offset + typeIdLen);
         case 40 -> RunRootInteraction.deserialize(buf, offset + typeIdLen);
         case 41 -> CameraInteraction.deserialize(buf, offset + typeIdLen);
         case 42 -> SpawnDeployableFromRaycastInteraction.deserialize(buf, offset + typeIdLen);
         case 43 -> MemoriesConditionInteraction.deserialize(buf, offset + typeIdLen);
         case 44 -> ToggleGliderInteraction.deserialize(buf, offset + typeIdLen);
      });
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);

      return typeIdLen + switch (typeId) {
         case 0 -> SimpleBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> SimpleInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> PlaceBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> BreakBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 4 -> PickBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 5 -> UseBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 6 -> UseEntityInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 7 -> BuilderToolInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 8 -> ModifyInventoryInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 9 -> ChargingInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 10 -> WieldingInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 11 -> ChainingInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 12 -> ConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 13 -> StatsConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 14 -> BlockConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 15 -> ReplaceInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 16 -> ChangeBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 17 -> ChangeStateInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 18 -> FirstClickInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("Interaction", typeId);
         case 20 -> SelectInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 21 -> DamageEntityInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 22 -> RepeatInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 23 -> ParallelInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 24 -> ChangeActiveSlotInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 25 -> EffectConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 26 -> ApplyForceInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 27 -> ApplyEffectInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 28 -> ClearEntityEffectInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 29 -> SerialInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 30 -> ChangeStatInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 31 -> MovementConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 32 -> ProjectileInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 33 -> RemoveEntityInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 34 -> ResetCooldownInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 35 -> TriggerCooldownInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 36 -> CooldownConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 37 -> ChainFlagInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 38 -> IncrementCooldownInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 39 -> CancelChainInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 40 -> RunRootInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 41 -> CameraInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 42 -> SpawnDeployableFromRaycastInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 43 -> MemoriesConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
         case 44 -> ToggleGliderInteraction.computeBytesConsumed(buf, offset + typeIdLen);
      };
   }

   public int getTypeId() {
      if (this instanceof BreakBlockInteraction sub) {
         return 3;
      } else if (this instanceof PickBlockInteraction sub) {
         return 4;
      } else if (this instanceof UseBlockInteraction sub) {
         return 5;
      } else if (this instanceof BlockConditionInteraction sub) {
         return 14;
      } else if (this instanceof ChangeBlockInteraction sub) {
         return 16;
      } else if (this instanceof ChangeStateInteraction sub) {
         return 17;
      } else if (this instanceof SimpleBlockInteraction sub) {
         return 0;
      } else if (this instanceof PlaceBlockInteraction sub) {
         return 2;
      } else if (this instanceof UseEntityInteraction sub) {
         return 6;
      } else if (this instanceof BuilderToolInteraction sub) {
         return 7;
      } else if (this instanceof ModifyInventoryInteraction sub) {
         return 8;
      } else if (this instanceof WieldingInteraction sub) {
         return 10;
      } else if (this instanceof ConditionInteraction sub) {
         return 12;
      } else if (this instanceof StatsConditionInteraction sub) {
         return 13;
      } else if (this instanceof SelectInteraction sub) {
         return 20;
      } else if (this instanceof RepeatInteraction sub) {
         return 22;
      } else if (this instanceof EffectConditionInteraction sub) {
         return 25;
      } else if (this instanceof ApplyForceInteraction sub) {
         return 26;
      } else if (this instanceof ApplyEffectInteraction sub) {
         return 27;
      } else if (this instanceof ClearEntityEffectInteraction sub) {
         return 28;
      } else if (this instanceof ChangeStatInteraction sub) {
         return 30;
      } else if (this instanceof MovementConditionInteraction sub) {
         return 31;
      } else if (this instanceof ProjectileInteraction sub) {
         return 32;
      } else if (this instanceof RemoveEntityInteraction sub) {
         return 33;
      } else if (this instanceof ResetCooldownInteraction sub) {
         return 34;
      } else if (this instanceof TriggerCooldownInteraction sub) {
         return 35;
      } else if (this instanceof CooldownConditionInteraction sub) {
         return 36;
      } else if (this instanceof ChainFlagInteraction sub) {
         return 37;
      } else if (this instanceof IncrementCooldownInteraction sub) {
         return 38;
      } else if (this instanceof CancelChainInteraction sub) {
         return 39;
      } else if (this instanceof RunRootInteraction sub) {
         return 40;
      } else if (this instanceof CameraInteraction sub) {
         return 41;
      } else if (this instanceof SpawnDeployableFromRaycastInteraction sub) {
         return 42;
      } else if (this instanceof ToggleGliderInteraction sub) {
         return 44;
      } else if (this instanceof SimpleInteraction sub) {
         return 1;
      } else if (this instanceof ChargingInteraction sub) {
         return 9;
      } else if (this instanceof ChainingInteraction sub) {
         return 11;
      } else if (this instanceof ReplaceInteraction sub) {
         return 15;
      } else if (this instanceof FirstClickInteraction sub) {
         return 18;
      } else if (this instanceof DamageEntityInteraction sub) {
         return 21;
      } else if (this instanceof ParallelInteraction sub) {
         return 23;
      } else if (this instanceof ChangeActiveSlotInteraction sub) {
         return 24;
      } else if (this instanceof SerialInteraction sub) {
         return 29;
      } else if (this instanceof MemoriesConditionInteraction sub) {
         return 43;
      } else {
         throw new IllegalStateException("Unknown subtype: " + this.getClass().getName());
      }
   }

   public abstract int serialize(@Nonnull ByteBuf var1);

   public abstract int computeSize();

   public int serializeWithTypeId(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      VarInt.write(buf, this.getTypeId());
      this.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   public int computeSizeWithTypeId() {
      return VarInt.size(this.getTypeId()) + this.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      int typeId = VarInt.peek(buffer, offset);
      int typeIdLen = VarInt.length(buffer, offset);

      return switch (typeId) {
         case 0 -> SimpleBlockInteraction.validateStructure(buffer, offset + typeIdLen);
         case 1 -> SimpleInteraction.validateStructure(buffer, offset + typeIdLen);
         case 2 -> PlaceBlockInteraction.validateStructure(buffer, offset + typeIdLen);
         case 3 -> BreakBlockInteraction.validateStructure(buffer, offset + typeIdLen);
         case 4 -> PickBlockInteraction.validateStructure(buffer, offset + typeIdLen);
         case 5 -> UseBlockInteraction.validateStructure(buffer, offset + typeIdLen);
         case 6 -> UseEntityInteraction.validateStructure(buffer, offset + typeIdLen);
         case 7 -> BuilderToolInteraction.validateStructure(buffer, offset + typeIdLen);
         case 8 -> ModifyInventoryInteraction.validateStructure(buffer, offset + typeIdLen);
         case 9 -> ChargingInteraction.validateStructure(buffer, offset + typeIdLen);
         case 10 -> WieldingInteraction.validateStructure(buffer, offset + typeIdLen);
         case 11 -> ChainingInteraction.validateStructure(buffer, offset + typeIdLen);
         case 12 -> ConditionInteraction.validateStructure(buffer, offset + typeIdLen);
         case 13 -> StatsConditionInteraction.validateStructure(buffer, offset + typeIdLen);
         case 14 -> BlockConditionInteraction.validateStructure(buffer, offset + typeIdLen);
         case 15 -> ReplaceInteraction.validateStructure(buffer, offset + typeIdLen);
         case 16 -> ChangeBlockInteraction.validateStructure(buffer, offset + typeIdLen);
         case 17 -> ChangeStateInteraction.validateStructure(buffer, offset + typeIdLen);
         case 18 -> FirstClickInteraction.validateStructure(buffer, offset + typeIdLen);
         default -> ValidationResult.error("Unknown polymorphic type ID " + typeId + " for Interaction");
         case 20 -> SelectInteraction.validateStructure(buffer, offset + typeIdLen);
         case 21 -> DamageEntityInteraction.validateStructure(buffer, offset + typeIdLen);
         case 22 -> RepeatInteraction.validateStructure(buffer, offset + typeIdLen);
         case 23 -> ParallelInteraction.validateStructure(buffer, offset + typeIdLen);
         case 24 -> ChangeActiveSlotInteraction.validateStructure(buffer, offset + typeIdLen);
         case 25 -> EffectConditionInteraction.validateStructure(buffer, offset + typeIdLen);
         case 26 -> ApplyForceInteraction.validateStructure(buffer, offset + typeIdLen);
         case 27 -> ApplyEffectInteraction.validateStructure(buffer, offset + typeIdLen);
         case 28 -> ClearEntityEffectInteraction.validateStructure(buffer, offset + typeIdLen);
         case 29 -> SerialInteraction.validateStructure(buffer, offset + typeIdLen);
         case 30 -> ChangeStatInteraction.validateStructure(buffer, offset + typeIdLen);
         case 31 -> MovementConditionInteraction.validateStructure(buffer, offset + typeIdLen);
         case 32 -> ProjectileInteraction.validateStructure(buffer, offset + typeIdLen);
         case 33 -> RemoveEntityInteraction.validateStructure(buffer, offset + typeIdLen);
         case 34 -> ResetCooldownInteraction.validateStructure(buffer, offset + typeIdLen);
         case 35 -> TriggerCooldownInteraction.validateStructure(buffer, offset + typeIdLen);
         case 36 -> CooldownConditionInteraction.validateStructure(buffer, offset + typeIdLen);
         case 37 -> ChainFlagInteraction.validateStructure(buffer, offset + typeIdLen);
         case 38 -> IncrementCooldownInteraction.validateStructure(buffer, offset + typeIdLen);
         case 39 -> CancelChainInteraction.validateStructure(buffer, offset + typeIdLen);
         case 40 -> RunRootInteraction.validateStructure(buffer, offset + typeIdLen);
         case 41 -> CameraInteraction.validateStructure(buffer, offset + typeIdLen);
         case 42 -> SpawnDeployableFromRaycastInteraction.validateStructure(buffer, offset + typeIdLen);
         case 43 -> MemoriesConditionInteraction.validateStructure(buffer, offset + typeIdLen);
         case 44 -> ToggleGliderInteraction.validateStructure(buffer, offset + typeIdLen);
      };
   }
}
