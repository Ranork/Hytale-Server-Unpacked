package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;

public abstract class AnnotatedComponentBase implements IAnnotatedComponent {
   protected IAnnotatedComponent parent;
   protected int index;

   @Override
   public void getInfo(Role role, ComponentInfo holder) {
   }

   @Override
   public void setContext(IAnnotatedComponent parent, int index) {
      this.parent = parent;
      this.index = index;
   }

   @Override
   public IAnnotatedComponent getParent() {
      return this.parent;
   }

   @Override
   public int getIndex() {
      return this.index;
   }
}
