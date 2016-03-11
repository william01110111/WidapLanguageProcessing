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
	
	//A things variety is the high level sort of thing it is. there are very limited options
	public enum VarietyEnum
	{
		GENERAL, //a general thing; abstract, noun; a car
		SPECIF, //a specific thing; concrete, noun; Tom's car
		ACTION, //a general action; abstract, verb; a car crash
		EVENT,  //a specific event; concrete, verb; that one time Tom crashed his car into the pond
		OTHER //used when none of the others fit
	}
	
	static class Variety extends Prop
	{
		private final VarietyEnum val;
		
		Variety(VarietyEnum inVar) {val=inVar;}
		String id() {return "variety";}
		String str() {return val.name();}
		VarietyEnum variety() {return val;}
		public boolean equals(Prop other) {return other.getClass().equals(Variety.class) && val.equals(((Variety)other).val);}
		
		//get boolean info about this things variety
		boolean isNoun() {return val==VarietyEnum.GENERAL || val==VarietyEnum.SPECIF;}
		boolean isVerb() {return val==VarietyEnum.ACTION || val==VarietyEnum.EVENT;}
		boolean isAbstract() {return val==VarietyEnum.ACTION || val==VarietyEnum.GENERAL;}
		boolean isConcrete () {return val==VarietyEnum.SPECIF || val==VarietyEnum.EVENT;}
	}
	
	//what type of thing it is, San Fransisco's type would be city, William's type would be person
	//the type object is almost always has the abstract variety of this object's variety; abstract things can have a type
	//San Fransisco is of concrete variety SPECIF; city's has an abstract variety of GENERAL and a type of place
	static class Type extends Prop
	{
		private final Thing typeObj;
		Type(Thing type0) {typeObj=type0;}
		String id() {return "type";}
		String str() {return typeObj==null?"[null]":typeObj.getName();}
		Thing type() {return typeObj;}
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
