package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.ArchetypeChunk;

public abstract class ArchetypeChunkSystem<ECS_TYPE> extends System<ECS_TYPE> implements QuerySystem<ECS_TYPE> {
   public abstract void onSystemAddedToArchetypeChunk(ArchetypeChunk<ECS_TYPE> var1);

   public abstract void onSystemRemovedFromArchetypeChunk(ArchetypeChunk<ECS_TYPE> var1);
}
