package net.widap.nlp;

import java.util.ArrayList;

public class Thing
{
	//do not add or remove from this list manually, use addProp and removeProp
	public ArrayList<Prop> props;
	private boolean boolProps[];
	public Thing nxtThing=null;
	
	public static final String defaultName="[unnamed thing]";
	
	Thing()
	{
		props=new ArrayList<>();
		boolProps=new boolean[Attrib.values().length];
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
		if (WidapMind.extraMessages)
		{
			WidapMind.message("adding ["+prop+"] to "+this);
		}
		
		if (prop instanceof Prop.Attrib)
		{
			if (boolProps[((Prop.Attrib)prop).val.ordinal()])
			{
				WidapMind.errorMsg("tried to add "+((Prop.Attrib)prop).val.name()+" twice to "+toString());
				return;
			}
			
			boolProps[((Prop.Attrib)prop).val.ordinal()]=true;
		}
		
		prop=prop.getPropToAdd(this);
		props.add(prop);
		
		for (int i=0; i<props.size()-1; i++)
		{
			Prop prop0=props.get(i);
			
			if (prop0.equals(prop))
			{
				removeProp(props.size()-1);
				if (WidapMind.lotsOfChecks)
					WidapMind.message("["+prop+"] was already in "+this.getName());
				break;
			}
		}
	}
	
	public void addProps(ArrayList<Prop> props)
	{
		for (Prop prop : props)
			addProp(prop);
	}
	
	//removes only the last instance
	public void removeProp(Prop prop)
	{
		for (int i=props.size()-1; i>=0; i--)
		{
			if (props.get(i)==prop)
			{
				removeProp(i);
				return;
			}
		}
		
		if (WidapMind.lotsOfChecks)
		{
			WidapMind.errorMsg("could not find ["+prop+"] in "+this+" (keep in mind it has to be the same instance, not just equivalent)");
			new Exception().printStackTrace();
		}
	}
	
	public void removeProp(int i)
	{
		Prop prop=props.get(i);
		props.remove(i);
		prop.removedFromThing(this);
	}
	
	public void removeAllProps()
	{
		while (props.size()>0)
			removeProp(props.size()-1);
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
	
	public boolean is(Attrib in)
	{
		return boolProps[in.ordinal()];
	}
	
	public Thing getType()
	{
		for (Prop prop : props)
		{
			if (prop instanceof Prop.Type)
			{
				return ((Prop.Type)prop).other;
			}
			else if (prop instanceof Prop.LinkTemp && ((Prop.LinkTemp)prop).linkClass==Prop.Type.class)
			{
				return ((Prop.LinkTemp)prop).other;
			}
		}
		
		return null;
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
			else if (prop instanceof Prop.LinkTemp && ((Prop.LinkTemp)prop).linkClass==Prop.Type.class)
			{
				things.add(((Prop.LinkTemp)prop).other);
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
	public boolean contains(Thing other, boolean ignoreLinks)
	{
		if (ignoreLinks)
		{
			for (Prop otherProp : other.props)
			{
				if (!(otherProp instanceof Prop.Link) && !(otherProp instanceof Prop.LinkTemp) && !hasProp(otherProp))
					return false;
			}
		}
		else
		{
			for (Prop otherProp : other.props)
			{
				if (!hasProp(otherProp))
					return false;
			}
		}
		
		return true;
	}
	
	public boolean equals(Thing other)
	{
		WidapMind.errorMsg("needed to call with ignoreLinks variable");
		new Exception().printStackTrace();
		return false;
	}
	
	public boolean equals(Thing other, boolean ignoreLinks)
	{
		return this==other || (other.props.size()==props.size() && contains(other, ignoreLinks));
	}
	
	public void check()
	{
		for (Prop prop : props)
		{
			prop.check();
		}
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
