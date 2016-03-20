package net.widap.nlp;

import java.util.ArrayList;

/**
 *  Created by william on 3/9/16.
 */

//an idea that can be anything from an unrecognised word to a thing with attributes and actions
public class Idea
{
	WidapMind mind;
	
	IdeaNode next, prev;
	
	public String str;
	public Word.Variant variant;
	public ArrayList<Prop> props;
	public boolean plural;
	//ArrayList<Thing> things;
	public Thing thing;
	
	private Idea(WidapMind m)
	{
		//if (WidapMind.lotsOfMsgs)
		//	WidapMind.message("made new Idea");
		
		mind=m;
		variant=null;
		props=new ArrayList<>();
		plural=false;
		//things=new ArrayList<>();
		thing=null;
		str="";
	}
	
	//if either is null, the other is only used to find the correct data structure, which node it is doesn't matter
	//if n0 is null, inserts this idea at the very beginning and creates new 2nd node to come after it that contains all the pointers the old beginning node had
	//old beginning node pointers stay valid and still point to the first node
	//if n1 is null, inserts this idea at the very end and creates a new 2nd-to-last node which contains the same previous pointers as the old end node
	//old end node pointers stay valid and still point to the last node,
	//unless there was originally only one node in the whole structure, in which case that becomes the first
	//if neither is null and they are both different, this is inserted between them without creating any nodes
	//if they are the same and not null, that node is split (the original becomes the first of the two) and this is inserted
	//if both are null, and error is thrown
	public Idea(IdeaNode n0, IdeaNode n1, WidapMind m)
	{
		this(m);
		
		if (n0==null && n1==null)
		{
			WidapMind.errorMsg("tried to create TextParser.Idea with both nodes null");
		}
		else if (n0==null)
		{
			addBetween(n1.getStart(), false);
		}
		else if (n1==null)
		{
			//if there is only one node in the structure so far, make sure it stays at the start, otherwise, push the last node forward to keep it at the end
			IdeaNode end=n0.getEnd();
			if (end==n0.getStart())
				addBetween(end, false);
			else
				addBetween(end, true);
		}
		else if (n0==n1)
		{
			addBetween(n0, false);
		}
		else
		{
			addBetween(n0, n1);
		}
	}
	
	public Idea(IdeaNode n0, IdeaNode n1, String inStr, WidapMind m)
	{
		this(n0, n1, m);
		str=inStr;
	}
	
	public Idea(IdeaNode n0, IdeaNode n1, String inStr, Word.Variant inVar, WidapMind m)
	{
		this(n0, n1, inStr, m);
		variant=inVar;
	}
	
	public Idea(IdeaNode n0, IdeaNode n1, String inStr, Prop inProp, WidapMind m)
	{
		this(n0, n1, inStr, m);
		props.add(inProp);
	}
	
	public Idea(IdeaNode n0, IdeaNode n1, String inStr, ArrayList<Prop> inProps, WidapMind m)
	{
		this(n0, n1, inStr, m);
		props.addAll(inProps);
	}
	
	public Idea(IdeaNode n0, IdeaNode n1, String inStr, Thing inThing, WidapMind m)
	{
		this(n0, n1, inStr, m);
		thing=inThing.copy();
	}
	
	public Idea(IdeaNode n0, IdeaNode n1, String inStr, Thing inThing, boolean inPl, WidapMind m)
	{
		this(n0, n1, inStr, inThing, m);
		plural=inPl;
	}
	
	public Idea(IdeaData data, WidapMind m)
	{
		this(data.prev, data.next, m);
		str=data.str;
		variant=data.variant;
		props.addAll(data.props);
		plural=data.plural;
		thing=data.thing; //its ok that it doesn't make a copy, the Thing in IdeaData should be unique
	}
	
	//the following two functions are for adding this Idea into a node network and should only be called by the constructors
	private void addBetween(IdeaNode n0, IdeaNode n1)
	{
		n0.ideas.add(this);
		n0.next.add(this);
		n1.prev.add(this);
		next=n1;
		prev=n0;
	}
	
