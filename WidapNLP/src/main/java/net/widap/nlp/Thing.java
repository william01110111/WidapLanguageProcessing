package net.widap.nlp;

import java.util.ArrayList;

public class Thing
{
	//do not add or remove from this list manually, use addProp and removeProp
	public ArrayList<Prop> props;
	public boolean isAbstract;
	public Thing nxtThing=null;
	
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
	
	public void addProp(Prop prop)
	{
		if (prop instanceof Prop.Instance)
		{
			WidapMind.errorMsg("tried to add Prop.Instance directly to thing, this is a no-no");
			return;
		}
		else if (prop instanceof Prop.Type)
		{
			if (!((Prop.Type)prop).type.isAbstract)
				WidapMind.errorMsg("set "+getName()+"'s type to concrete thing, "+((Prop.Type)prop).type.getName());
			
			((Prop.Type)prop).type.props.add(new Prop.Instance(this));
		}
		else if (prop instanceof Prop.Abstract)
		{
			if (isAbstract)
			{
				WidapMind.errorMsg("added abstract prop twice to "+toString());
				return;
			}
			
			isAbstract=true;
		}
		
		props.add(prop);
	}
	
	public void removeProp(Prop prop)
	{
		if (prop instanceof Prop.Instance)
		{
			Thing other=((Prop.Instance)prop).instance;
			other.removeProp(other.getProp(new Prop.Type(this)));
		}
		else
		{
			if (prop instanceof Prop.Type) 
			{
				ArrayList<Prop> otherProps=((Prop.Type)prop).type.props;
				
				for (int i=0; i<otherProps.size(); i++)
				{
					Prop prop0=otherProps.get(i);
					
					if (prop0 instanceof Prop.Instance)
					{
						if (((Prop.Instance)prop0).instance==((Prop.Type)prop).type)
						{
							otherProps.remove(i);
							return;
						}
					}
				}
			}
			else if (prop instanceof Prop.Abstract)
			{
				isAbstract=false;
			}
			
			props.remove(prop);
		}
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
		Prop prop=getProp(Prop.Type.class);
		
		if (prop==null)
			return null;
		else
			return ((Prop.Type)prop).type;
	}
	
	public ArrayList<Thing> getTypes()
	{
		ArrayList<Thing> things=new ArrayList<>();
		
		for (Prop prop : props)
		{
			if (prop instanceof Prop.Type)
			{
				things.add(((Prop.Type)prop).type);
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
	
	public Prop getProp(Prop in)
	{
		for (Prop prop : props)
		{
			if (prop.equals(in))
				return prop;
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
	
	public boolean equals(Thing other)
	{
		return other.props.size()==props.size() && contains(other);
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
