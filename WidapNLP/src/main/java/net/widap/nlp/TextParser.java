package net.widap.nlp;

import java.util.ArrayList;

public class TextParser
{
	WidapMind mind;
	WordList confirmWords, yesWords, trueWords, falseWords, noWords, thatsWords, idkWords;
	WordTree dict;
	
	TextParser(WidapMind myWidapMind)
	{
		mind=myWidapMind;
		
		dict=myWidapMind.dict;
		if (dict==null)
			WidapMind.errorMsg("dict null what TextParser was inited");
		
		confirmWords=new WordList();
		confirmWords.add("ok", 12);
		confirmWords.add("I see", 8);
		confirmWords.add("yes", 5);
		confirmWords.add("yup", 3);
		confirmWords.add("mm-hmm", 1);
		confirmWords.add("oh", 2);
		confirmWords.add("gotcha", 2);
		confirmWords.add("kk", 1);
		
		yesWords=new WordList();
		yesWords.add("yes", 12);
		yesWords.add("yup", 3);
		yesWords.add("affirmative", 2);
		
		trueWords=new WordList();
		trueWords.add("true", 6);
		trueWords.add("right", 4);
		trueWords.add("correct", 2);
		
		noWords=new WordList();
		noWords.add("no", 12);
		noWords.add("nope", 3);
		noWords.add("negative", 2);
		
		falseWords=new WordList();
		falseWords.add("false", 6);
		falseWords.add("wrong", 4);
		falseWords.add("incorrect", 2);
		
		thatsWords=new WordList();
		thatsWords.add("that's", 3);
		thatsWords.add("that is", 2);
		
		idkWords=new WordList();
		idkWords.add("I don't know", 10);
		idkWords.add("I'm not sure", 4);
		idkWords.add("I am unsure", 2);
		idkWords.add("idk", 1);
	}
	
	public String parse(String in)
	{
		String out="";
		ArrayList<String> sects=splitIntoSections(in);
		for (String str : sects)
		{
			out+=parseSection(str)+" ";
		}
		return out;
	}
	
	public ArrayList<String> splitIntoSections(String input)
	{
		ArrayList<String> list=new ArrayList<>();
		char[] stopSym={'.', ';', '?', '!'};
		
		int i0=0, i1;
		
		for (i1=0; i1<=input.length(); i1++)
		{
			for (char c : stopSym)
			{
				if (i1>i0 && (i1==input.length() || input.charAt(i1)==c))
				{
					String sub=input.substring(i0, i1);
					while (sub.startsWith(" "))
						sub=sub.substring(1);
					while (sub.endsWith(" "))
						sub=sub.substring(0, sub.length()-1);
					if (sub.length()>0)
						list.add(sub);
					i0=i1+1;
				}
			}
		}
		
		return list;
	}
	
	//parses one section (sentence) using sub method calls
	private String parseSection(String input)
	{
		String out=parseExplicitCommands(input);
		
		if (out!=null)
			return out;
		
		Idea idea=parseIdeas(str2Ideas(input));
		
		if (idea==null)
		{
			out="[parseIdeas() failed to parse '" + input + "']";
		}
		else
		{
			out=idea.toString();
		}
		
		return out;
	}
	
