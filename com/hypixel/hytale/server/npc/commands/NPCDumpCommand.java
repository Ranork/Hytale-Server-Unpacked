package com.hypixel.hytale.server.npc.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class NPCDumpCommand extends NPCWorldCommandBase {
   @Nonnull
   private final FlagArg jsonArg = this.withFlagArg("json", "server.commands.npc.dump.json");

   public NPCDumpCommand() {
      super("dump", "server.commands.npc.dump.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull NPCEntity npc, @Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref
   ) {
      StringBuilder sb = new StringBuilder(npc.getRoleName());
      sb.append(":\n");
      Role role = npc.getRole();
      if (role != null) {
         if (!this.jsonArg.get(context)) {
            List<ComponentInfo> componentInfoList = new ObjectArrayList();
            dumpComponent(role, role, -1, 0, componentInfoList);

            for (ComponentInfo info : componentInfoList) {
               sb.append(info).append('\n');
            }
         } else {
            JsonObject obj = new JsonObject();
            dumpComponentsAsJson(role, role, -1, 0, obj);
            sb.append(obj);
         }
      }

      NPCPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
   }

   private static void dumpComponent(
      @Nonnull Role role, @Nonnull IAnnotatedComponent component, int index, int nestingDepth, @Nonnull List<ComponentInfo> infoList
   ) {
      ComponentInfo componentInfo = new ComponentInfo(component.getClass().getSimpleName(), index, nestingDepth);
      infoList.add(componentInfo);
      if (component instanceof IAnnotatedComponentCollection aggregate) {
         int nestedComponentCount = aggregate.componentCount();

         for (int i = 0; i < nestedComponentCount; i++) {
            IAnnotatedComponent nestedComponent = aggregate.getComponent(i);
            if (nestedComponent != null) {
               dumpComponent(role, nestedComponent, i, nestingDepth + 1, infoList);
            }
         }
      }

      component.getInfo(role, componentInfo);
   }

   private static void dumpComponentsAsJson(
      @Nonnull Role role, @Nonnull IAnnotatedComponent component, int index, int nestingDepth, @Nonnull JsonElement parent
   ) {
      ComponentInfo componentInfo = new ComponentInfo(component.getClass().getSimpleName(), index, nestingDepth);
      JsonObject object = parent.isJsonObject() ? parent.getAsJsonObject() : new JsonObject();
      object.add("name", new JsonPrimitive(componentInfo.getName()));
      if (componentInfo.getIndex() >= 0) {
         object.add("index", new JsonPrimitive(componentInfo.getIndex()));
      }

      if (component instanceof IAnnotatedComponentCollection aggregate) {
         JsonArray array = new JsonArray();
         object.add("children", array);
         int nestedComponentCount = aggregate.componentCount();

         for (int i = 0; i < nestedComponentCount; i++) {
            IAnnotatedComponent nestedComponent = aggregate.getComponent(i);
            if (nestedComponent != null) {
               dumpComponentsAsJson(role, nestedComponent, i, nestingDepth + 1, array);
            }
         }
      }

      component.getInfo(role, componentInfo);
      List<String> fields = componentInfo.getFields();
      if (!fields.isEmpty()) {
         JsonArray array = new JsonArray();

         for (String field : fields) {
            array.add(field);
         }

         object.add("parameters", array);
      }

      if (parent.isJsonArray()) {
         parent.getAsJsonArray().add(object);
      }
   }
}
