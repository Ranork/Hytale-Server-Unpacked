package com.hypixel.hytale.server.npc.blackboard.view.event;

import com.hypixel.hytale.common.util.ListUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.IntObjectConsumer;
import com.hypixel.hytale.function.predicate.BiIntPredicate;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EventTypeRegistration<EventType extends Enum<EventType>, NotificationType extends EventNotification> {
   @Nullable
   private static final ComponentType<EntityStore, NPCEntity> NPC_COMPONENT_TYPE = NPCEntity.getComponentType();
   private final EventType type;
   private final BitSet eventSets = new BitSet();
   private final Int2ObjectMap<List<Ref<EntityStore>>> entitiesBySet = new Int2ObjectOpenHashMap();
   private final BiIntPredicate setTester;
   private final IEventCallback<EventType, NotificationType> eventCallback;

   public EventTypeRegistration(EventType type, BiIntPredicate setTester, IEventCallback<EventType, NotificationType> eventCallback) {
      this.type = type;
      this.setTester = setTester;
      this.eventCallback = eventCallback;
   }

   public void initialiseEntity(Ref<EntityStore> ref, @Nonnull IntSet changeSets) {
      IntIterator it = changeSets.iterator();

      while (it.hasNext()) {
         int set = it.nextInt();
         this.eventSets.set(set);
         ((List)this.entitiesBySet.computeIfAbsent(set, k -> new ObjectArrayList())).add(ref);
      }
   }

   public void relayEvent(
      int senderTypeId,
      @Nonnull NotificationType reusableEventNotification,
      Ref<EntityStore> skipEntityReference,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Ref<EntityStore> initiator = reusableEventNotification.getInitiator();

      for (int set = this.eventSets.nextSetBit(0); set >= 0; set = this.eventSets.nextSetBit(set + 1)) {
         if (this.setTester.test(set, senderTypeId)) {
            List<Ref<EntityStore>> entities = (List<Ref<EntityStore>>)this.entitiesBySet.get(set);
            if (entities != null) {
               reusableEventNotification.setSet(set);

               for (int j = 0; j < entities.size(); j++) {
                  Ref<EntityStore> entity = entities.get(j);
                  if (entity.isValid() && !entity.equals(initiator) && !entity.equals(skipEntityReference)) {
                     NPCEntity npc = componentAccessor.getComponent(entity, NPC_COMPONENT_TYPE);
                     this.eventCallback.notify(npc, this.type, reusableEventNotification);
                  }
               }
            }
         }

         if (set == Integer.MAX_VALUE) {
            break;
         }
      }
   }

   public int getSetCount() {
      return this.eventSets.cardinality();
   }

   public void forEach(@Nonnull IntObjectConsumer<EventType> setConsumer, @Nonnull Consumer<Ref<EntityStore>> npcConsumer) {
      for (int set = this.eventSets.nextSetBit(0); set >= 0; set = this.eventSets.nextSetBit(set + 1)) {
         setConsumer.accept(set, this.type);
         List<Ref<EntityStore>> entities = (List<Ref<EntityStore>>)this.entitiesBySet.get(set);
         if (entities != null) {
            for (int i = 0; i < entities.size(); i++) {
               Ref<EntityStore> entity = entities.get(i);
               if (entity.isValid()) {
                  npcConsumer.accept(entity);
               }
            }

            if (set == Integer.MAX_VALUE) {
               break;
            }
         }
      }
   }

   public void cleanup() {
      for (int set = this.eventSets.nextSetBit(0); set >= 0; set = this.eventSets.nextSetBit(set + 1)) {
         List<Ref<EntityStore>> entities = (List<Ref<EntityStore>>)this.entitiesBySet.getOrDefault(set, null);
         if (entities != null) {
            ListUtil.removeIf(entities, r -> !r.isValid());
            if (entities.isEmpty()) {
               this.eventSets.clear(set);
            }
         }

         if (set == Integer.MAX_VALUE) {
            break;
         }
      }
   }
}
