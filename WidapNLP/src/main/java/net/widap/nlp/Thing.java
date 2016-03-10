package net.widap.nlp;

import java.util.ArrayList;

public class Thing
{
	public ArrayList<Prop> props;
	public Thing nxtThing=null;
	
	Thing()
	{
		props=new ArrayList<>();
	}
	
	Thing(String name)
	{
		this(); //calls other constructor
		
		addProp(new Prop.Name(name));
	}
	
	//only a shallow copy because properties are supposed to be immutable
	Thing copy()
	{
		Thing out=new Thing();
		
		for (Prop prop : props)
			out.addProp(prop);
		
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
	
	public void addProp(Prop newProp)
	{
		props.add(newProp);
	}
	
	//returns the string of the first name type property
	public String getName()
	{
		Prop prop=getProp(Prop.Name.class);
		
		if (prop==null)
			return "[unnamed thing]";
		else
			return prop.str();
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
		Prop prop=getProp(Prop.Name.class);
		
		if (prop==null)
			return null;
		else
			return ((Prop.Type)prop).type();
	}
	
	public ArrayList<Thing> getTypes()
	{
		ArrayList<Prop> props=getProps(Prop.Name.class);
		ArrayList<Thing> things=new ArrayList<>();
		
		for (Prop prop : props)
		{
			if (((Prop.Type)prop).type()!=null)
			{
				things.add(((Prop.Type)prop).type());
			}
		}
		
		return things;
	}
	
	public Prop.VarietyEnum getVariety()
	{
		Prop prop=getProp(Prop.Name.class);
		
		if (prop==null)
			return Prop.VarietyEnum.OTHER;
		else
			return ((Prop.Variety)prop).variety();
	}
	
	boolean isNoun()
	{
		for (Prop prop : props)
		{
			if (prop.getClass().equals(Prop.Variety.class))
			{
				if (((Prop.Variety)prop).isNoun())
					return true;
			}
		}
		
		return false;
	}
	
	boolean isVerb()
	{
		for (Prop prop : props)
		{
			if (prop.getClass().equals(Prop.Variety.class))
			{
				if (((Prop.Variety)prop).isVerb())
					return true;
			}
		}
		
		return false;
	}
	
	boolean isAbstract()
	{
		for (Prop prop : props)
		{
			if (prop.getClass().equals(Prop.Variety.class))
			{
				if (((Prop.Variety)prop).isAbstract())
					return true;
			}
		}
		
		return false;
	}
	
	boolean isConcrete()
	{
		for (Prop prop : props)
		{
			if (prop.getClass().equals(Prop.Variety.class))
			{
				if (((Prop.Variety)prop).isConcrete())
					return true;
			}
		}
		
		return false;
		
		//return ((Prop.Variety)getProp(Prop.Variety)).isConcrete();
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
	
	//essentially merges the other thing into this one
	//does not mess with the linked list
	//the attributes that are added to this thing remain linked to the old thing, so it is recommended that the old thing is gotten rid of
	public void appendThing(Thing other)
	{
		for (Prop prop : other.props)
		{
			boolean repeat=false;
			
			for (Prop prop1 : props)
			{
				if (prop.getClass().equals(prop1.getClass()) && prop.id().equals(prop1.id()) && prop.str().equals(prop1.str()))
					repeat=true;
			}
			
			if (!repeat)
				props.add(prop);
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
	
	public String toString()
	{
		Prop prop;
		String out="(";
		
		for (int i=0; i<props.size(); i++)
		{
			prop=props.get(i);
			out+=prop.id()+": "+prop.str()+(i==props.size()-1?"":", ");
		}
		
		out+=")";
		
		return out;
	}
}
