package net.widap.nlp;

import java.util.ArrayList;

/**
 *  Created by william on 3/9/16.
 */

public class Idea//an idea that can be anything from an unrecognised word to a thing with attributes and actions
{
	WidapMind mind;
	
	IdeaNode next, prev;
	
	String str;
	Word.Variant variant;
	boolean plural;
	//ArrayList<Thing> things;
	Thing thing;
	
	private Idea(WidapMind m)
	{
		//if (WidapMind.lotsOfMsgs)
		//	WidapMind.message("made new Idea");
		
		mind=m;
		//things=new ArrayList<>();
		thing=null;
		variant=null;
		plural=false;
		str=null;
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
	
	public Idea(IdeaNode n0, IdeaNode n1, String inStr, Thing inThing, WidapMind m)
	{
		this(n0, n1, inStr, m);
		thing=inThing;
	}
	
	public Idea(IdeaNode n0, IdeaNode n1, String inStr, Thing inThing, boolean inPl, WidapMind m)
	{
		this(n0, n1, inStr, m);
		thing=inThing;
		plural=inPl;
	}
	
	//the following functions are for adding this Idea into a node network and should only be called by the constructors
	
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
		if (thing==null && variant==null)
		{
			ArrayList<Word.Variant> variants=mind.dict.getVariants(str, true);
			
			for (Word.Variant v : variants)
				new Idea(prev, next, str, v, mind).remove(true);
			
			ArrayList<Thing> things=mind.getThings(str);
			
			for (Thing t : things)
				new Idea(prev, next, str, t, mind).remove(true);
		}
	}
	
	//this is where the magic happens
	public boolean merge()
	{
		return false;
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
		return variant==o.variant && thing==o.thing && plural==o.plural && str.equals(o.str);
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
		String out="[no data in Idea]";
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
			out=thing.toString();
		}
		else if (variant!=null)
		{
			out=variant.toString();
		}
		else if (str!=null)
		{
			out="'"+str+"'";
		}
		
		return out;
	}
}