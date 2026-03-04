package com.hypixel.hytale.builtin.adventure.objectives.components;

import com.hypixel.hytale.builtin.adventure.objectives.historydata.ObjectiveHistoryData;
import com.hypixel.hytale.builtin.adventure.objectives.historydata.ObjectiveLineHistoryData;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class ObjectiveHistoryComponent implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<ObjectiveHistoryComponent> CODEC = BuilderCodec.builder(ObjectiveHistoryComponent.class, ObjectiveHistoryComponent::new)
      .append(
         new KeyedCodec<>("ObjectiveHistory", new MapCodec<>(ObjectiveHistoryData.CODEC, Object2ObjectOpenHashMap::new, false)),
         (objectiveHistoryComponent, stringObjectiveHistoryDataMap) -> objectiveHistoryComponent.objectiveHistoryMap = stringObjectiveHistoryDataMap,
         objectiveHistoryComponent -> objectiveHistoryComponent.objectiveHistoryMap
      )
      .add()
      .append(
         new KeyedCodec<>("ObjectiveLineHistory", new MapCodec<>(ObjectiveLineHistoryData.CODEC, Object2ObjectOpenHashMap::new, false)),
         (objectiveHistoryComponent, stringObjectiveLineHistoryDataMap) -> objectiveHistoryComponent.objectiveLineHistoryMap = stringObjectiveLineHistoryDataMap,
         objectiveHistoryComponent -> objectiveHistoryComponent.objectiveLineHistoryMap
      )
      .add()
      .build();
   private Map<String, ObjectiveHistoryData> objectiveHistoryMap = new Object2ObjectOpenHashMap();
   private Map<String, ObjectiveLineHistoryData> objectiveLineHistoryMap = new Object2ObjectOpenHashMap();

   public Map<String, ObjectiveHistoryData> getObjectiveHistoryMap() {
      return this.objectiveHistoryMap;
   }

   public Map<String, ObjectiveLineHistoryData> getObjectiveLineHistoryMap() {
      return this.objectiveLineHistoryMap;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      ObjectiveHistoryComponent component = new ObjectiveHistoryComponent();
      component.objectiveHistoryMap.putAll(this.objectiveHistoryMap);
      component.objectiveLineHistoryMap.putAll(this.objectiveLineHistoryMap);
      return component;
   }
}
