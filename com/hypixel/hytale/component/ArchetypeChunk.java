package com.hypixel.hytale.component;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.function.consumer.IntObjectConsumer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArchetypeChunk<ECS_TYPE> {
   @Nonnull
   private static final ArchetypeChunk[] EMPTY_ARRAY = new ArchetypeChunk[0];
   @Nonnull
   protected final ComponentRegistry<ECS_TYPE> componentRegistry;
   @Nonnull
   protected final Archetype<ECS_TYPE> archetype;
   protected int entitiesSize;
   @Nonnull
   protected Ref<ECS_TYPE>[] refs = new Ref[16];
   protected Component<ECS_TYPE>[][] components;
   private final BitSet systemIndexes = new BitSet();
   private int archetypeIndex = Integer.MIN_VALUE;

   public static <ECS_TYPE> ArchetypeChunk<ECS_TYPE>[] emptyArray() {
      return EMPTY_ARRAY;
   }

   public ArchetypeChunk(@Nonnull Store<ECS_TYPE> store, @Nonnull Archetype<ECS_TYPE> archetype) {
      this.componentRegistry = store.getRegistry();
      this.archetype = archetype;
      this.components = new Component[archetype.length()][];

      for (int i = archetype.getMinIndex(); i < archetype.length(); i++) {
         ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)archetype.get(i);
         if (componentType != null) {
            this.components[i] = new Component[16];
         }
      }
   }

   @Nonnull
   public Archetype<ECS_TYPE> getArchetype() {
      return this.archetype;
   }

   public int size() {
      return this.entitiesSize;
   }

   @Nonnull
   public Ref<ECS_TYPE> getReferenceTo(int index) {
      if (index >= 0 && index < this.entitiesSize) {
         return this.refs[index];
      } else {
         throw new IndexOutOfBoundsException(index);
      }
   }

   public <T extends Component<ECS_TYPE>> void setComponent(int index, @Nonnull ComponentType<ECS_TYPE, T> componentType, @Nonnull T component) {
      componentType.validateRegistry(this.componentRegistry);
      if (index < 0 || index >= this.entitiesSize) {
         throw new IndexOutOfBoundsException(index);
      } else if (!this.archetype.contains(componentType)) {
         throw new IllegalArgumentException("Entity doesn't have component type " + componentType);
      } else {
         this.components[componentType.getIndex()][index] = component;
      }
   }

   @Nullable
   public <T extends Component<ECS_TYPE>> T getComponent(int index, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      componentType.validateRegistry(this.componentRegistry);
      return this.__internal_getComponent(index, componentType);
   }

   @Nullable
   protected <T extends Component<ECS_TYPE>> T __internal_getComponent(int index, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      if (index < 0 || index >= this.entitiesSize) {
         throw new IndexOutOfBoundsException(index);
      } else {
         return (T)(!this.archetype.contains(componentType) ? null : this.components[componentType.getIndex()][index]);
      }
   }

   @Nullable
   protected <T extends Component<ECS_TYPE>> T __internal_getComponentConcurrent(int index, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      if (index >= 0 && this.archetype.contains(componentType)) {
         Component<ECS_TYPE>[] col = this.components[componentType.getIndex()];
         return (T)(index >= col.length ? null : col[index]);
      } else {
         return null;
      }
   }

   public int addEntity(@Nonnull Ref<ECS_TYPE> ref, @Nonnull Holder<ECS_TYPE> holder) {
      if (!this.archetype.equals(holder.getArchetype())) {
         throw new IllegalArgumentException("EntityHolder is not for this archetype chunk!");
      } else {
         int entityIndex = this.entitiesSize++;
         int oldLength = this.refs.length;
         if (oldLength <= entityIndex) {
            int newLength = ArrayUtil.grow(entityIndex);
            this.refs = Arrays.copyOf(this.refs, newLength);

            for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
                  .get(i);
               if (componentType != null) {
                  Component<ECS_TYPE>[] grown = Arrays.copyOf(this.components[i], newLength);
                  grown[entityIndex] = holder.getComponent((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)componentType);
                  this.components[i] = grown;
               }
            }

            this.refs[entityIndex] = ref;
            return entityIndex;
         } else {
            this.refs[entityIndex] = ref;

            for (int ix = this.archetype.getMinIndex(); ix < this.archetype.length(); ix++) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
                  .get(ix);
               if (componentType != null) {
                  this.components[ix][entityIndex] = holder.getComponent((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)componentType);
               }
            }

            return entityIndex;
         }
      }
   }

   public void addEntityFrom(@Nonnull Ref<ECS_TYPE> ref, @Nonnull ArchetypeChunk<ECS_TYPE> source, int sourceIndex) {
      this.__internal_addEntity(ref, source, sourceIndex, -1, null);
   }

   public void addEntityFrom(
      @Nonnull Ref<ECS_TYPE> ref,
      @Nonnull ArchetypeChunk<ECS_TYPE> source,
      int sourceIndex,
      @Nonnull ComponentType<ECS_TYPE, ?> addedType,
      @Nonnull Component<ECS_TYPE> addedComponent
   ) {
      this.__internal_addEntity(ref, source, sourceIndex, addedType.getIndex(), addedComponent);
   }

   private void __internal_addEntity(
      @Nonnull Ref<ECS_TYPE> ref, @Nonnull ArchetypeChunk<ECS_TYPE> source, int sourceIndex, int overrideIndex, @Nullable Component<ECS_TYPE> overrideComponent
   ) {
      int entityIndex = this.entitiesSize++;
      int sourceLastIndex = source.entitiesSize - 1;
      ref.beginMutation();
      Ref<ECS_TYPE> movedRef = sourceIndex != sourceLastIndex ? source.refs[sourceLastIndex] : null;
      if (movedRef != null) {
         movedRef.beginMutation();
      }

      if (this.refs.length <= entityIndex) {
         int newLength = ArrayUtil.grow(entityIndex);
         this.refs = Arrays.copyOf(this.refs, newLength);

         for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
            if (this.archetype.get(i) != null) {
               Component<ECS_TYPE>[] grown = Arrays.copyOf(this.components[i], newLength);
               grown[entityIndex] = i == overrideIndex ? overrideComponent : source.components[i][sourceIndex];
               this.components[i] = grown;
            }
         }

         if (sourceIndex != sourceLastIndex) {
            for (int ix = source.archetype.getMinIndex(); ix < source.archetype.length(); ix++) {
               if (source.archetype.get(ix) != null) {
                  Component<ECS_TYPE>[] col = source.components[ix];
                  col[sourceIndex] = col[sourceLastIndex];
                  col[sourceLastIndex] = null;
               }
            }
         } else {
            for (int ixx = source.archetype.getMinIndex(); ixx < source.archetype.length(); ixx++) {
               if (source.archetype.get(ixx) != null) {
                  source.components[ixx][sourceLastIndex] = null;
               }
            }
         }
      } else {
         if (sourceIndex != sourceLastIndex) {
            for (int ixxx = source.archetype.getMinIndex(); ixxx < source.archetype.length(); ixxx++) {
               if (source.archetype.get(ixxx) != null) {
                  Component<ECS_TYPE>[] col = source.components[ixxx];
                  if (ixxx < this.components.length && this.components[ixxx] != null) {
                     this.components[ixxx][entityIndex] = col[sourceIndex];
                  }

                  col[sourceIndex] = col[sourceLastIndex];
                  col[sourceLastIndex] = null;
               }
            }
         } else {
            for (int ixxxx = source.archetype.getMinIndex(); ixxxx < source.archetype.length(); ixxxx++) {
               if (source.archetype.get(ixxxx) != null) {
                  Component<ECS_TYPE>[] col = source.components[ixxxx];
                  if (ixxxx < this.components.length && this.components[ixxxx] != null) {
                     this.components[ixxxx][entityIndex] = col[sourceIndex];
                  }

                  col[sourceIndex] = null;
               }
            }
         }

         if (overrideIndex >= 0) {
            this.components[overrideIndex][entityIndex] = overrideComponent;
         }
      }

      this.refs[entityIndex] = ref;
      ref.setPosition(this, entityIndex);
      ref.endMutation();
      if (movedRef != null) {
         assert movedRef.isValid() : "movedRef in live chunk range must be valid";

         movedRef.setPosition(source, sourceIndex);
         movedRef.endMutation();
         source.refs[sourceIndex] = movedRef;
      }

      source.refs[sourceLastIndex] = null;
      source.entitiesSize = sourceLastIndex;
   }

   @Nonnull
   public Holder<ECS_TYPE> copyEntity(int entityIndex, @Nonnull Holder<ECS_TYPE> target) {
      if (entityIndex >= this.entitiesSize) {
         throw new IndexOutOfBoundsException(entityIndex);
      } else {
         Component<ECS_TYPE>[] entityComponents = target.ensureComponentsSize(this.archetype.length());

         for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
               .get(i);
            if (componentType != null) {
               Component<ECS_TYPE> component = this.components[i][entityIndex];
               entityComponents[i] = component.clone();
            }
         }

         target.init(this.archetype, entityComponents);
         return target;
      }
   }

   @Nonnull
   public Holder<ECS_TYPE> copySerializableEntity(@Nonnull ComponentRegistry.Data<ECS_TYPE> data, int entityIndex, @Nonnull Holder<ECS_TYPE> target) {
      if (entityIndex >= this.entitiesSize) {
         throw new IndexOutOfBoundsException(entityIndex);
      } else {
         Archetype<ECS_TYPE> serializableArchetype = this.archetype.getSerializableArchetype(data);
         Component<ECS_TYPE>[] entityComponents = target.ensureComponentsSize(serializableArchetype.length());

         for (int i = serializableArchetype.getMinIndex(); i < serializableArchetype.length(); i++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)serializableArchetype.get(
               i
            );
            if (componentType != null) {
               Component<ECS_TYPE> component = this.components[i][entityIndex];
               entityComponents[i] = component.cloneSerializable();
            }
         }

         target.init(serializableArchetype, entityComponents);
         return target;
      }
   }

   @Nonnull
   public Holder<ECS_TYPE> removeEntity(int entityIndex, @Nonnull Holder<ECS_TYPE> target) {
      if (entityIndex >= this.entitiesSize) {
         throw new IndexOutOfBoundsException(entityIndex);
      } else {
         int lastIndex = this.entitiesSize - 1;
         Component<ECS_TYPE>[] entityComponents = target.ensureComponentsSize(this.archetype.length());
         if (entityIndex != lastIndex) {
            Ref<ECS_TYPE> ref = this.refs[lastIndex];
            if (ref.isValid()) {
               ref.beginMutation();
            }

            this.refs[entityIndex] = ref;

            for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
                  .get(i);
               if (componentType != null) {
                  Component<ECS_TYPE>[] col = this.components[i];
                  entityComponents[i] = col[entityIndex];
                  col[entityIndex] = col[lastIndex];
                  col[lastIndex] = null;
               }
            }

            if (ref.isValid()) {
               ref.setPosition(this, entityIndex);
               ref.endMutation();
            }
         } else {
            for (int ix = this.archetype.getMinIndex(); ix < this.archetype.length(); ix++) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
                  .get(ix);
               if (componentType != null) {
                  Component<ECS_TYPE>[] col = this.components[ix];
                  entityComponents[ix] = col[lastIndex];
                  col[lastIndex] = null;
               }
            }
         }

         this.refs[lastIndex] = null;
         this.entitiesSize = lastIndex;
         target.init(this.archetype, entityComponents);
         return target;
      }
   }

   public void transferTo(
      @Nonnull Holder<ECS_TYPE> tempInternalEntityHolder,
      @Nonnull ArchetypeChunk<ECS_TYPE> chunk,
      @Nonnull Consumer<Holder<ECS_TYPE>> modification,
      @Nonnull IntObjectConsumer<Ref<ECS_TYPE>> referenceConsumer
   ) {
      Component<ECS_TYPE>[] entityComponents = new Component[this.archetype.length()];

      for (int entityIndex = 0; entityIndex < this.entitiesSize; entityIndex++) {
         Ref<ECS_TYPE> ref = this.refs[entityIndex];
         ref.beginMutation();
         this.refs[entityIndex] = null;
         Arrays.fill(entityComponents, 0, this.archetype.getMinIndex(), null);

         for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
               .get(i);
            if (componentType == null) {
               entityComponents[i] = null;
            } else {
               entityComponents[i] = this.components[i][entityIndex];
               this.components[i][entityIndex] = null;
            }
         }

         tempInternalEntityHolder._internal_init(this.archetype, entityComponents, this.componentRegistry.getUnknownComponentType());
         modification.accept(tempInternalEntityHolder);
         int newEntityIndex = chunk.addEntity(ref, tempInternalEntityHolder);
         referenceConsumer.accept(newEntityIndex, ref);
         ref.endMutation();
      }

      this.entitiesSize = 0;
   }

   public void transferSomeTo(
      @Nonnull Holder<ECS_TYPE> tempInternalEntityHolder,
      @Nonnull ArchetypeChunk<ECS_TYPE> chunk,
      @Nonnull IntPredicate shouldTransfer,
      @Nonnull Consumer<Holder<ECS_TYPE>> modification,
      @Nonnull IntObjectConsumer<Ref<ECS_TYPE>> referenceConsumer
   ) {
      int firstTransfered = Integer.MIN_VALUE;
      Component[] entityComponents = new Component[this.archetype.length()];

      for (int entityIndex = 0; entityIndex < this.entitiesSize; entityIndex++) {
         if (shouldTransfer.test(entityIndex)) {
            if (firstTransfered == Integer.MIN_VALUE) {
               firstTransfered = entityIndex;
            }

            Ref<ECS_TYPE> ref = this.refs[entityIndex];
            ref.beginMutation();
            this.refs[entityIndex] = null;
            Arrays.fill(entityComponents, 0, this.archetype.getMinIndex(), null);

            for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
                  .get(i);
               if (componentType == null) {
                  entityComponents[i] = null;
               } else {
                  entityComponents[i] = this.components[i][entityIndex];
                  this.components[i][entityIndex] = null;
               }
            }

            tempInternalEntityHolder.init(this.archetype, entityComponents);
            modification.accept(tempInternalEntityHolder);
            int newEntityIndex = chunk.addEntity(ref, tempInternalEntityHolder);
            referenceConsumer.accept(newEntityIndex, ref);
            ref.endMutation();
         }
      }

      if (firstTransfered != Integer.MIN_VALUE) {
         int writeIndex = firstTransfered;

         for (int readIndex = firstTransfered + 1; readIndex < this.entitiesSize; readIndex++) {
            if (this.refs[readIndex] != null) {
               if (writeIndex != readIndex) {
                  this.fillEmptyIndex(writeIndex, readIndex);
               }

               writeIndex++;
            }
         }

         for (int ix = writeIndex; ix < this.entitiesSize; ix++) {
            this.refs[ix] = null;

            for (int j = this.archetype.getMinIndex(); j < this.archetype.length(); j++) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
                  .get(j);
               if (componentType != null) {
                  this.components[j][ix] = null;
               }
            }
         }

         this.entitiesSize = writeIndex;
      }
   }

   protected void fillEmptyIndex(int entityIndex, int lastIndex) {
      Ref<ECS_TYPE> ref = this.refs[lastIndex];
      if (ref.isValid()) {
         ref.beginMutation();
      }

      for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
         ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype.get(i);
         if (componentType != null) {
            Component<ECS_TYPE>[] componentArr = this.components[i];
            componentArr[entityIndex] = componentArr[lastIndex];
         }
      }

      this.refs[entityIndex] = ref;
      if (ref.isValid()) {
         ref.setPosition(this, entityIndex);
         ref.endMutation();
      }
   }

   BitSet getSystemIndexes() {
      return this.systemIndexes;
   }

   int getArchetypeIndex() {
      return this.archetypeIndex;
   }

   void setArchetypeIndex(int archetypeIndex) {
      this.archetypeIndex = archetypeIndex;
   }

   public void appendDump(@Nonnull String prefix, @Nonnull StringBuilder sb) {
      sb.append(prefix).append("archetype=").append(this.archetype).append("\n");
      sb.append(prefix).append("entitiesSize=").append(this.entitiesSize).append("\n");

      for (int i = 0; i < this.entitiesSize; i++) {
         sb.append(prefix).append("\t- ").append(this.refs[i]).append("\n");
         sb.append(prefix).append("\t").append("components=").append("\n");

         for (int x = this.archetype.getMinIndex(); x < this.archetype.length(); x++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
               .get(x);
            if (componentType != null) {
               sb.append(prefix).append("\t\t- ").append(x).append("\t").append(this.components[x][i]).append("\n");
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ArchetypeChunk{archetype="
         + this.archetype
         + ", entitiesSize="
         + this.entitiesSize
         + ", entityReferences="
         + Arrays.toString((Object[])this.refs)
         + ", components="
         + Arrays.toString((Object[])this.components)
         + "}";
   }
}
