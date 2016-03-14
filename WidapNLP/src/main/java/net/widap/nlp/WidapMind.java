package net.widap.nlp;

import java.util.ArrayList;
import java.util.Random;

public class WidapMind
{
	
	Thing thingStrt=null;
	TextParser parser;
	WordTree dict;
	Random rand;
	static int errorNum=0;
	static final int maxErrorNum=240;
	static final boolean lotsOfChecks=true; //if to run checks on many of the internal data structures
	//turning off may speed things up considerably, and it won't cause any new errors, just fail the catch any that may arise
	
	private boolean quit=false;
	
	public void setQuit(Boolean q) {quit=q;}
	
	public boolean getQuit() {return quit;}
	
	public WidapMind()
	{
		//make a new random generator that will be used throughout the program, seed it with the time
		rand=new Random(System.currentTimeMillis());
		
		dict=new WordTree();
		parser=new TextParser(this);
	}
	
	public String parse(String input)
	{
		return parser.parse(input);
	}
	
	public Thing addThing(Thing in)
	{
		ArrayList<Thing> things=getThings(in);
		
		if (things.size()>1)
		{
			WidapMind.errorMsg("there were multiple things that matched "+in+", aborting thing add");
			return null;
		}
		
		if (things.size()==0)
		{
			in.nxtThing=thingStrt;
			thingStrt=in;
			
			for (int i=0; i<in.props.size(); i++)
			{
				Prop prop=in.props.get(0);
				
				if (prop instanceof Prop.Type)
				{
					Thing other;
					if ((other=addThing(((Prop.Type)prop).type))!=((Prop.Type)prop).type)
					{
						in.removeProp(prop);
						i--;
						
						in.addProp(new Prop.Type(other));
					}
				}
			}
			
			return in;
		}
		else
			return things.get(0);
	}
	
	//you send this method an attrib name and a value to match, its a bit confusing.
	//a normal call would be getThing("name", "tardis")
	//with atrbName being literally the string "name" because you want to find the thing who's name is tardis
	//another call could be getThing("color", "blue"); this would return all the blue things
	public ArrayList<Thing> getThings(String atrbName, String atrbval)
	{
		Thing thing=thingStrt;
		ArrayList<Thing> list=new ArrayList<>();
		
		while (thing!=null)
		{
			ArrayList<String> vals=thing.getValStr(atrbName);
			
			for (String str : vals)
			{
				if (str.equals(atrbval))
				{
					list.add(thing);
				}
			}
			
			thing=thing.nxtThing;
		}
		
		return list;
	}
	
	public ArrayList<Thing> getThings(String name)
	{
		Thing thing=thingStrt;
		ArrayList<Thing> list=new ArrayList<>();
		
		while (thing!=null)
		{
			if (thing.getName().equals(name))
				list.add(thing);
			
			thing=thing.nxtThing;
		}
		
		return list;
	}
	
	//returns all things that in is a subset of
	public ArrayList<Thing> getThings(Thing in)
	{
		Thing thing=thingStrt;
		ArrayList<Thing> list=new ArrayList<>();
		
		while (thing!=null)
		{
			if (thing.contains(in))
				list.add(thing);
			
			thing=thing.nxtThing;
		}
		
		return list;
	}
	
	public static void message(String msg)
	{
		System.out.println("WidapMind message: "+msg);
	}
	
	public static void errorMsg(String msg)
	{
		if (errorNum<maxErrorNum)
		{
			StackTraceElement[] ste=Thread.currentThread().getStackTrace();
			
			System.out.println("WidapMind error in "+ste[2].getClassName()+"."+ste[2].getMethodName()+"(): "+msg);
			
			if (errorNum>=0)
				++errorNum;
		}
		else if (errorNum==maxErrorNum)
		{
			System.out.println("more WidapMind errors hidden");
			++errorNum;
		}
	}
}
