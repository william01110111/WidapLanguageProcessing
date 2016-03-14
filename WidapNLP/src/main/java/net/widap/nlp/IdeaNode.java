package net.widap.nlp;

import java.util.ArrayList;

/**
 *  Created by william on 3/10/16.
 */
public class IdeaNode //nodes connect ideas in a complex data structure
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
	
	public ArrayList<Idea> ideas; //order is not important, these should all point to the same list for the nodes of any one structure structure
	
	public IdeaNode(ArrayList<Idea> inIdeas)
	{
		ideas=inIdeas;
		next=new ArrayList<>();
		prev=new ArrayList<>();
	}
	
	//returns the end node of the structure this is part of
	public IdeaNode getEnd()
	{
		IdeaNode end=this;
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
	public IdeaNode getStart()
	{
		IdeaNode start=this;
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
	
	//returns all the ideas that span from the start to the end
	public ArrayList<Idea> getSingleIdeas()
	{
		IdeaNode start=getStart(), end=getEnd();
		ArrayList<Idea> out=new ArrayList<>();
		
		for (Idea idea : start.next)
		{
			if (idea.next==end)
			{
				out.add(idea);
			}
		}
		
		return out;
	}
	
	//if this is part of a structure that contains at least one idea
	public boolean hasIdeas()
	{
		return prev.size()>0 || next.size()>0;
	}
	
	public void splitAll()
	{
		for (int i=0; i<ideas.size(); i++) 
		{
			Idea idea=ideas.get(i);
			idea.split();
		}
	}
	
	public void mergeAll()
	{
		int ideaNum;
		
		do
		{
			ideaNum=ideas.size();
			
			for (int j=0; j<ideas.size(); j++)
			{
				if (j>10000)
				{
					WidapMind.errorMsg("merge loop timed out, indicating either an endless loop or just a hella complicated sentence");
					break;
				}
				
				Idea idea=ideas.get(j);
				idea.merge();
			}
		}
		while (ideas.size()>ideaNum); //loop until things stop happening
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
		*/
		
		ArrayList<VisualSect> sects=new ArrayList<>();
		getStart().populateSectList(sects);
		
		ArrayList<String> list0=new ArrayList<>();
		list0.add("[START]━");
		sects.add(new VisualSect(new IdeaNode(null), getStart(), list0)); //creating the node with a null ArrayList shouldn't cause any problems, but it probably will
		
		ArrayList<String> list1=new ArrayList<>();
		list1.add("━[END]");
		sects.add(new VisualSect(getEnd(), new IdeaNode(null), list1));
		
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
		public final IdeaNode strt, end;
		public final ArrayList<String> lines;
		
		VisualSect(IdeaNode s, IdeaNode e, ArrayList<String> l)
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
			//lines.add("━["+idea.toString()+"]━");
			lines.add("━"+idea.toString()+"━");
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
		
		checkInternal(new ArrayList<IdeaNode>());
	}
	
	//called by checkStructure, recursively looks through the list and returns the end node to make sure the list collapses down into a single end node
	private IdeaNode checkInternal(ArrayList<IdeaNode> nodes)
	{
		IdeaNode end=getEnd();
		
		for (IdeaNode node : nodes)
		{
			if (node==this)
			{
				WidapMind.errorMsg("loop detected in node data structure. THIS IS VERY BAD!!!");
				return end;
			}
		}
		
		nodes.add(this);
		
		for (Idea idea : prev)
		{
			if (idea.next!=this)
			{
				WidapMind.errorMsg("idea "+idea+" pointed backward at by node does not point forward to the same node");
			}
		}
		
		for (Idea idea : next)
		{
			if (idea.prev!=this)
			{
				WidapMind.errorMsg("idea "+idea+" pointed to by node does not point back at the same node");
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
