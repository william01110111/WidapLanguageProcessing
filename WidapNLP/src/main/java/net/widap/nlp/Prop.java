package net.widap.nlp;

public abstract class Prop
{
	Prop() {}
	
	//returns the id string of the property, could be "name", "color" or "type"
	String id()
	{
		return "[no id]";
	}
	
	//returns the value of this property as a string, could be "tardis", "blue", or "time machine" respectively
	String str()
	{
		return "[no value]";
	}
	
	public String toString()
	{
		return id()+": "+str();
	}
	
	public boolean equals(Prop other)
	{
		return getClass().equals(other.getClass()) && id().equals(other.id()) && str().equals(other.str());
	}
	
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
	
	//what type of thing it is, San Fransisco's type would be city, William's type would be person
	//the type object is almost always has the abstract variety of this object's variety; abstract things can have a type
	//San Fransisco is of concrete variety SPECIF; city's has an abstract variety of GENERAL and a type of place
	static class Type extends Prop
	{
		public final Thing type;
		
		Type(Thing type0)
		{
			type=type0;
		}
		String id() {return "type";}
		String str() {return type==null?"[null]":type.getName();}
	}
	
	static class Abstract extends Prop
	{
		Abstract(){}
		String id() {return "abstract";}
		String str() {return "yes";}
	}
	
	//the things who's type is this
	//this should never be created directly, Thing.addProp will make it for you
	static class Instance extends Prop
	{
		public final Thing instance;
		
		Instance(Thing inst)
		{
			instance=inst;
		}
		
		String id() {return "instance";}
		String str() {return instance==null?"[null]":instance.getName();}
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
