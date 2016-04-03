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
		for (int i=0; i<sects.size(); i++)
		{
			String str=sects.get(i);
			out+=parseSection(str);
			if (i<sects.size()-1)
				out+=" ";
		}
		
		if (!out.endsWith("\n"))
			out+="\n";
		
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
		else out="";
		
		IdeaNode start=str2Ideas(input);
		//Idea.Node nodeStrt=new Idea.Node();
		
		Idea idea=findIdea(start);
		
		//out=nodeStrt.toStringList();
		out+=start.toStringVisual2();
		//out+=start.toStringVisual0();
		
		if (idea==null)
			out+="could not resolve input into a single idea\n";
		else if (idea.thing==null)
		{
			out+="idea that was selected ("+idea+") had no thing\n";
		}
		else
		{
			out+="sentence processed successfully, adding to memory...\n";
			Thing thing=mind.addThing(idea.thing);
			thing.addProps(idea.props);
		}
		
		return out;
	}
	
	//returns the best Idea from the node structure
	public static Idea findIdea(IdeaNode start)
	{
		if (WidapMind.lotsOfChecks)
			start.checkStructure();
		
		parseIdeas(start);
		
		if (WidapMind.lotsOfChecks)
			start.checkStructure();
		
		ArrayList<Idea> ideas=start.getSingleIdeas();
		
		if (ideas.size()==0)
			return null;
		else if (ideas.size()==1)
			return ideas.get(0);
		else
		{
			for (Idea idea0 : ideas)
			{
				if (idea0.thing!=null)
				{
					boolean containsAll=true;
					
					for (Idea idea1 : ideas)
					{
						if (idea0!=idea1 && !idea0.contains(idea1))
						{
							containsAll=false;
							break;
						}
					}
					
					if (containsAll)
					{
						return idea0;
					}
				}
			}
			
			for (Idea idea0 : ideas)
			{
				if (idea0.thing!=null)
				{
					WidapMind.errorMsg("was unable to determine the best idea, so choosing the first one that has a thing");
					return idea0;
				}
			}
			
			WidapMind.errorMsg("was unable to find an idea with a thing, so choosing the first one");
			return ideas.get(0);
		}
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
		else if (input.equals("what does a tie fighter look like"))
		{
			output+="\n┏┓        ┏━━━━━━━┓        ┏┓\n"+
					"┃┃     ┏━━┛       ┗━━┓     ┃┃\n"+
					"┃┃    ┏┛   ┏━━━━━┓   ┗┓    ┃┃\n"+
					"┃┃    ┃  ┏━┛     ┗━┓  ┃    ┃┃\n"+
					"┃┣━━━━┫  ┃         ┃  ┣━━━━┫┃\n"+
					"┃┣━━━━┫  ┗━┓     ┏━┛  ┣━━━━┫┃\n"+
					"┃┃    ┃    ┗━━━━━┛    ┃    ┃┃\n"+
					"┃┃    ┗┓    ┏┓ ┏┓    ┏┛    ┃┃\n"+
					"┃┃     ┗━━┓ ┗┻━┻┛ ┏━━┛     ┃┃\n"+
					"┗┛        ┗━━━━━━━┛        ┗┛";
		}
		else if (
				input.equals("what is the answer") ||
						input.equals("what is the answer to life, the universe and everything") ||
						input.equals("what is the meaning of life"))
		{
			output+="42";
		}
		else if (chkPtrn(input, "[w]", words))
		{
			Word word;
			
			word=mind.dict.getWord(words.get(0), true);
			
			if (word==null)
				output+="I don't know that word.";
			else
			{
				output+="I know the word '"+words.get(0)+"':\n\n";
				
				for (int i=0; i<word.variants.size(); i++)
				{
					output+=word.variants.get(i)+"\n";
				}
			}
		}
		/*else if (chkPtrn(input, "[w] are [w]", words))
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
		}*/
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
						else if (prop.getClass().equals(Prop.StrProp.class))
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
		else if (input.equals("list things"))
		{
			Thing thing=mind.thingStrt;
			int i=0, max=120;
			
			output+="listing up to "+max+" things...\n";
			
			while (thing!=null && i<max)
			{
				output+=thing+"\n";
				
				thing=thing.nxtThing;
				i++;
			}
			
			if (thing==null)
				output+="thing list finished";
			else
				output+="there are more things that weren't displayed";
		}
		
		if (output.equals(""))
		{
			return null;
			//output="[no output for "+input+"]";
		}
		
		return output;
	}
	
	private static void parseIdeas(IdeaNode start)
	{
		start.splitAll();
		start.mergeAll();
	}
	
	private IdeaNode str2Ideas(String inStr)
	{
		IdeaNode start=new IdeaNode(new ArrayList<Idea>()), end=start;
		
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
				new Idea(end, end, ",", mind);
			}
			
			new Idea(end, end, str, mind);
			
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
