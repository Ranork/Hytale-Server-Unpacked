package com.hypixel.hytale.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.component.data.change.ComponentChange;
import com.hypixel.hytale.component.data.unknown.TempUnknownComponent;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.locks.StampedLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class Holder<ECS_TYPE> {
   private static final Holder<?>[] EMPTY_ARRAY = new Holder[0];
   @Nullable
   private final ComponentRegistry<ECS_TYPE> registry;
   private final StampedLock lock = new StampedLock();
   private Archetype<ECS_TYPE> archetype;
   @Nullable
   private Component<ECS_TYPE>[] components;
   private boolean ensureValidComponents = true;

   public static <T> Holder<T>[] emptyArray() {
      return (Holder<T>[])EMPTY_ARRAY;
   }

   Holder() {
      this.registry = null;
   }

   Holder(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      this.registry = registry;
      this.archetype = Archetype.empty();
      this.components = Component.EMPTY_ARRAY;
   }

   Holder(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull Archetype<ECS_TYPE> archetype, @Nonnull Component<ECS_TYPE>[] components) {
      this.registry = registry;
      this.init(archetype, components);
   }

   @Nonnull
   public Component<ECS_TYPE>[] ensureComponentsSize(int size) {
      long stamp = this.lock.writeLock();

      Component[] var4;
      try {
         if (this.components != null) {
            if (this.components.length < size) {
               this.components = Arrays.copyOf(this.components, size);
            }

            return this.components;
         }

         this.components = new Component[size];
         var4 = this.components;
      } finally {
         this.lock.unlockWrite(stamp);
      }

      return var4;
   }

   public void init(@Nonnull Archetype<ECS_TYPE> archetype, @Nonnull Component<ECS_TYPE>[] components) {
      archetype.validate();
      archetype.validateComponents(components, null);
      long stamp = this.lock.writeLock();

      try {
         this.archetype = archetype;
         this.components = components;
         this.ensureValidComponents = true;
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   public void _internal_init(
      @Nonnull Archetype<ECS_TYPE> archetype,
      @Nonnull Component<ECS_TYPE>[] components,
      @Nonnull ComponentType<ECS_TYPE, UnknownComponents<ECS_TYPE>> unknownComponentType
   ) {
      archetype.validateComponents(components, unknownComponentType);
      long stamp = this.lock.writeLock();

      try {
         this.archetype = archetype;
         this.components = components;
         this.ensureValidComponents = false;
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   public Archetype<ECS_TYPE> getArchetype() {
      return this.archetype;
   }

   public <T extends Component<ECS_TYPE>> void ensureComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
      assert this.archetype != null;

      assert this.registry != null;

      if (this.ensureValidComponents) {
         componentType.validate();
      }

      long stamp = this.lock.writeLock();

      try {
         if (!this.archetype.contains(componentType)) {
            T component = this.registry.createComponent(componentType);
            this.addComponent0(componentType, component);
            return;
         }
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   @Nonnull
   public <T extends Component<ECS_TYPE>> T ensureAndGetComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
      this.ensureComponent(componentType);
      return this.getComponent(componentType);
   }

   public <T extends Component<ECS_TYPE>> void addComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component) {
      assert this.archetype != null;

      long stamp = this.lock.writeLock();

      try {
         if (this.ensureValidComponents) {
            componentType.validate();
         }

         if (this.archetype.contains(componentType)) {
            throw new IllegalArgumentException("Entity contains component type: " + componentType);
         }

         this.addComponent0(componentType, component);
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   private <T extends Component<ECS_TYPE>> void addComponent0(@Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component) {
      assert this.archetype != null;

      assert this.components != null;

      this.archetype = Archetype.add(this.archetype, componentType);
      int newLength = this.archetype.length();
      if (this.components.length < newLength) {
         this.components = Arrays.copyOf(this.components, newLength);
      }

      this.components[componentType.getIndex()] = component;
   }

   public <T extends Component<ECS_TYPE>> void replaceComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component) {
      assert this.archetype != null;

      assert this.components != null;

      long stamp = this.lock.writeLock();

      try {
         if (this.ensureValidComponents) {
            componentType.validate();
         }

         this.archetype.validateComponentType(componentType);
         this.components[componentType.getIndex()] = component;
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   public <T extends Component<ECS_TYPE>> void putComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component) {
      if (this.<T>getComponent(componentType) != null) {
         this.replaceComponent(componentType, component);
      } else {
         this.addComponent(componentType, component);
      }
   }

   @Nullable
   public <T extends Component<ECS_TYPE>> T getComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
      assert this.archetype != null;

      assert this.components != null;

      long stamp = this.lock.readLock();

      Object var4;
      try {
         if (this.ensureValidComponents) {
            componentType.validate();
         }

         if (this.archetype.contains(componentType)) {
            return (T)this.components[componentType.getIndex()];
         }

         var4 = null;
      } finally {
         this.lock.unlockRead(stamp);
      }

      return (T)var4;
   }

   public <T extends Component<ECS_TYPE>> void removeComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
      assert this.archetype != null;

      assert this.components != null;

      long stamp = this.lock.writeLock();

      try {
         if (this.ensureValidComponents) {
            componentType.validate();
         }

         this.archetype.validateComponentType(componentType);
         this.archetype = Archetype.remove(this.archetype, componentType);
         this.components[componentType.getIndex()] = null;
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   public <T extends Component<ECS_TYPE>> boolean tryRemoveComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
      if (this.<T>getComponent(componentType) == null) {
         return false;
      } else {
         this.removeComponent(componentType);
         return true;
      }
   }

   public boolean hasSerializableComponents(@Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      assert this.archetype != null;

      return this.archetype.hasSerializableComponents(data);
   }

   public void updateData(@Nonnull ComponentRegistry.Data<ECS_TYPE> oldData, @Nonnull ComponentRegistry.Data<ECS_TYPE> newData) {
      assert this.archetype != null;

      assert this.components != null;

      assert this.registry != null;

      long stamp = this.lock.writeLock();

      try {
         if (this.archetype.isEmpty()) {
            return;
         }

         ComponentType<ECS_TYPE, UnknownComponents<ECS_TYPE>> unknownComponentType = this.registry.getUnknownComponentType();

         for (int i = 0; i < newData.getDataChangeCount(); i++) {
            if (newData.getDataChange(i) instanceof ComponentChange<ECS_TYPE, ? extends Component<ECS_TYPE>> componentChange) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = componentChange.getComponentType();
               switch (componentChange.getType()) {
                  case REGISTERED:
                     assert this.archetype != null;

                     if (!this.archetype.contains(componentType) && this.archetype.contains(unknownComponentType)) {
                        String componentId = newData.getComponentId(componentType);
                        Codec<Component<ECS_TYPE>> componentCodec = newData.getComponentCodec((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)componentType);
                        if (componentCodec != null) {
                           UnknownComponents<ECS_TYPE> unknownComponents = (UnknownComponents<ECS_TYPE>)this.components[unknownComponentType.getIndex()];

                           assert unknownComponents != null;

                           Component<ECS_TYPE> component = unknownComponents.removeComponent(componentId, componentCodec);
                           if (component != null) {
                              this.addComponent0(componentType, component);
                           }
                        }
                     }
                     break;
                  case UNREGISTERED:
                     assert this.archetype != null;

                     if (this.archetype.contains(componentType)) {
                        String componentId = oldData.getComponentId(componentType);
                        Codec<Component<ECS_TYPE>> componentCodec = oldData.getComponentCodec((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)componentType);
                        if (componentCodec != null) {
                           UnknownComponents<ECS_TYPE> unknownComponents;
                           if (this.archetype.contains(unknownComponentType)) {
                              unknownComponents = (UnknownComponents<ECS_TYPE>)this.components[unknownComponentType.getIndex()];

                              assert unknownComponents != null;
                           } else {
                              unknownComponents = new UnknownComponents<>();
                              this.addComponent0(unknownComponentType, unknownComponents);
                           }

                           Component<ECS_TYPE> component = this.components[componentType.getIndex()];
                           unknownComponents.addComponent(componentId, component, componentCodec);
                        }

                        this.archetype = Archetype.remove(this.archetype, componentType);
                        this.components[componentType.getIndex()] = null;
                     }
               }
            }
         }
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   @Nonnull
   public Holder<ECS_TYPE> clone() {
      assert this.archetype != null;

      assert this.components != null;

      assert this.registry != null;

      long stamp = this.lock.readLock();

      Holder var9;
      try {
         Component<ECS_TYPE>[] componentsClone = new Component[this.components.length];

         for (int i = 0; i < this.components.length; i++) {
            Component<ECS_TYPE> component = this.components[i];
            if (component != null) {
               componentsClone[i] = component.clone();
            }
         }

         var9 = this.registry.newHolder(this.archetype, componentsClone);
      } finally {
         this.lock.unlockRead(stamp);
      }

      return var9;
   }

   public Holder<ECS_TYPE> cloneSerializable(@Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      assert this.archetype != null;

      assert this.components != null;

      assert this.registry != null;

      long stamp = this.lock.readLock();

      Holder var11;
      try {
         Archetype<ECS_TYPE> serializableArchetype = this.archetype.getSerializableArchetype(data);
         Component<ECS_TYPE>[] componentsClone = new Component[serializableArchetype.length()];

         for (int i = serializableArchetype.getMinIndex(); i < serializableArchetype.length(); i++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)serializableArchetype.get(
               i
            );
            if (componentType != null) {
               componentsClone[i] = this.components[i].cloneSerializable();
            }
         }

         var11 = this.registry.newHolder(serializableArchetype, componentsClone);
      } finally {
         this.lock.unlockRead(stamp);
      }

      return var11;
   }

   void loadComponentsMap(@Nonnull ComponentRegistry.Data<ECS_TYPE> data, @Nonnull Map<String, Component<ECS_TYPE>> map) {
      assert this.components != null;

      long stamp = this.lock.writeLock();

      try {
         ComponentType<ECS_TYPE, ?>[] componentTypes = new ComponentType[map.size()];
         int i = 0;
         ComponentType<ECS_TYPE, UnknownComponents<ECS_TYPE>> unknownComponentType = data.getRegistry().getUnknownComponentType();
         UnknownComponents<ECS_TYPE> unknownComponents = (UnknownComponents<ECS_TYPE>)map.remove("Unknown");
         if (unknownComponents != null) {
            for (Entry<String, BsonDocument> e : unknownComponents.getUnknownComponents().entrySet()) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> type = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)data.getComponentType(
                  e.getKey()
               );
               if (type != null && !map.containsKey(e.getKey())) {
                  Codec<Component<ECS_TYPE>> codec = data.getComponentCodec((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)type);
                  ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
                  Component<ECS_TYPE> decodedComponent = codec.decode((BsonValue)e.getValue(), extraInfo);
                  extraInfo.getValidationResults().logOrThrowValidatorExceptions(UnknownComponents.LOGGER);
                  if (componentTypes.length <= i) {
                     componentTypes = Arrays.copyOf(componentTypes, i + 1);
                  }

                  componentTypes[i++] = type;
                  int index = type.getIndex();
                  if (this.components.length <= index) {
                     this.components = Arrays.copyOf(this.components, index + 1);
                  }

                  this.components[index] = decodedComponent;
               }
            }

            if (componentTypes.length <= i) {
               componentTypes = Arrays.copyOf(componentTypes, i + 1);
            }

            componentTypes[i++] = unknownComponentType;
            int index = unknownComponentType.getIndex();
            if (this.components.length <= index) {
               this.components = Arrays.copyOf(this.components, index + 1);
            }

            this.components[index] = unknownComponents;
         }

         for (Entry<String, Component<ECS_TYPE>> entry : map.entrySet()) {
            Component<ECS_TYPE> component = entry.getValue();
            if (component instanceof TempUnknownComponent tempUnknownComponent) {
               if (unknownComponents == null) {
                  unknownComponents = new UnknownComponents<>();
                  if (componentTypes.length <= i) {
                     componentTypes = Arrays.copyOf(componentTypes, i + 1);
                  }

                  componentTypes[i++] = unknownComponentType;
                  int index = unknownComponentType.getIndex();
                  if (this.components.length <= index) {
                     this.components = Arrays.copyOf(this.components, index + 1);
                  }

                  this.components[index] = unknownComponents;
               }

               unknownComponents.addComponent(entry.getKey(), tempUnknownComponent);
            } else {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)data.getComponentType(
                  entry.getKey()
               );
               if (componentTypes.length <= i) {
                  componentTypes = Arrays.copyOf(componentTypes, i + 1);
               }

               componentTypes[i++] = componentType;
               int index = componentType.getIndex();
               if (this.components.length <= index) {
                  this.components = Arrays.copyOf(this.components, index + 1);
               }

               this.components[index] = component;
            }
         }

         this.archetype = Archetype.of(componentTypes.length == i ? componentTypes : Arrays.copyOf(componentTypes, i));
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   @Nonnull
   Map<String, Component<ECS_TYPE>> createComponentsMap(@Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      assert this.archetype != null;

      assert this.components != null;

      long stamp = this.lock.readLock();

      Map registry;
      try {
         if (!this.archetype.isEmpty()) {
            ComponentRegistry<ECS_TYPE> registryx = data.getRegistry();
            ComponentType<ECS_TYPE, UnknownComponents<ECS_TYPE>> unknownComponentType = registryx.getUnknownComponentType();
            Map<String, Component<ECS_TYPE>> map = new Object2ObjectOpenHashMap(this.archetype.length());

            for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
                  .get(i);
               if (componentType != null && data.getComponentCodec(componentType) != null) {
                  if (componentType == unknownComponentType) {
                     UnknownComponents<ECS_TYPE> unknownComponents = (UnknownComponents<ECS_TYPE>)this.components[componentType.getIndex()];

                     for (Entry<String, BsonDocument> entry : unknownComponents.getUnknownComponents().entrySet()) {
                        map.putIfAbsent(entry.getKey(), new TempUnknownComponent<>(entry.getValue()));
                     }
                  } else {
                     map.put(data.getComponentId(componentType), this.components[componentType.getIndex()]);
                  }
               }
            }

            return map;
         }

         registry = Collections.emptyMap();
      } finally {
         this.lock.unlockRead(stamp);
      }

      return registry;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Holder<?> that = (Holder<?>)o;
         long stamp = this.lock.readLock();
         long thatStamp = that.lock.readLock();

         boolean var7;
         try {
            if (Objects.equals(this.archetype, that.archetype)) {
               return Arrays.equals((Object[])this.components, (Object[])that.components);
            }

            var7 = false;
         } finally {
            that.lock.unlockRead(thatStamp);
            this.lock.unlockRead(stamp);
         }

         return var7;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      long stamp = this.lock.readLock();

      int var4;
      try {
         int result = this.archetype != null ? this.archetype.hashCode() : 0;
         result = 31 * result + Arrays.hashCode((Object[])this.components);
         var4 = result;
      } finally {
         this.lock.unlockRead(stamp);
      }

      return var4;
   }

   @Nonnull
   @Override
   public String toString() {
      long stamp = this.lock.readLock();

      String var3;
      try {
         var3 = "EntityHolder{archetype=" + this.archetype + ", components=" + Arrays.toString((Object[])this.components) + "}";
      } finally {
         this.lock.unlockRead(stamp);
      }

      return var3;
   }
}