	//splits the node into 2 nodes and adds this idea in between them (also used for adding at beginning and end)
	//if oldIsEnd, original node becomes the latter of the two, otherwise it goes first
	private void addBetween(IdeaNode n, boolean oldIsEnd)
	{
		if (oldIsEnd)
		{
			IdeaNode n0=new IdeaNode(n.ideas);
			
			for (Idea idea : n.prev)
			{
				n0.prev.add(idea);
				idea.next=n0;
			}
			
			n0.prev.clear();
			
			addBetween(n0, n);
		}
		else
		{
			IdeaNode n0=new IdeaNode(n.ideas);
			
			for (Idea idea : n.next)
			{
				n0.next.add(idea);
				idea.prev=n0;
			}
			
			n.next.clear();
			
			addBetween(n, n0);
		}
	}
	
	public void split()
	{
		if (variant!=null || props.size()!=0 || thing!=null)
			return;
		
		ArrayList<Word.Variant> variants=mind.dict.getVariants(str, true);
		
		for (Word.Variant v : variants)
			new Idea(prev, next, str, v, mind).remove(true);
		
		ArrayList<Thing> things;
		
		//get the singular version of the word
		String strSin=mind.dict.switchPOS(str, Word.POS.NOUN);
		
		things=mind.getThings(str);
		
		for (Thing t : things)
			new Idea(prev, next, str, t, false, mind).remove(true);
		
		if (!str.equals(strSin))
		{
			things=mind.getThings(strSin);
			
			for (Thing t : things)
				new Idea(prev, next, str, t, true, mind).remove(true);
		}
		
		boolean hasAdj=false;
		for (Word.Variant v : variants)
			if (Word.posMatches(Word.POS.ADJ, v.pos))
				hasAdj=true;
		
		if (hasAdj || variants.size()==0)
			new Idea(prev, next, str, new Prop.Attrib(strSin), mind).remove(true);
		
		boolean hasNoun=false;
		for (Word.Variant v : variants)
			if (Word.posMatches(Word.POS.NOUN, v.pos))
				hasNoun=true;
		
		if (hasNoun)
		{
			Thing thing=new Thing(strSin);
			thing.addProp(new Prop.Abstract());
			new Idea(prev, next, str, thing, false, mind).remove(true);
		}
		
		if (variants.size()==0)
			new Idea(prev, next, str, new Thing(strSin), false, mind).remove(true);
		
		boolean hasPlNoun=false;
		for (Word.Variant v : variants)
			if (Word.posMatches(Word.POS.NOUN_PL, v.pos))
				hasPlNoun=true;
		
		if (hasPlNoun)
		{
			Thing thing=new Thing(strSin);
			thing.addProp(new Prop.Abstract());
			new Idea(prev, next, str, thing, true, mind).remove(true);
		}
		
		if (variants.size()==0)
			new Idea(prev, next, str, new Thing(strSin), true, mind).remove(true);
	}
	
	//this is where the magic happens
	public void merge()
	{
		ArrayList<IdeaData> ideas=new ArrayList<>();
		
		getThingsFwd(ideas);
		getPropsFwd(ideas);
		
		for (IdeaData data : ideas)
			new Idea(data, mind).remove(true);
		
		if (thing!=null)
		{
			
		}
		else if (props.size()!=0)
		{
			
		}
		else if (variant!=null)
		{
			/*if (Word.posMatches(Word.POS.ADJ, variant.pos))
			{
				ArrayList<IdeaData> ideas=new ArrayList<>();
				
				getThingsFwd(ideas);
				
				for (IdeaData data : ideas)
					new Idea(data, mind).remove(true);
			}*/
		}
		else
		{
			switch (str)
			{
			}
		}
	}
	
