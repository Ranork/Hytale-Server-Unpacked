package com.hypixel.hytale.component;

import com.hypixel.hytale.component.event.EntityEventType;
import com.hypixel.hytale.component.event.WorldEventType;
import com.hypixel.hytale.component.system.EcsEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandBuffer<ECS_TYPE> implements ComponentAccessor<ECS_TYPE> {
   @Nonnull
   private final Store<ECS_TYPE> store;
   @Nonnull
   private final Deque<Consumer<Store<ECS_TYPE>>> queue = new ArrayDeque<>();
   @Nullable
   private Ref<ECS_TYPE> trackedRef;
   private boolean trackedRefRemoved;
   @Nullable
   private CommandBuffer<ECS_TYPE> parentBuffer;
   @Nullable
   private Thread thread;

   protected CommandBuffer(@Nonnull Store<ECS_TYPE> store) {
      this.store = store;

      assert this.setThread();
   }

   @Nonnull
   public Store<ECS_TYPE> getStore() {
      return this.store;
   }

   public void run(@Nonnull Consumer<Store<ECS_TYPE>> consumer) {
      assert Thread.currentThread() == this.thread;

      this.queue.add(consumer);
   }

   @Override
   public <T extends Component<ECS_TYPE>> T getComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      assert Thread.currentThread() == this.thread;

      return this.store.__internal_getComponent(ref, componentType);
   }

   @Nonnull
   @Override
   public Archetype<ECS_TYPE> getArchetype(@Nonnull Ref<ECS_TYPE> ref) {
      assert Thread.currentThread() == this.thread;

      return this.store.__internal_getArchetype(ref);
   }

   @Nonnull
   @Override
   public <T extends Resource<ECS_TYPE>> T getResource(@Nonnull ResourceType<ECS_TYPE, T> resourceType) {
      assert Thread.currentThread() == this.thread;

      return this.store.__internal_getResource(resourceType);
   }

   @Nonnull
   @Override
   public ECS_TYPE getExternalData() {
      return this.store.getExternalData();
   }

   @Nonnull
   @Override
   public Ref<ECS_TYPE>[] addEntities(@Nonnull Holder<ECS_TYPE>[] holders, @Nonnull AddReason reason) {
      assert Thread.currentThread() == this.thread;

      Ref<ECS_TYPE>[] refs = new Ref[holders.length];

      for (int i = 0; i < holders.length; i++) {
         refs[i] = new Ref<>(this.store);
      }

      this.queue.add(chunk -> chunk.addEntities(holders, refs, reason));
      return refs;
   }

   @Nonnull
   @Override
   public Ref<ECS_TYPE> addEntity(@Nonnull Holder<ECS_TYPE> holder, @Nonnull AddReason reason) {
      assert Thread.currentThread() == this.thread;

      Ref<ECS_TYPE> ref = new Ref<>(this.store);
      this.queue.add(chunk -> chunk.addEntity(holder, ref, reason));
      return ref;
   }

   public void addEntities(
      @Nonnull Holder<ECS_TYPE>[] holders, int holderStart, @Nonnull Ref<ECS_TYPE>[] refs, int refStart, int length, @Nonnull AddReason reason
   ) {
      assert Thread.currentThread() == this.thread;

      for (int i = refStart; i < refStart + length; i++) {
         refs[i] = new Ref<>(this.store);
      }

      this.queue.add(chunk -> chunk.addEntities(holders, holderStart, refs, refStart, length, reason));
   }

   @Nonnull
   public Ref<ECS_TYPE> addEntity(@Nonnull Holder<ECS_TYPE> holder, @Nonnull Ref<ECS_TYPE> ref, @Nonnull AddReason reason) {
      if (ref.isValid()) {
         throw new IllegalArgumentException("EntityReference is already in use!");
      } else if (ref.getStore() != this.store) {
         throw new IllegalArgumentException("EntityReference is not for the correct store!");
      } else {
         assert Thread.currentThread() == this.thread;

         this.queue.add(chunk -> chunk.addEntity(holder, ref, reason));
         return ref;
      }
   }

   @Nonnull
   public Holder<ECS_TYPE> copyEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull Holder<ECS_TYPE> target) {
      assert Thread.currentThread() == this.thread;

      this.queue.add(chunk -> chunk.copyEntity(ref, target));
      return target;
   }

   public void tryRemoveEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull RemoveReason reason) {
      assert Thread.currentThread() == this.thread;

      Throwable source = new Throwable();
      this.queue.add(chunk -> {
         if (ref.isValid()) {
            chunk.removeEntity(ref, chunk.getRegistry().newHolder(), reason, source);
         }
      });
      if (ref.equals(this.trackedRef)) {
         this.trackedRefRemoved = true;
      }

      if (this.parentBuffer != null) {
         this.parentBuffer.testRemovedTracked(ref);
      }
   }

   public void removeEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull RemoveReason reason) {
      assert Thread.currentThread() == this.thread;

      Throwable source = new Throwable();
      this.queue.add(chunk -> chunk.removeEntity(ref, chunk.getRegistry().newHolder(), reason, source));
      if (ref.equals(this.trackedRef)) {
         this.trackedRefRemoved = true;
      }

      if (this.parentBuffer != null) {
         this.parentBuffer.testRemovedTracked(ref);
      }
   }

   @Nonnull
   @Override
   public Holder<ECS_TYPE> removeEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull Holder<ECS_TYPE> target, @Nonnull RemoveReason reason) {
      assert Thread.currentThread() == this.thread;

      Throwable source = new Throwable();
      this.queue.add(chunk -> chunk.removeEntity(ref, target, reason, source));
      if (ref.equals(this.trackedRef)) {
         this.trackedRefRemoved = true;
      }

      if (this.parentBuffer != null) {
         this.parentBuffer.testRemovedTracked(ref);
      }

      return target;
   }

   public <T extends Component<ECS_TYPE>> void ensureComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      assert Thread.currentThread() == this.thread;

      this.queue.add(chunk -> {
         if (ref.isValid()) {
            chunk.ensureComponent(ref, componentType);
         }
      });
   }

   @Nonnull
   @Override
   public <T extends Component<ECS_TYPE>> T ensureAndGetComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      assert Thread.currentThread() == this.thread;

      T component = this.store.__internal_getComponent(ref, componentType);
      if (component != null) {
         return component;
      } else {
         T newComponent = this.store.getRegistry()._internal_getData().createComponent(componentType);
         this.queue.add(chunk -> {
            if (ref.isValid()) {
               chunk.addComponent(ref, componentType, newComponent);
            }
         });
         return newComponent;
      }
   }

   @Nonnull
   @Override
   public <T extends Component<ECS_TYPE>> T addComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      assert Thread.currentThread() == this.thread;

      T component = this.store.getRegistry()._internal_getData().createComponent(componentType);
      this.queue.add(chunk -> {
         if (ref.isValid()) {
            chunk.addComponent(ref, componentType, component);
         }
      });
      return component;
   }

   @Override
   public <T extends Component<ECS_TYPE>> void addComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component) {
      assert Thread.currentThread() == this.thread;

      this.queue.add(chunk -> {
         if (ref.isValid()) {
            chunk.addComponent(ref, componentType, component);
         }
      });
   }

   public <T extends Component<ECS_TYPE>> void replaceComponent(
      @Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component
   ) {
      assert Thread.currentThread() == this.thread;

      this.queue.add(chunk -> {
         if (ref.isValid()) {
            chunk.replaceComponent(ref, componentType, component);
         }
      });
   }

   @Override
   public <T extends Component<ECS_TYPE>> void removeComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      assert Thread.currentThread() == this.thread;

      this.queue.add(chunk -> {
         if (ref.isValid()) {
            chunk.removeComponent(ref, componentType);
         }
      });
   }

   @Override
   public <T extends Component<ECS_TYPE>> void tryRemoveComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      assert Thread.currentThread() == this.thread;

      this.queue.add(chunk -> {
         if (ref.isValid()) {
            chunk.tryRemoveComponent(ref, componentType);
         }
      });
   }

   @Override
   public <T extends Component<ECS_TYPE>> void putComponent(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component) {
      assert Thread.currentThread() == this.thread;

      this.queue.add(chunk -> {
         if (ref.isValid()) {
            chunk.putComponent(ref, componentType, component);
         }
      });
   }

   @Override
   public <Event extends EcsEvent> void invoke(@Nonnull Ref<ECS_TYPE> ref, @Nonnull Event param) {
      assert Thread.currentThread() == this.thread;

      this.store.internal_invoke(this, ref, param);
   }

   @Override
   public <Event extends EcsEvent> void invoke(@Nonnull EntityEventType<ECS_TYPE, Event> systemType, @Nonnull Ref<ECS_TYPE> ref, @Nonnull Event param) {
      assert Thread.currentThread() == this.thread;

      this.store.internal_invoke(this, systemType, ref, param);
   }

   @Override
   public <Event extends EcsEvent> void invoke(@Nonnull Event param) {
      assert Thread.currentThread() == this.thread;

      this.store.internal_invoke(this, param);
   }

   @Override
   public <Event extends EcsEvent> void invoke(@Nonnull WorldEventType<ECS_TYPE, Event> systemType, @Nonnull Event param) {
      assert Thread.currentThread() == this.thread;

      this.store.internal_invoke(this, systemType, param);
   }

   void track(@Nonnull Ref<ECS_TYPE> ref) {
      this.trackedRef = ref;
   }

   private void testRemovedTracked(@Nonnull Ref<ECS_TYPE> ref) {
      if (ref.equals(this.trackedRef)) {
         this.trackedRefRemoved = true;
      }

      if (this.parentBuffer != null) {
         this.parentBuffer.testRemovedTracked(ref);
      }
   }

   boolean consumeWasTrackedRefRemoved() {
      if (this.trackedRef == null) {
         throw new IllegalStateException("Not tracking any ref!");
      } else {
         boolean wasRemoved = this.trackedRefRemoved;
         this.trackedRefRemoved = false;
         return wasRemoved;
      }
   }

   void consume() {
      this.trackedRef = null;
      this.trackedRefRemoved = false;

      assert Thread.currentThread() == this.thread;

      while (!this.queue.isEmpty()) {
         this.queue.pop().accept(this.store);
      }

      this.store.storeCommandBuffer(this);
   }

   @Nonnull
   public CommandBuffer<ECS_TYPE> fork() {
      CommandBuffer<ECS_TYPE> forkedBuffer = this.store.takeCommandBuffer();
      forkedBuffer.parentBuffer = this;
      return forkedBuffer;
   }

   public void mergeParallel(@Nonnull CommandBuffer<ECS_TYPE> commandBuffer) {
      this.trackedRef = null;
      this.trackedRefRemoved = false;
      this.parentBuffer = null;

      while (!this.queue.isEmpty()) {
         commandBuffer.queue.add(this.queue.pop());
      }

      this.store.storeCommandBuffer(this);
   }

   public boolean setThread() {
      boolean areAssertionsEnabled = false;
      if (!$assertionsDisabled) {
         areAssertionsEnabled = true;
         if (false) {
            throw new AssertionError();
         }
      }

      if (!areAssertionsEnabled) {
         throw new AssertionError("setThread should only be called when assertions are enabled!");
      } else {
         this.thread = Thread.currentThread();
         return true;
      }
   }

   public void validateEmpty() {
      if (!this.queue.isEmpty()) {
         throw new AssertionError("CommandBuffer must be empty when returned to store!");
      }
   }
}