	//parses explicitly programmed commands, such as quit, and takes the appropriate action
	//returns null if it doesn't find any commands
	private String parseExplicitCommands(String input)
	{
		String output="";
		ArrayList<String> words=new ArrayList<>();
		
		input=input.toLowerCase();
		
		//output+="input: '"+input+"'\n";
		
		if (input.equals("quit"))
		{
			output="goodbye";
			mind.setQuit(true);
		}
		else if (chkPtrn(input, "[w]", words))
		{
			Word word;
			
			word=mind.dict.getWord(words.get(0));
			
			if (word==null)
				output+="I don't know that word.";
			else
			{
				output+="I know that word!\n";
				
				for (int i=0; i<word.variants.size(); i++)
				{
					output+="\n"+word.variants.get(i).txt+" ("+word.variants.get(i).pos.name()+")";
				}
			}
		}
		else if (chkPtrn(input, "[w] are [w]", words))
		{
			Thing thing, type;
			String name, typeName;
			
			name=dict.switchPOS(words.get(0), Word.POS.NOUN);
			
			ArrayList<Thing> things=mind.getThings(name);
			
			if (things.size()>0)
				thing=things.get(0);
			else
			{
				thing=new Thing(name);
				mind.addThing(thing);
			}
			
			typeName=dict.switchPOS(words.get(1), Word.POS.NOUN);
			
			ArrayList<Thing> types=mind.getThings(typeName);
			
			if (types.size()>0)
			{
				type=types.get(0);
			}
			else if (!typeName.equals(words.get(1)) && dict.getVariants(typeName, Word.POS.NOUN).size()>0)
			{
				type=new Thing(typeName);
				mind.addThing(type);
			}
			else
				type=null;
			
			if (type!=null)
				thing.addProp(new Prop.Type(type));
			else
				thing.addProp(new Prop.Attrib("attrib", words.get(1)));
			
			output+=confirmWords+".";
		}
		else if (chkPtrn(input, "a [w] is a [w]", words))
		{
			Thing thing, type;
			String name, typeName;
			
			name=words.get(0);
			
			ArrayList<Thing> things=mind.getThings(name);
			
			if (things.size()>0)
				thing=things.get(0);
			else
			{
				thing=new Thing(name);
				mind.addThing(thing);
			}
			
			typeName=words.get(1);
			
			ArrayList<Thing> types=mind.getThings(typeName);
			
			if (types.size()>0)
			{
				type=types.get(0);
			}
			else
			{
				type=new Thing(typeName);
				mind.addThing(type);
			}
			
			thing.addProp(new Prop.Type(type));
			
			output+=confirmWords+".";
		}
		else if (chkPtrn(input, "are [w] [w]", words))
		{
			boolean ans=false;
			
			if (words.size()!=2)
				WidapMind.errorMsg(words.size()+" words!!!");
			
			ArrayList<Thing> things=mind.getThings("name", dict.switchPOS(words.get(0), Word.POS.NOUN));
			
			for (Thing thing : things)
			{
				for (Prop props : thing.props)
				{
					if (props.str().equals(words.get(1)))
						ans=true;
				}
			}
			
			if (things.size()==0)
			{
				output+=idkWords+" if "+words.get(0)+" are "+words.get(1)+".";
			}
			else if (ans)
			{
				output+=yesWords;
				
				if (mind.rand.nextInt(2)>0)
				{
					output+=", "+thatsWords+" "+trueWords+".";
				}
				else
				{
					output+='.';
				}
			}
			else
			{
				output+=noWords;
				
				if (mind.rand.nextInt(2)>0)
				{
					output+=", "+thatsWords+" "+falseWords+".";
				}
				else
				{
					output+='.';
				}
			}
		}
		else if (chkPtrn(input, "what is [w]", words) || chkPtrn(input, "what is a [w]", words))
		{
			ArrayList<Thing> things=mind.getThings(words.get(0));
			ArrayList<String> strs=new ArrayList<>();
			
			for (Thing thing : things)
			{
				ArrayList<Thing> types=thing.getTypes();
				
				for (Thing type : types)
				{
					strs.add(type.getName());
				}
			}
			
			if (things.size()==0)
				output+="I don't know about "+words.get(0)+".";
			else if (strs.size()==0)
				output+="I've heard of "+words.get(0)+" but I don't know what it is.";
			else
				output+=words.get(0)+" is "+combineList(strs)+".";
		}
		else if (chkPtrn(input, "what are [w]", words))
		{
			ArrayList<Thing> things=mind.getThings(dict.switchPOS(words.get(0), Word.POS.NOUN));
			ArrayList<String> strs=new ArrayList<>();
			
			for (Thing thing : things)
			{
				ArrayList<Thing> types=thing.getTypes();
				
				for (Thing type : types)
				{
					strs.add(dict.switchPOS(type.getName(), Word.POS.NOUN_PL));
				}
			}
			
			if (things.size()==0)
				output+="I don't know about "+words.get(0)+".";
			else if (strs.size()==0)
				output+="I've heard of "+words.get(0)+" but I don't know what they are.";
			else
				output+=words.get(0)+" are "+combineList(strs)+".";
		}
		else if (chkPtrn(input, "tell me about [w]", words) || chkPtrn(input, "what do you know about [w]", words))
		{
			ArrayList<Thing> things;
			
			things=mind.getThings(words.get(0));
			
			if (things.size()==0)
			{
				things=mind.getThings(dict.switchPOS(words.get(0), Word.POS.NOUN));
			}
			
			if (things.size()==0)
			{
				output+="I don't know anything about "+words.get(0)+".";
			}
			else
			{
				ArrayList<String> strs=new ArrayList<>();
				
				for (Thing thing : things)
				{
					for (Prop prop : thing.props)
					{
						if (prop.getClass().equals(Prop.Name.class))
						{
							
						}
						else if (prop.getClass().equals(Prop.Type.class))
						{
							strs.add("they are types of "+dict.switchPOS(prop.str(), Word.POS.NOUN_PL));
						}
						else if (prop.getClass().equals(Prop.Attrib.class))
						{
							strs.add("they are "+prop.str());
						}
						else
						{
							strs.add("they are "+prop.str());
						}
					}
				}
				
				output+=combineList(strs)+".";
			}
		}
		
		if (output.equals(""))
		{
			return null;
			//output="[no output for "+input+"]";
		}
		
		return output;
	}
	
	private Idea parseIdeas(Node start)
	{
		
		
		return null;
	}
	
