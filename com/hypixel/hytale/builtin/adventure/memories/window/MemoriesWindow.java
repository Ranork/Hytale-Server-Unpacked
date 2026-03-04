package com.hypixel.hytale.builtin.adventure.memories.window;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.adventure.memories.component.PlayerMemories;
import com.hypixel.hytale.builtin.adventure.memories.memories.Memory;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MemoriesWindow extends Window {
   @Nonnull
   private final JsonObject windowData = new JsonObject();

   public MemoriesWindow() {
      super(WindowType.Memories);
   }

   @Nonnull
   @Override
   public JsonObject getData() {
      return this.windowData;
   }

   @Override
   public boolean onOpen0(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      JsonArray array = new JsonArray();
      PlayerMemories playerMemoriesComponent = store.getComponent(ref, PlayerMemories.getComponentType());
      if (playerMemoriesComponent != null) {
         this.windowData.addProperty("capacity", playerMemoriesComponent.getMemoriesCapacity());

         for (Memory memory : playerMemoriesComponent.getRecordedMemories()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("title", memory.getTitle());
            obj.add("tooltipText", BsonUtil.translateBsonToJson(Message.CODEC.encode(memory.getTooltipText(), EmptyExtraInfo.EMPTY).asDocument()));
            String iconPath = memory.getIconPath();
            if (iconPath != null && !iconPath.isEmpty()) {
               obj.addProperty("icon", iconPath);
            }

            String category = GetCategoryIconPathForMemory(memory);
            if (category != null) {
               obj.addProperty("categoryIcon", category);
            }

            array.add(obj);
         }
      } else {
         this.windowData.addProperty("capacity", 0);
      }

      this.windowData.add("memories", array);
      this.invalidate();
      return true;
   }

   @Nullable
   private static String GetCategoryIconPathForMemory(@Nonnull Memory memory) {
      Map<String, Set<Memory>> allMemories = MemoriesPlugin.get().getAllMemories();

      for (Entry<String, Set<Memory>> entry : allMemories.entrySet()) {
         if (entry.getValue().contains(memory)) {
            String memoryCategoryIconBasePath = "UI/Custom/Pages/Memories/categories/%s.png";
            return String.format("UI/Custom/Pages/Memories/categories/%s.png", entry.getKey());
         }
      }

      return null;
   }

   @Override
   public void onClose0(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
   }
}
