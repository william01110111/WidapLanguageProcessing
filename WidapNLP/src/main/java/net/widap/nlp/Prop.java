package net.widap.nlp;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Objects;

public abstract class Prop
{
	Prop() {}
	
	//returns the id string of the property, could be "name", "color" or "type"
	String id()
	{
		return getClass().getSimpleName().toLowerCase();
	}
	
	//returns the value of this property as a string, could be "tardis", "blue", or "time machine" respectively
	String str()
	{
		return id();
	}
	
	//returns the property to add to the thing, will normally just return this, if returned null, will do nothing
	public Prop getPropToAdd(Thing thing) {return this;}
	
	//does any internal operations that need to be done immediately after removing from a thing; called automatically
	public void removedFromThing(Thing thing) {}
	
	public String toString()
	{
		return id()+": "+str();
	}
	
	public boolean equals(Prop other)
	{
		return getClass().equals(other.getClass()) && id().equals(other.id()) && str().equals(other.str());
	}
	
	//returns a copy of this property, it if fine to just return this (as is default) if that won't mess anything up
	public Prop copy() {return this;}
	
	//checks the property for internal errors and displays them with WidapMind.errMsg()
	public void check() {}
	
	//a general attribute that doesn't fit into any specific type of property, or for an unknown property type
	static class Attrib extends Prop
	{
		private final String idStr, strStr;
		
		Attrib(String id, String str)
		{
			idStr=id;
			strStr=str;
		}
		
		Attrib(String str)
		{
			idStr="attrib";
			strStr=str;
		}
		
		String id() {return idStr;}
		
		String str() {return strStr;}
	}
	
	//simply the name of the thing, a thing can have multiple names
	static class Name extends Prop
	{
		private final String name;
		
		Name(String name0) {name=name0;}
		String id() {return "name";}
		String str() {return name;}
	}
	
	static class Link extends Prop
	{
		public final Thing other;
		
		private Link otherLink=null;
		private boolean removeOther=false;
		
		public Link(Thing otherIn)
		{
			other=otherIn;
		}
		
		String id() {return "link";}
		
		String str() {return other==null?"[null]":other.getName();}
		
		//the default Prop equals method may say they are equal when they are not, but I have had endless problems with this because it is recursive (by calling Thing.equals)
		/*public boolean equals(Prop prop)
		{
			return getClass().equals(prop.getClass()) && other.equals(((Link)prop).other); && id().equals(prop.id());
		}*/
		
		public final Prop getPropToAdd(Thing thing)
		{
			if (removeOther)
				return copy().getPropToAdd(thing);
			
			//this will happen in normal operation, it is not an error
			if (otherLink==null)
				makeOtherLink(thing);
			
			return this;
		}
		
		protected void makeOtherLink(Thing thing)
		{
			makeOtherLink(new Link(thing));
		}
		
		protected final void makeOtherLink(Link link)
		{
			otherLink=link;
			otherLink.otherLink=this;
			other.addProp(otherLink);
			removeOther=true;
		}
		
		public final void removedFromThing(Thing thing)
		{
			if (removeOther)
			{
				otherLink.removeOther=false;
				other.removeProp(otherLink);
			}
		}
		
		public Prop copy()
		{
			try
			{
				return this.getClass().getDeclaredConstructor(Thing.class).newInstance(other);
			}
			catch(Exception e)
			{
				WidapMind.errorMsg("exception in Link.copy: "+e.getMessage());
				e.printStackTrace();
				return this;
			}
		}
	}
	
	//what type of thing it is, San Fransisco's type would be city, William's type would be person
	//the type thing is always abstract; abstract things can have a type; San Fransisco is not abstract; city is
	static class Type extends Link
	{
		Type(Thing thing) {super(thing);}
		
		String id() {return "type";}
		
		protected void makeOtherLink(Thing thing)
		{
			makeOtherLink(new Instance(thing));
		}
	}
	
	//the things who's type is this
	//this should never be created directly, Thing.addProp will make it for you
	static class Instance extends Link
	{
		Instance(Thing thing) {super(thing);}
		
		String id() {return "instance";}
		
		protected void makeOtherLink(Thing thing)
		{
			makeOtherLink(new Type(thing));
		}
	}
	
	static class DefaultOfType extends Link
	{
		DefaultOfType(Thing thing) {super(thing);}
		
		String id() {return "default of type";}
		
		protected void makeOtherLink(Thing thing)
		{
			makeOtherLink(new DefaultInstance(thing));
		}
	}
	
	static class DefaultInstance extends Link
	{
		DefaultInstance(Thing thing) {super(thing);}
		
		String id() {return "default instance";}
		
		protected void makeOtherLink(Thing thing)
		{
			makeOtherLink(new DefaultOfType(thing));
		}
	}
	
	static class Abstract extends Prop
	{
		Abstract(){}
		String id() {return "abstract";}
		String str() {return "yes";}
		void remove(Thing thing) {thing.isAbstract=false;}
	}
	
	static class Color extends Prop
	{
		private final String idStr, clrStr;
		
		Color(String clr) {idStr="color"; clrStr=clr;}
		Color(String id, String clr) {idStr=id; clrStr=clr;}
		String id() {return idStr;}
		String str() {return clrStr;}
	}
}
