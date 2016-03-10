package net.widap.nlp;

import java.util.ArrayList;

/**
 *  Created by william on 3/9/16.
 */

public class Idea//an idea that can be anything from an unrecognised word to a thing with attributes and actions
{
	WidapMind mind;
	
	Node next, prev;
	
	String str;
	Word.Variant variant;
	boolean plural;
	//ArrayList<Thing> things;
	Thing thing;
	
	public Idea(WidapMind m)
	{
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
	public Idea(Node n0, Node n1, WidapMind m)
	{
		this(m);
		
		if (n0==null && n1==null)
		{
			WidapMind.errorMsg("tried to create TextParser. Idea with both nodes null");
		}
		else if (n0==null)
		{
			splitNAdd(n1.getStart());
		}
		else if (n1==null)
		{
			//if there is only one node in the structure so far, make sure it stays at the start, otherwise, push the last node forward to keep it at the end
			Node end=n0.getEnd();
			if (end==n0.getStart())
				splitNAdd(end);
			else
				splitNAddEnd(end);
		}
		else if (n0==n1)
		{
			splitNAdd(n0);
		}
		else
		{
			addBetween(n0, n1);
		}
	}
	
	public Idea(Node n0, Node n1, String inStr, WidapMind m)
	{
		this(n0, n1, m);
		str=inStr;
	}
	
	//the following functions are for adding this Idea into a node network and should only be called by the constructors
	
	private void addBetween(Node n0, Node n1)
	{
		n0.next.add(this);
		n1.prev.add(this);
		next=n1;
		prev=n0;
	}
	
	//splits the node into 2 nodes and adds this idea in between them (also used for adding at beginning and end)
	//original node always becomes the first of the two
	private void splitNAdd(Node n0)
	{
		Node n1=new Node();
		
		for (Idea idea : n0.next)
		{
			n1.next.add(idea);
		}
		
		n0.next.clear();
		
		addBetween(n0, n1);
	}
	
	//like splitNAdd, but original becomes the end
	private void splitNAddEnd(Node n1)
	{
		Node n0=new Node();
		
		for (Idea idea : n1.prev)
		{
			n0.prev.add(idea);
		}
		
		n1.prev.clear();
		
		addBetween(n0, n1);
	}
	
	//splits the entire structure into 
	public void split()
	{
		WidapMind.message(toString()+" split.");
		
		if (thing==null && variant==null)
		{
			ArrayList<Word.Variant> variants=mind.dict.getVariants(str);
			
			for (Word.Variant v : variants)
			{
				if (v!=variant)
				{
					Idea idea=new Idea(prev, next, str, mind);
					idea.variant=v;
				}
			}
			
			ArrayList<Thing> things=mind.getThings(str);
			
			for (Thing t : things)
			{
				if (t!=thing)
				{
					Idea idea=new Idea(prev, next, str, mind);
					idea.thing=t;
				}
			}
		}
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
	
	public static class Node //nodes connect ideas in a complex data structure
	{
		//in the data structure, by starting at the start node and at each node going to one of the 'next' Ideas and it's 'next' node,
		//you will eventually get to the end (represented by a node with a next ArrayList with length 0)
		//below is a diagram of it with Is being Ideas, Ns being nodes and lines being pointers between them
		//Node does little within its own methods; it is mainly used for storage
		
		//              .-- I --- N --- I --- N --- I --- N --- I --.
		//             /         /       \         /                 \
		// start ---  N --- I --'         '-- N --'                   N --- I --- N --- end
		//             \                                             /
		//              '----------------------- I -----------------'
		
		public ArrayList<Idea> next, prev; //all the Ideas that link to this node and all the ones this node links top
		
		public Node()
		{
			next=new ArrayList<>();
			prev=new ArrayList<>();
		}
		
		//returns the end node of the structure this is part of
		public Node getEnd()
		{
			Node end=this;
			int i=0;
			while (end.next.size()>0)
			{
				//stop endless loops if the data structure is messed up
				if (WidapMind.lotsOfChecks)
				{
					i++;
					
					if (i>100000)
						break;
				}
				
				end=end.next.get(0).next; //search for the last one
			}
			
			if (WidapMind.lotsOfChecks)
			{
				if (i>100000)
				{
					WidapMind.errorMsg("TextParser.Node.getEnd() appeared to get stuck in a loop, indicating that the node structure is bad.");
				}
			}
			
			return end;
		}
		
		//returns the end node of the structure this is part of
		public Node getStart()
		{
			Node start=this;
			int i=0;
			while (start.prev.size()>0)
			{
				//stop endless loops if the data structure is messed up
				if (WidapMind.lotsOfChecks)
				{
					i++;
					
					if (i>100000)
						break;
				}
				
				start=start.prev.get(0).prev; //search for the last one
			}
			
			if (WidapMind.lotsOfChecks)
			{
				if (i>100000)
				{
					WidapMind.errorMsg("TextParser.Node.getEnd() appeared to get stuck in a loop, indicating that the node structure is bad.");
				}
			}
			
			return start;
		}
		
		//if this is part of a structure that contains at least one idea
		public boolean hasIdeas()
		{
			return prev.size()>0 || next.size()>0;
		}
		
		public void splitAll()
		{
			int i;
			
			for (i=0; i<next.size(); i++)
			{
				Idea idea=next.get(i);
				idea.split();
				//idea.next.splitAll();
			}
		}
		
		//has issues
		//turn this entire data structure into a multi-line human readable string
		public String toStringList()
		{
			//make sure it is the start node of the structure
			if (prev.size()>0)
				return getStart().toStringList();
			
			return "all possible node system paths:\n"+listStructInternal();
		}
		
		private String listStructInternal()
		{
			String out="";
			
			if (next.size()==0)
				return "[end]";
			
			if (prev.size()==0)
				out+="[start] -> ";
			
			for (int i=0; i<next.size(); i++)
			{
				if (i>0)
					out+="\n";
				Idea idea=next.get(i);
				out+=idea.toString()+" -> "+idea.next.listStructInternal();
			}
			
			return out;
		}
		
		//display the entire data structure visually using text
		public String toStringVisual()
		{
			//below are the thick unicode box drawing characters that can be copied from
			/*retrieved from http://unicode-table.com/en/blocks/box-drawing/
			━ ┃ ┏ ┓ ┗ ┛ ┣ ┫ ┳ ┻ ╋
			
			
			┏┓        ┏━━━━━━━┓        ┏┓
			┃┃     ┏━━┛       ┗━━┓     ┃┃
			┃┃    ┏┛   ┏━━━━━┓   ┗┓    ┃┃
			┃┃    ┃  ┏━┛     ┗━┓  ┃    ┃┃
			┃┣━━━━┫  ┃         ┃  ┣━━━━┫┃
			┃┣━━━━┫  ┗━┓     ┏━┛  ┣━━━━┫┃
			┃┃    ┃    ┗━━━━━┛    ┃    ┃┃
			┃┃    ┗┓             ┏┛    ┃┃
			┃┃     ┗━━┓       ┏━━┛     ┃┃
			┗┛        ┗━━━━━━━┛        ┗┛
			 */
			
			//yes, that was absolutely necessary in order to test the continuity of the symbols in a practical application
			
			ArrayList<VisualSect> sects=new ArrayList<>();
			getStart().populateSectList(sects);
			
			ArrayList<String> list0=new ArrayList<>();
			list0.add("[START]━");
			sects.add(new VisualSect(new Node(), getStart(), list0));
			
			ArrayList<String> list1=new ArrayList<>();
			list1.add("━[END]");
			sects.add(new VisualSect(getEnd(), new Node(), list1));
			
			WidapMind.message("sect list size: "+sects.size());
			
			boolean stop;
			
			do
			{
				stop=true;
				
				for (int i=0; i<sects.size(); i++)
				{
					VisualSect sect=sects.get(i);
					
					boolean invalid=false;
					VisualSect other=null;
					
					for (int j=0; j<sects.size() && !invalid; j++)
					{
						if (j!=i)
						{
							if (sects.get(j).end==sect.end)
								invalid=true;
							
							if (sect.end==sects.get(j).strt)
							{
								if (other==null)
								{
									other=sects.get(j);
								}
								else
								{
									invalid=true;
								}
							}
						}
					}
					
					if (other!=null && !invalid)
					{ //merge them
						
						stop=false; //if this iteration did anything, stop must be set to false
						
						ArrayList<String> lines=new ArrayList<>();
						
						int lineNum=Math.max(sect.lines.size(), other.lines.size());
						
						for (int j=0; j<lineNum; j++)
						{
							String l0, l1;
							
							if (j<sect.lines.size())
								l0=sect.lines.get(j);
							else
							{
								l0="";
								
								for (int k=0; k<sect.lines.get(0).length(); k++)
									l0+=" ";
							}
							
							if (j<other.lines.size())
								l1=other.lines.get(j);
							else
							{
								l1="";
								
								for (int k=0; k<other.lines.get(0).length(); k++)
									l1+=" ";
							}
							
							lines.add(l0+l1);
						}
						
						sects.set(i, new VisualSect(sect.strt, other.end, lines));
						sects.remove(other);
					}
				}
				
				for (int i=0; i<sects.size(); i++)
				{
					VisualSect sect=sects.get(i);
					
					ArrayList<VisualSect> others=new ArrayList<>();
					
					for (int j=0; j<sects.size(); j++)
					{
						//it is ok that j will equal i once
						
						VisualSect other=sects.get(j);
						
						if (other.strt==sect.strt && other.end==sect.end)
						{
							others.add(other);
						}
					}
					
					if (others.size()>1)
					{ //merge them
						
						stop=false; //if this iteration did anything, stop must be set to false
						
						ArrayList<String> lines=new ArrayList<>();
						
						int maxLng=0;
						
						for (VisualSect other : others)
							maxLng=Math.max(other.lines.get(0).length(), maxLng);
						
						for (int j=0; j<others.size(); j++)
						{
							ArrayList<String> oLines=others.get(j).lines;
							String linePad="", spacePad="";
							
							//━ ┃ ┏ ┓ ┗ ┛ ┣ ┫ ┳ ┻ ╋
							
							for (int k=0; k<maxLng-oLines.get(0).length(); k++)
							{
								spacePad+=" ";
								linePad+="━";
							}
							
							for (int k=0; k<oLines.size(); k++)
							{
								String str=oLines.get(k);
								String strt, end;
								String pad;
								
								if (k==0)
								{
									pad=linePad;
									
									if (j==0)
									{
										if (others.size()>1)
										{
											strt="┳";
											end="┳";
										}
										else
										{
											strt="━";
											end="━";
										}
									}
									else if (j<others.size()-1)
									{
										strt="┣";
										end="┫";
									}
									else
									{
										strt="┗";
										end="┛";
									}
								}
								else
								{
									pad=spacePad;
									
									if (j<others.size()-1)
									{
										strt="┃";
										end="┃";
									}
									else
									{
										strt=" ";
										end=" ";
									}
								}
								
								lines.add(strt+str+pad+end);
							}
						}
						
						for (VisualSect other : others)
							sects.remove(other);
						
						sects.add(i, new VisualSect(sect.strt, sect.end, lines));
					}
				}
			}
			while (!stop);
			
			if (sects.size()==1)
			{
				String comp="node structure diagram:\n";
				
				for (String line : sects.get(0).lines)
				{
					comp+=line+"\n";
				}
				
				return comp;
			}
			else
			{
				String comp="data structure visualizer failed, here are the incomplete sects::\n";
				
				for (VisualSect sect : sects)
				{
					for (String line : sect.lines)
					{
						comp+=line+"\n";
					}
				}
				
				return comp;
			}
		}
		
		//section in the visual display of the node structure
		class VisualSect
		{
			public final Node strt, end;
			public final ArrayList<String> lines;
			
			VisualSect(Node s, Node e, ArrayList<String> l)
			{
				strt=s;
				end=e;
				lines=l;
			}
		}
		
		private void populateSectList(ArrayList<VisualSect> sects)
		{
			for (Idea idea : next)
			{
				ArrayList<String> lines=new ArrayList<>();
				lines.add("━["+idea.toString()+"]━");
				sects.add(new VisualSect(this, idea.next, lines));
				
				boolean dupli=false;
				for (VisualSect sect : sects)
				{
					if (sect.strt==idea.next)
						dupli=true;
				}
				
				if (!dupli)
				{
					idea.next.populateSectList(sects);
				}
			}
		}
		
		//checks to make sure everything is right in the data structure this starts (will have error if this is not the starting node)
		public void checkStructure()
		{
			if (prev.size()>0)
			{
				WidapMind.errorMsg("called TextParser.Node.checkStructure() on non starting node.");
			}
			
			checkInternal(new ArrayList<Node>());
		}
		
		//called by checkStructure, recursively looks through the list and returns the end node to make sure the list collapses down into a single end node
		private Node checkInternal(ArrayList<Node> nodes)
		{
			Node end=getEnd();
			
			for (Node node : nodes)
			{
				if (node==this)
				{
					WidapMind.errorMsg("loop detected in node data structure in text parser. THIS IS VERY BAD!!!");
					return end;
				}
			}
			
			nodes.add(this);
			
			for (Idea idea : prev)
			{
				if (idea.next!=this)
				{
					WidapMind.errorMsg("idea pointed backward at by node does not point forward to the same node in text parser");
				}
			}
			
			for (Idea idea : next)
			{
				if (idea.prev!=this)
				{
					WidapMind.errorMsg("idea pointed to by node does not point back at the same node in text parser");
				}
				
				if (idea.next.checkInternal(nodes)!=end)
				{
					WidapMind.errorMsg("Node sentence structure in text parser did not end at a single node.");
				}
			}
			
			nodes.remove(this);
			
			return end;
		}
	}
}