	private Node str2Ideas(String inStr)
	{
		Node start=new Node(), end=start;
		
		int i=0;
		while (i<inStr.length())
		{
			int j=i;
			i=inStr.indexOf(' ', j);
			if (i==-1)
				i=inStr.length();
			
			String str=inStr.substring(j, i);
			
			//check for coma at the end
			//even though it doesn't look like it, everything gets put in the right order
			if (str.endsWith(","))
			{
				str=str.substring(0, str.length()-1);
				new Idea(",", end, end);
			}
			
			new Idea(str, end, end);
			
			end=end.getEnd();
			
			i++; //skip the space
		}
		
		return start;
	}
	
	public static boolean chkPtrn(String input, String pattern, ArrayList<String> words)
	{
		String tag;
		int p0, p1=0, i0, i1=0;
		
		pattern=pattern.toLowerCase();
		input=input.toLowerCase();
		
		words.clear(); //make sure there aren't any words left over
		
		while (true)
		{
			p0=pattern.indexOf("[", p1);
			
			if (p0==-1)
				return pattern.substring(p1).equals(input.substring(i1));
			else
			{
				i0=i1+p0-p1;
				if (i0>input.length() || !pattern.substring(p1, p0).equals(input.substring(i1, i0)))
					return false;
				
				p1=pattern.indexOf("]", p0)+1;
				if (p1==-1)
				{
					WidapMind.errorMsg("in chkPtrn(), no closing bracket in '"+pattern+"'.");
					return false;
				}
				
				tag=pattern.substring(p0+1, p1-1);
				
				if (tag.equals("w"))
				{
					//search for a non-character
					
					i1=i0;
					
					while (i1<input.length() && input.charAt(i1)>='a' && input.charAt(i1)<='z')
						i1++;
					
					words.add(input.substring(i0, i1));
				}
				else
				{
					WidapMind.errorMsg("in chkPtrn(), Unrecognised tag: '"+tag+"' in '"+pattern+"'.");
					return false;
				}
			}
		}
	}
	
	//combines a list of strings, basically it just inserts commas and shit
	public static String combineList(ArrayList<String> list)
	{
		String out="";
		
		for (int i=0; i<list.size(); i++)
		{
			out+=list.get(i);
			if (i<list.size()-2)
				out+=", ";
			else if (i==list.size()-2)
				out+=" and ";
		}
		
		return out;
	}
	
	static class Node //nodes connect ideas in a complex data structure
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
				}
				
				return end;
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
	
	static class Idea //an idea that can be anything from an unrecognised word to a thing with attributes and actions
	{
		Node next, prev;
		
		String str;
		Word.Variant variant;
		boolean plural;
		ArrayList<Thing> things;
		
		//if either is null, the other is only used to find the correct data structure, which node it is doesn't matter
		//if n0 is null, inserts this idea at the very beginning and creates new 2nd node to come after it that contains all the pointers the old beginning node had
			//old beginning node pointers stay valid and still point to the first node
		//if n1 is null, inserts this idea at the very end and creates a new 2nd-to-last node which contains the same previous pointers as the old end node
			//old end node pointers stay valid and still point to the last node,
			//unless there was originally only one node in the whole structure, in which case that becomes the first
		//if neither is null and they are both different, this is inserted between them without creating any nodes
		//if they are the same and not null, that node is split (the original becomes the first of the two) and this is inserted
		//if both are null, and error is thrown
		public Idea(Node n0, Node n1)
		{
			things=new ArrayList<>();
			variant=null;
			plural=false;
			str=null;
			
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
		
		public Idea(String inStr, Node n0, Node n1)
		{
			this(n0, n1);
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
		
		public String toString()
		{
			String out="[no data in Idea]";
			if (things.size()>0)
			{
				ArrayList<String> list=new ArrayList<>();
				for (Thing thing : things)
				{
					list.add(thing.toString());
				}
				out=combineList(list);
			}
			else if (variant!=null)
			{
				out=variant.toString();
			}
			else if (str!=null)
			{
				out=str;
			}
			
			return out;
		}
	}
	
	public class WordList //a list of words that all mean the same thing, random picking of them will be implemented later
	{
		ArrayList<String> words;
		ArrayList<Integer> loc;
		
		public WordList()
		{
			words=new ArrayList<>();
			loc=new ArrayList<>();
		}
		
		public void add(String word)
		{
			add(word, 1);
		}
		
		public void add(String word, int num)
		{
			words.add(word);
			
			//add the number representing the position of the string a bunch of times (or just one) to the list of indexes
			for (int i=0; i<num; i++)
			{
				loc.add(words.size()-1);
			}
		}
		
		public String get()
		{
			if (words.size()>0)
				return words.get(loc.get(mind.rand.nextInt(loc.size())));
			else
				return "[no words in word list]";
		}
		
		public String toString()
		{
			return get();
		}
	}
}
