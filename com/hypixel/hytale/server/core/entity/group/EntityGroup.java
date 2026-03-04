package com.hypixel.hytale.server.core.entity.group;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.IntBiObjectConsumer;
import com.hypixel.hytale.function.consumer.IntTriObjectConsumer;
import com.hypixel.hytale.function.consumer.QuadConsumer;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityGroup implements Component<EntityStore> {
   @Nonnull
   private final Set<Ref<EntityStore>> memberSet = new HashSet<>();
   @Nonnull
   private final List<Ref<EntityStore>> memberList = new ObjectArrayList();
   @Nullable
   private Ref<EntityStore> leaderRef;
   private boolean dissolved;

   public static ComponentType<EntityStore, EntityGroup> getComponentType() {
      return EntityModule.get().getEntityGroupComponentType();
   }

   @Nullable
   public Ref<EntityStore> getLeaderRef() {
      return this.leaderRef;
   }

   public void setLeaderRef(@Nonnull Ref<EntityStore> leaderRef) {
      this.leaderRef = leaderRef;
   }

   public void add(@Nonnull Ref<EntityStore> reference) {
      if (!this.memberSet.add(reference)) {
         throw new IllegalStateException("Attempting to add entity " + reference + " that is already a member of the group!");
      } else {
         this.memberList.add(reference);
      }
   }

   public void remove(@Nonnull Ref<EntityStore> reference) {
      if (!this.memberSet.remove(reference)) {
         throw new IllegalStateException("Attempting to remove entity " + reference + " that is not a member of the group!");
      } else {
         this.memberList.remove(reference);
      }
   }

   @Nullable
   public Ref<EntityStore> getFirst() {
      return !this.memberList.isEmpty() ? this.memberList.getFirst() : null;
   }

   @Nonnull
   public List<Ref<EntityStore>> getMemberList() {
      return this.memberList;
   }

   public int size() {
      return this.memberSet.size();
   }

   public boolean isDissolved() {
      return this.dissolved;
   }

   public void setDissolved(boolean dissolved) {
      this.dissolved = dissolved;
   }

   public void clear() {
      this.memberSet.clear();
      this.memberList.clear();
      this.leaderRef = null;
      this.dissolved = true;
   }

   public boolean isMember(Ref<EntityStore> reference) {
      return !this.dissolved && this.memberSet.contains(reference);
   }

   public <T> void forEachMemberExcludingLeader(@Nonnull TriConsumer<Ref<EntityStore>, Ref<EntityStore>, T> consumer, Ref<EntityStore> sender, T arg) {
      this.forEachMember(consumer, sender, arg, this.leaderRef);
   }

   public <T> void forEachMemberExcludingSelf(@Nonnull TriConsumer<Ref<EntityStore>, Ref<EntityStore>, T> consumer, Ref<EntityStore> sender, T arg) {
      this.forEachMember(consumer, sender, arg, sender);
   }

   public <T> void forEachMember(@Nonnull TriConsumer<Ref<EntityStore>, Ref<EntityStore>, T> consumer, Ref<EntityStore> sender, T arg) {
      this.forEachMember(consumer, sender, arg, null);
   }

   public <T> void forEachMember(
      @Nonnull TriConsumer<Ref<EntityStore>, Ref<EntityStore>, T> consumer, Ref<EntityStore> sender, T arg, Ref<EntityStore> excludeReference
   ) {
      for (int i = 0; i < this.memberList.size(); i++) {
         Ref<EntityStore> member = this.memberList.get(i);
         if (member.isValid() && !member.equals(excludeReference)) {
            consumer.accept(member, sender, arg);
         }
      }
   }

   public <T, V> void forEachMemberExcludingLeader(@Nonnull QuadConsumer<Ref<EntityStore>, Ref<EntityStore>, T, V> consumer, Ref<EntityStore> sender, T t, V v) {
      this.forEachMember(consumer, sender, t, v, this.leaderRef);
   }

   public <T, V> void forEachMemberExcludingSelf(@Nonnull QuadConsumer<Ref<EntityStore>, Ref<EntityStore>, T, V> consumer, Ref<EntityStore> sender, T t, V v) {
      this.forEachMember(consumer, sender, t, v, sender);
   }

   public <T, V> void forEachMember(@Nonnull QuadConsumer<Ref<EntityStore>, Ref<EntityStore>, T, V> consumer, Ref<EntityStore> sender, T t, V v) {
      this.forEachMember(consumer, sender, t, v, null);
   }

   public <T, V> void forEachMember(
      @Nonnull QuadConsumer<Ref<EntityStore>, Ref<EntityStore>, T, V> consumer, Ref<EntityStore> sender, T t, V v, Ref<EntityStore> excludeReference
   ) {
      for (int i = 0; i < this.memberList.size(); i++) {
         Ref<EntityStore> member = this.memberList.get(i);
         if (member.isValid() && !member.equals(excludeReference)) {
            consumer.accept(member, sender, t, v);
         }
      }
   }

   public <T, V> void forEachMemberExcludingLeader(
      @Nonnull IntTriObjectConsumer<Ref<EntityStore>, Ref<EntityStore>, T> consumer, Ref<EntityStore> sender, T t, int value
   ) {
      this.forEachMember(consumer, sender, t, value, this.leaderRef);
   }

   public <T, V> void forEachMemberExcludingSelf(
      @Nonnull IntTriObjectConsumer<Ref<EntityStore>, Ref<EntityStore>, T> consumer, @Nonnull Ref<EntityStore> sender, T t, int value
   ) {
      this.forEachMember(consumer, sender, t, value, sender);
   }

   public <T> void forEachMember(@Nonnull IntTriObjectConsumer<Ref<EntityStore>, Ref<EntityStore>, T> consumer, Ref<EntityStore> sender, T t, int value) {
      this.forEachMember(consumer, sender, t, value, null);
   }

   public <T> void forEachMember(
      @Nonnull IntTriObjectConsumer<Ref<EntityStore>, Ref<EntityStore>, T> consumer, Ref<EntityStore> sender, T t, int value, Ref<EntityStore> excludeReference
   ) {
      for (int i = 0; i < this.memberList.size(); i++) {
         Ref<EntityStore> member = this.memberList.get(i);
         if (member.isValid() && !member.equals(excludeReference)) {
            consumer.accept(value, member, sender, t);
         }
      }
   }

   public <T> void forEachMember(@Nonnull IntBiObjectConsumer<Ref<EntityStore>, T> consumer, T t) {
      for (int i = 0; i < this.memberList.size(); i++) {
         Ref<EntityStore> member = this.memberList.get(i);
         if (member.isValid()) {
            consumer.accept(i, member, t);
         }
      }
   }

   @Nullable
   public Ref<EntityStore> testMembers(@Nonnull Predicate<Ref<EntityStore>> predicate, boolean skipLeader) {
      for (int i = 0; i < this.memberList.size(); i++) {
         Ref<EntityStore> member = this.memberList.get(i);
         if (member.isValid() && (!skipLeader || !member.equals(this.leaderRef)) && predicate.test(member)) {
            return member;
         }
      }

      return null;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      EntityGroup group = new EntityGroup();
      group.memberSet.addAll(this.memberSet);
      group.memberList.addAll(this.memberList);
      group.leaderRef = this.leaderRef;
      group.dissolved = this.dissolved;
      return group;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntityGroup{leader="
         + this.leaderRef
         + ", memberSet="
         + this.memberSet
         + ", memberList="
         + this.memberList
         + ", dissolved="
         + this.dissolved
         + "}";
   }
}
