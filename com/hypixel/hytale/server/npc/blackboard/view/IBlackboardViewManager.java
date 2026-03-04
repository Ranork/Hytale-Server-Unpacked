package com.hypixel.hytale.server.npc.blackboard.view;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import java.util.function.Consumer;

public interface IBlackboardViewManager<View extends IBlackboardView<View>> {
   View get(Ref<EntityStore> var1, Blackboard var2, ComponentAccessor<EntityStore> var3);

   View get(Vector3d var1, Blackboard var2);

   View get(int var1, int var2, Blackboard var3);

   View get(long var1, Blackboard var3);

   View getIfExists(long var1);

   void cleanup();

   void onWorldRemoved();

   void forEachView(Consumer<View> var1);

   void clear();
}
