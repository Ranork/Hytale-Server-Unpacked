package com.hypixel.hytale.component.system.data;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.QuerySystem;
import com.hypixel.hytale.component.system.System;
import java.util.List;

public abstract class ArchetypeDataSystem<ECS_TYPE, Q, R> extends System<ECS_TYPE> implements QuerySystem<ECS_TYPE> {
   public abstract void fetch(ArchetypeChunk<ECS_TYPE> var1, Store<ECS_TYPE> var2, CommandBuffer<ECS_TYPE> var3, Q var4, List<R> var5);
}
