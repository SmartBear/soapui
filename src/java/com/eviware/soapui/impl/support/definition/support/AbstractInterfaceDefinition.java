package com.eviware.soapui.impl.support.definition.support;

import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.InterfaceDefinition;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;

import java.util.List;

public abstract class AbstractInterfaceDefinition<T extends AbstractInterface> implements InterfaceDefinition<T>
{
   private DefinitionCache definitionCache;
   private T iface;

   protected AbstractInterfaceDefinition(T iface)
   {
      this.iface = iface;
   }

   public DefinitionCache getDefinitionCache()
   {
      return definitionCache;
   }

   public void setDefinitionCache(DefinitionCache definitionCache)
   {
      this.definitionCache = definitionCache;
   }

   public InterfaceDefinitionPart getRootPart()
   {
      return definitionCache == null ? null : definitionCache.getRootPart();
   }

   public List getDefinitionParts() throws Exception
   {
      return definitionCache == null ? null : definitionCache.getDefinitionParts();
   }

   public T getInterface()
   {
      return iface;
   }
}
