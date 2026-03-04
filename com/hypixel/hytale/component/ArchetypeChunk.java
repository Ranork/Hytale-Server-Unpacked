package com.hypixel.hytale.component;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.function.consumer.IntObjectConsumer;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArchetypeChunk<ECS_TYPE> {
   @Nonnull
   private static final ArchetypeChunk[] EMPTY_ARRAY = new ArchetypeChunk[0];
   @Nonnull
   protected final Store<ECS_TYPE> store;
   @Nonnull
   protected final Archetype<ECS_TYPE> archetype;
   protected int entitiesSize;
   @Nonnull
   protected Ref<ECS_TYPE>[] refs = new Ref[16];
   protected Component<ECS_TYPE>[][] components;

   public static <ECS_TYPE> ArchetypeChunk<ECS_TYPE>[] emptyArray() {
      return EMPTY_ARRAY;
   }

   public ArchetypeChunk(@Nonnull Store<ECS_TYPE> store, @Nonnull Archetype<ECS_TYPE> archetype) {
      this.store = store;
      this.archetype = archetype;
      this.components = new Component[archetype.length()][];

      for (int i = archetype.getMinIndex(); i < archetype.length(); i++) {
         ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)archetype.get(i);
         if (componentType != null) {
            this.components[componentType.getIndex()] = new Component[16];
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
      componentType.validateRegistry(this.store.getRegistry());
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
      componentType.validateRegistry(this.store.getRegistry());
      if (index < 0 || index >= this.entitiesSize) {
         throw new IndexOutOfBoundsException(index);
      } else {
         return (T)(!this.archetype.contains(componentType) ? null : this.components[componentType.getIndex()][index]);
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
                  int componentTypeIndex = componentType.getIndex();
                  this.components[componentTypeIndex] = Arrays.copyOf(this.components[componentTypeIndex], newLength);
               }
            }
         }

         this.refs[entityIndex] = ref;

         for (int ix = this.archetype.getMinIndex(); ix < this.archetype.length(); ix++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
               .get(ix);
            if (componentType != null) {
               this.components[componentType.getIndex()][entityIndex] = holder.getComponent((ComponentType<ECS_TYPE, Component<ECS_TYPE>>)componentType);
            }
         }

         return entityIndex;
      }
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
               int componentTypeIndex = componentType.getIndex();
               Component<ECS_TYPE> component = this.components[componentTypeIndex][entityIndex];
               entityComponents[componentTypeIndex] = component.clone();
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
               int componentTypeIndex = componentType.getIndex();
               Component<ECS_TYPE> component = this.components[componentTypeIndex][entityIndex];
               entityComponents[componentTypeIndex] = component.cloneSerializable();
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
         Component<ECS_TYPE>[] entityComponents = target.ensureComponentsSize(this.archetype.length());

         for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
               .get(i);
            if (componentType != null) {
               int componentTypeIndex = componentType.getIndex();
               entityComponents[componentTypeIndex] = this.components[componentTypeIndex][entityIndex];
            }
         }

         int lastIndex = this.entitiesSize - 1;
         if (entityIndex != lastIndex) {
            this.fillEmptyIndex(entityIndex, lastIndex);
         }

         this.refs[lastIndex] = null;

         for (int ix = this.archetype.getMinIndex(); ix < this.archetype.length(); ix++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
               .get(ix);
            if (componentType != null) {
               this.components[componentType.getIndex()][lastIndex] = null;
            }
         }

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
         this.refs[entityIndex] = null;

         for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
            ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
               .get(i);
            if (componentType != null) {
               int componentTypeIndex = componentType.getIndex();
               entityComponents[componentTypeIndex] = this.components[componentTypeIndex][entityIndex];
               this.components[componentTypeIndex][entityIndex] = null;
            }
         }

         tempInternalEntityHolder._internal_init(this.archetype, entityComponents, this.store.getRegistry().getUnknownComponentType());
         modification.accept(tempInternalEntityHolder);
         int newEntityIndex = chunk.addEntity(ref, tempInternalEntityHolder);
         referenceConsumer.accept(newEntityIndex, ref);
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
      int lastTransfered = Integer.MIN_VALUE;
      Component<ECS_TYPE>[] entityComponents = new Component[this.archetype.length()];

      for (int entityIndex = 0; entityIndex < this.entitiesSize; entityIndex++) {
         if (shouldTransfer.test(entityIndex)) {
            if (firstTransfered == Integer.MIN_VALUE) {
               firstTransfered = entityIndex;
            }

            lastTransfered = entityIndex;
            Ref<ECS_TYPE> ref = this.refs[entityIndex];
            this.refs[entityIndex] = null;

            for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
               ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype
                  .get(i);
               if (componentType != null) {
                  int componentTypeIndex = componentType.getIndex();
                  entityComponents[componentTypeIndex] = this.components[componentTypeIndex][entityIndex];
                  this.components[componentTypeIndex][entityIndex] = null;
               }
            }

            tempInternalEntityHolder.init(this.archetype, entityComponents);
            modification.accept(tempInternalEntityHolder);
            int newEntityIndex = chunk.addEntity(ref, tempInternalEntityHolder);
            referenceConsumer.accept(newEntityIndex, ref);
         }
      }

      if (firstTransfered != Integer.MIN_VALUE) {
         if (lastTransfered == this.entitiesSize - 1) {
            this.entitiesSize = firstTransfered;
            return;
         }

         int newSize = this.entitiesSize - (lastTransfered - firstTransfered + 1);

         for (int entityIndexx = firstTransfered; entityIndexx <= lastTransfered; entityIndexx++) {
            if (this.refs[entityIndexx] == null) {
               int lastIndex = this.entitiesSize - 1;
               if (lastIndex == lastTransfered) {
                  break;
               }

               if (entityIndexx != lastIndex) {
                  this.fillEmptyIndex(entityIndexx, lastIndex);
               }

               this.entitiesSize--;
            }
         }

         this.entitiesSize = newSize;
      }
   }

   protected void fillEmptyIndex(int entityIndex, int lastIndex) {
      Ref<ECS_TYPE> ref = this.refs[lastIndex];
      this.store.setEntityChunkIndex(ref, entityIndex);

      for (int i = this.archetype.getMinIndex(); i < this.archetype.length(); i++) {
         ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = (ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>)this.archetype.get(i);
         if (componentType != null) {
            Component<ECS_TYPE>[] componentArr = this.components[componentType.getIndex()];
            componentArr[entityIndex] = componentArr[lastIndex];
         }
      }

      this.refs[entityIndex] = ref;
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
               sb.append(prefix)
                  .append("\t\t- ")
                  .append(componentType.getIndex())
                  .append("\t")
                  .append(this.components[componentType.getIndex()][x])
                  .append("\n");
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