	private void getThingsFwd(ArrayList<IdeaData> data)
	{
		if (thing!=null)
		{
			data.add(new IdeaData(prev, next, str, thing, plural));
		}
		else if (props.size()>0)
		{
			int start=data.size();
			
			for (Idea idea : next.next)
			{
				idea.getThingsFwd(data);
			}
			
			for (int i=start; i<data.size(); i++)
			{
				IdeaData elem=data.get(i);
				
				if (elem.thing!=null && elem.thing.isAbstract)
				{
					Thing thing=new Thing();
					thing.addProp(new Prop.Type(elem.thing));
					
					for (Prop prop : props)
						thing.addProp(prop);
					
					elem.thing=thing; //note that the plurality of the thing stays what it was
					
					elem.str=str+" "+elem.str;
					elem.prev=prev;
				}
				else
				{
					data.remove(i);
					i--;
				}
			}
		}
		else if (variant!=null)
		{
			
		}
		else
		{
			if (data.size()==0)
			{
				switch (str)
				{
				case "the":
					
					int start=data.size();
					
					for (Idea idea : next.next)
						idea.getThingsFwd(data);
					
					for (int i=start; i<data.size(); i++)
					{
						IdeaData elem=data.get(i);
						
						if (elem.thing!=null)
						{
							if (elem.thing.isAbstract)
							{
								Thing thing=new Thing();
								thing.addProp(new Prop.Type(elem.thing));
								elem.thing=thing;
							}
							
							Thing type=elem.thing.getType();
							
							if (type==null)
								break;
							
							type.addProp(new Prop.DefaultInstance(elem.thing));
							
							elem.str=str+" "+elem.str;
							elem.prev=prev;
						}
						else
						{
							data.remove(i);
							i--;
						}
					}
					
					break;
				}
			}
		}
	}
	
	private void getPropsFwd(ArrayList<IdeaData> data)
	{
		if (props.size()>0)
		{
			int start=data.size(); //stops the method from interfering with elements that do not go through it
			
			for (Idea idea : next.next)
				idea.getPropsFwd(data);
			
			if (data.size()==start)
				data.add(new IdeaData(prev, next, ""));
			
			for (int i=start; i<data.size(); i++)
			{
				IdeaData elem=data.get(i);
				
				for (Prop prop : props)
					elem.props.add(prop);
				
				elem.prev=prev;
				
				if (elem.str.length()>0)
					elem.str=" "+elem.str;
				
				elem.str=str+elem.str;
			}
		}
	}
	
	private void mergeConjunctions(ArrayList<IdeaData> data)
	{
		if (thing!=null || props.size()>0 || variant!=null)
		{
			switch (str)
			{
			case "and":
				for (Idea idea0 : prev.prev)
				{
					if (idea0.thing==null && idea0.props.size()>0)
					{
						for (Idea idea1 : next.next)
						{
							if (idea1.thing==null && idea1.props.size()>0)
							{
								ArrayList<Prop> allProps=new ArrayList<>();
								allProps.addAll(idea0.props);
								allProps.addAll(idea1.props);
								new IdeaData(idea0.prev, idea1.next, idea0.str+" "+str+" "+idea1.str, allProps);
							}
						}
					}
				}
				break;
			
			case "is":
				for (Idea idea0 : prev.prev)
				{
					if (idea0.thing!=null && !idea0.plural && !idea0.thing.isAbstract)
					{
						for (Idea idea1 : next.next)
						{
							if (idea1.props.size()>0)
							{
								Idea idea=new Idea(idea0.prev, idea1.next, idea0.str+" "+str+" "+idea1.str, idea0.thing, false, mind);
								idea.props.addAll(idea1.props);
								idea.remove(true);
							}
						}
					}
				}
				break;
			
			case "are":
				for (Idea idea0 : prev.prev)
				{
					if (idea0.thing!=null && idea0.plural)
					{
						for (Idea idea1 : next.next)
						{
							if (idea1.props.size()>0)
							{
								Idea idea=new Idea(idea0.prev, idea1.next, idea0.str+" "+str+" "+idea1.str, idea0.thing, true, mind);
								idea.props.addAll(idea1.props);
								idea.remove(true);
							}
						}
					}
				}
				break;
			}
		}
	}
	
	//searches to see id this Idea has a duplicate in the same place in the structure
	private boolean isDuplicate()
	{
		for (Idea other : prev.next)
		{
			if (other!=this && next==other.next && equals(other))
				return true;
		}
		
		return false;
	}
	
	//if this idea is equal to another idea (note that to return true, the Things must be the exact same thing, not just matching things)
	public boolean equals(Idea o)
	{
		if (this==o)
			return true;
		
		if (variant!=o.variant)
			return false;
		
		if (props.size()!=o.props.size())
			return false;
		else
		{ //assumes properties are in the same order, will return not equal otherwise
			for (int i=0; i<props.size(); i++)
			{
				if (!props.get(i).equals(o.props.get(i)))
					return false;
			}
		}
		
		if (thing!=o.thing)
		{
			if (thing==null || o.thing==null || !thing.equals(o.thing))
				return false;
		}
		
		if (plural!=o.plural)
			return false;
		
		if (!str.equals(o.str))
			return false;
		
		return true;
	}
	
