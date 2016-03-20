package net.widap.nlp;

import java.util.ArrayList;

public class Thing
{
	//do not add or remove from this list manually, use addProp and removeProp
	public ArrayList<Prop> props;
	public boolean isAbstract;
	public Thing nxtThing=null;
	
	public static final String defaultName="[unnamed thing]";
	
	Thing()
	{
		props=new ArrayList<>();
		isAbstract=false;
	}
	
	Thing(String name)
	{
		this(); //calls other constructor
		
		addProp(new Prop.Name(name));
	}
	
	Thing copy()
	{
		Thing out=new Thing();
		
		for (Prop prop : props)
			out.addProp(prop); //no need to call copy as it will automatically be called if needed from getPropToAdd()
			//out.addProp(prop.copy()); //this will usually make a shallow copy but will make a deep one when needed
		
		return out;
	}
	
	public ArrayList<String> getValStr(String id)
	{
		ArrayList<String> output=new ArrayList<>();
		
		for (Prop prop : props)
		{
			if (prop.id().equals(id))
				output.add(prop.str());
		}
		
		return output;
	}
	
	public void addProp(Prop prop)
	{
		if (prop instanceof Prop.Abstract)
		{
			if (isAbstract)
			{
				WidapMind.errorMsg("added abstract prop twice to "+toString());
				return;
			}
			
			isAbstract=true;
		}
		
		props.add(prop.getPropToAdd(this));
	}
	
	public void addProps(ArrayList<Prop> props)
	{
		for (Prop prop : props)
			addProp(prop);
	}
	
	public void removeProp(Prop prop)
	{
		props.remove(prop);
		prop.removedFromThing(this);
	}
	
	//returns the string of the first name type property
	public String getName()
	{
		Prop prop;
		
		prop=getProp(Prop.Name.class);
		
		if (prop!=null)
			return prop.str();
		
		prop=getProp(Prop.DefaultOfType.class);
		
		if (prop!=null)
			return "the "+prop.str();
		
		prop=getProp(Prop.Type.class);
		
		if (prop!=null)
			return "a "+prop.str();
		
		return defaultName;
	}
	
	//simply returns if the given string matches at least one name
	public boolean checkName(String name)
	{
		ArrayList<Prop> props=getProps(Prop.Name.class);
		
		for (Prop prop : props)
		{
			if (prop.str().equals(name))
				return true;
		}
		
		return false;
	}
	
	public Thing getType()
	{
		Prop prop=getProp(Prop.Type.class);
		
		if (prop==null)
			return null;
		else
			return ((Prop.Type)prop).other;
	}
	
	public ArrayList<Thing> getTypes()
	{
		ArrayList<Thing> things=new ArrayList<>();
		
		for (Prop prop : props)
		{
			if (prop instanceof Prop.Type)
			{
				things.add(((Prop.Type)prop).other);
			}
		}
		
		return things;
	}
	
	public boolean hasProp(Prop other)
	{
		for (Prop prop : props)
		{
			if (prop.equals(other))
				return true;
		}
		
		return false;
	}
	
	public Prop getProp(Class propClass)
	{
		for (Prop prop : props)
		{
			if (propClass.isInstance(prop))
			{
				return prop;
			}
		}
		
		return null;
	}
	
	public ArrayList<Prop> getProps(Class propClass)
	{
		ArrayList<Prop> props=new ArrayList<>();
		
		for (Prop prop : props)
		{
			if (propClass.isInstance(prop))
			{
				props.add(prop);
			}
		}
		
		return props;
	}
	
	public Prop getProp(Prop in)
	{
		for (Prop prop : props)
		{
			if (prop.equals(in))
				return prop;
		}
		
		return null;
	}
	
	//essentially merges the other thing into this one
	//does not mess with the linked list
	public void appendThing(Thing other)
	{
		for (Prop prop : other.props)
		{
			boolean repeat=false;
			
			//loop to check to see if the old an new prop
			for (Prop prop1 : props)
			{
				if (prop.equals(prop1))
					repeat=true;
			}
			
			if (!repeat)
				addProp(prop.copy());
		}
	}
	
	//returns if the properties of the other thing are a subset of this thing's properties
	public boolean contains(Thing other)
	{
		for (Prop otherProp : other.props)
		{
			if (!hasProp(otherProp))
				return false;
		}
		
		return true;
	}
	
	public boolean equals(Thing other)
	{
		return this==other || (other.props.size()==props.size() && contains(other));
	}
	
	public String toString()
	{
		String out="{";
		
		for (int i=0; i<props.size(); i++)
		{
			Prop prop=props.get(i);
			out+=prop;
			if (i<props.size()-1)
				out+=", ";
		}
		
		out+="}";
		
		return out;
	}
}
