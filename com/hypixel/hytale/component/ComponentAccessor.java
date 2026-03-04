package com.hypixel.hytale.component;

import com.hypixel.hytale.component.event.EntityEventType;
import com.hypixel.hytale.component.event.WorldEventType;
import com.hypixel.hytale.component.system.EcsEvent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ComponentAccessor<ECS_TYPE> {
   @Nullable
   <T extends Component<ECS_TYPE>> T getComponent(@Nonnull Ref<ECS_TYPE> var1, @Nonnull ComponentType<ECS_TYPE, T> var2);

   @Nonnull
   <T extends Component<ECS_TYPE>> T ensureAndGetComponent(@Nonnull Ref<ECS_TYPE> var1, @Nonnull ComponentType<ECS_TYPE, T> var2);

   @Nonnull
   Archetype<ECS_TYPE> getArchetype(@Nonnull Ref<ECS_TYPE> var1);

   @Nonnull
   <T extends Resource<ECS_TYPE>> T getResource(@Nonnull ResourceType<ECS_TYPE, T> var1);

   @Nonnull
   ECS_TYPE getExternalData();

   <T extends Component<ECS_TYPE>> void putComponent(@Nonnull Ref<ECS_TYPE> var1, @Nonnull ComponentType<ECS_TYPE, T> var2, @Nonnull T var3);

   <T extends Component<ECS_TYPE>> void addComponent(@Nonnull Ref<ECS_TYPE> var1, @Nonnull ComponentType<ECS_TYPE, T> var2, @Nonnull T var3);

   <T extends Component<ECS_TYPE>> T addComponent(@Nonnull Ref<ECS_TYPE> var1, @Nonnull ComponentType<ECS_TYPE, T> var2);

   Ref<ECS_TYPE>[] addEntities(@Nonnull Holder<ECS_TYPE>[] var1, @Nonnull AddReason var2);

   @Nullable
   Ref<ECS_TYPE> addEntity(@Nonnull Holder<ECS_TYPE> var1, @Nonnull AddReason var2);

   @Nonnull
   Holder<ECS_TYPE> removeEntity(@Nonnull Ref<ECS_TYPE> var1, @Nonnull Holder<ECS_TYPE> var2, @Nonnull RemoveReason var3);

   <T extends Component<ECS_TYPE>> void removeComponent(@Nonnull Ref<ECS_TYPE> var1, @Nonnull ComponentType<ECS_TYPE, T> var2);

   <T extends Component<ECS_TYPE>> void tryRemoveComponent(@Nonnull Ref<ECS_TYPE> var1, @Nonnull ComponentType<ECS_TYPE, T> var2);

   <Event extends EcsEvent> void invoke(@Nonnull Ref<ECS_TYPE> var1, @Nonnull Event var2);

   <Event extends EcsEvent> void invoke(@Nonnull EntityEventType<ECS_TYPE, Event> var1, @Nonnull Ref<ECS_TYPE> var2, @Nonnull Event var3);

   <Event extends EcsEvent> void invoke(@Nonnull Event var1);

   <Event extends EcsEvent> void invoke(@Nonnull WorldEventType<ECS_TYPE, Event> var1, @Nonnull Event var2);
}