	//if this idea contains another idea
	public boolean contains(Idea o)
	{
		if (variant!=o.variant)
			return false;
		
		if (thing!=null && o.thing!=null && !thing.contains(o.thing))
			return false;
		
		if (thing==null && o.thing!=null)
			return false;
		
		
		if (props.size()<o.props.size())
			return false;
		
		for (Prop prop : props)
		{
			boolean hasMatch=false;
			
			for (Prop oProp : o.props)
			{
				if (prop.equals(oProp))
				{
					hasMatch=true;
					break;
				}
			}
			
			if (!hasMatch)
				return false;
		}
		
		return true;
	}
	
	//removes this idea from the structure, it then becomes invalid and trying to use us is a bad idea
	public void remove(boolean onlyIfDupli)
	{
		if (onlyIfDupli && !isDuplicate())
			return;
		
		//if (WidapMind.lotsOfMsgs)
		//	WidapMind.message(toString()+" removed from data structure");
		
		next.ideas.remove(this);
		
		prev.next.remove(this);
		next.prev.remove(this);
		next=null;
		prev=null;
	}
	
	public String toString()
	{
		String out="";
			/*if (things.size()>0)
			{
				ArrayList<String> list=new ArrayList<>();
				for (Thing thing : things)
				{
					list.add(thing.toString());
				}
				out=combineList(list);
			}*/
		if (thing!=null)
		{
			out+=thing.toString();
			if (plural)
				out+="(PL)";
		}
		
		if (props.size()>0)
		{
			if (!out.equals(""))
				out+="←";
			
			out+="[";
			
			for (int i=0; i<props.size(); i++)
			{
				Prop prop=props.get(i);
				out+=prop;
				if (i<props.size()-1)
					out+=", ";
			}
			
			out+="]";
		}
		
		if (variant!=null)
		{
			out+="("+variant.toString()+")";
		}
		
		if (out.equals("") && str!=null)
		{
			out+="'"+str+"'";
		}
		
		if (out.equals(""))
			out="[no data in Idea]";
		
		return out;
	}
	
	//holds most ofd the data of an idea, and you can construct an idea from it but it does not have the same methods or live in a data structure
	private static class IdeaData
	{
		IdeaNode next, prev; //note that IdeaData can not be part of the data structure, this is just information on how to place an Idea made from it
		public String str;
		public Word.Variant variant;
		public ArrayList<Prop> props;
		public boolean plural;
		//ArrayList<Thing> things;
		public Thing thing;
		
		IdeaData()
		{
			next=null;
			prev=null;
			str="";
			props=new ArrayList<>();
			variant=null;
			plural=false;
			thing=null;
		}
		
		IdeaData(IdeaNode prevIn, IdeaNode nextIn, String strIn)
		{
			this();
			next=nextIn;
			prev=prevIn;
			str=strIn;
		}
		
		IdeaData(IdeaNode prevIn, IdeaNode nextIn, String strIn, Word.Variant variantIn)
		{
			this(prevIn, nextIn, strIn);
			variant=variantIn;
		}
		
		IdeaData(IdeaNode prevIn, IdeaNode nextIn, String strIn, Thing thingIn)
		{
			this(prevIn, nextIn, strIn);
			thing=thingIn.copy();
		}
		
		IdeaData(IdeaNode prevIn, IdeaNode nextIn, String strIn, Thing thingIn, boolean plIn)
		{
			this(prevIn, nextIn, strIn);
			thing=thingIn.copy();
			plural=plIn;
		}
		
		IdeaData(IdeaNode prevIn, IdeaNode nextIn, String strIn, ArrayList<Prop> propsIn)
		{
			this(prevIn, nextIn, strIn);
			props.addAll(propsIn);
		}
		
		IdeaData(IdeaNode prevIn, IdeaNode nextIn, String strIn, Prop prop)
		{
			this(prevIn, nextIn, strIn);
			props.add(prop);
		}
		
	}
}