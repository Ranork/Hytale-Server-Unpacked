package com.hypixel.hytale.builtin.npccombatactionevaluator.memory;

import com.hypixel.hytale.builtin.npccombatactionevaluator.NPCCombatActionEvaluatorPlugin;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TargetMemory implements Component<EntityStore> {
   @Nonnull
   private final Int2FloatOpenHashMap knownFriendlies = new Int2FloatOpenHashMap();
   @Nonnull
   private final List<Ref<EntityStore>> knownFriendliesList = new ObjectArrayList();
   @Nonnull
   private final Int2FloatOpenHashMap knownHostiles = new Int2FloatOpenHashMap();
   @Nonnull
   private final List<Ref<EntityStore>> knownHostilesList = new ObjectArrayList();
   private final float rememberFor;
   @Nullable
   private Ref<EntityStore> closestHostile;

   public static ComponentType<EntityStore, TargetMemory> getComponentType() {
      return NPCCombatActionEvaluatorPlugin.get().getTargetMemoryComponentType();
   }

   public TargetMemory(float rememberFor) {
      this.rememberFor = rememberFor;
      this.knownFriendlies.defaultReturnValue(-1.0F);
      this.knownHostiles.defaultReturnValue(-1.0F);
   }

   @Nonnull
   public Int2FloatOpenHashMap getKnownFriendlies() {
      return this.knownFriendlies;
   }

   @Nonnull
   public List<Ref<EntityStore>> getKnownFriendliesList() {
      return this.knownFriendliesList;
   }

   @Nonnull
   public Int2FloatOpenHashMap getKnownHostiles() {
      return this.knownHostiles;
   }

   @Nonnull
   public List<Ref<EntityStore>> getKnownHostilesList() {
      return this.knownHostilesList;
   }

   public float getRememberFor() {
      return this.rememberFor;
   }

   @Nullable
   public Ref<EntityStore> getClosestHostile() {
      return this.closestHostile;
   }

   public void setClosestHostile(@Nullable Ref<EntityStore> ref) {
      this.closestHostile = ref;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new TargetMemory(this.rememberFor);
   }
}
