package net.widap.nlp;

import java.util.ArrayList;

public class WordTree
{
	private String prefix;
	private VariantHolder listStrt=null;
	private int listLng=0, wordsBelow=0; //wordsBelow is the number of words in lower levels of the tree
	private boolean allWordsSame=true; //if all the words in the list are the same, default is true if there are 0 or 1 words in list
	private boolean hasSplit=false;
	private WordTree[] letters=null;
	
	static final int variantsBeforeSplit=12;
	static final boolean reportErrors=true;
	
	WordTree()
	{
		this("");
	}
	
	WordTree(String newPrefix)
	{
		if (newPrefix==null)
			prefix="";
		else
			prefix=newPrefix;
	}
	
	public void addVariant(String txt, Word.POS pos)
	{
		addVariant(new Word.Variant(convertToLetters(txt), pos));
	}
	
	public boolean addVariant(String txt, Word.POS pos, String match)
	{ //adds a variant to the word that contains the match, it also smartly merges words when needed
		
		return addVariant(new Word.Variant(convertToLetters(txt), pos), match);
	}
	
	public void addVariant(Word.Variant variant)
	{
		if (WidapMind.lotsOfChecks)
		{
			if (!prefix.equals(""))
				WidapMind.errorMsg("called WordTree.addVariant() on a lower level of the tree");
		}
		
		Word word=getWord(variant.txt);
		
		boolean addVariant;
		
		if (word==null)
		{
			word=new Word();
			word.add(variant);
			addVariant=true;
		}
		else
		{
			addVariant=word.add(variant);
		}
		
		if (addVariant)
		{
			VariantHolder holder=new VariantHolder();
			holder.variant=variant;
			addHolder(holder);
		}
	}
	
	//adds the variant to the word that contains a variant with the same string as match
	//returns true if it worked
	public boolean addVariant(Word.Variant variant, String match)
	{
		if (WidapMind.lotsOfChecks)
		{
			if (!prefix.equals(""))
				WidapMind.errorMsg("called WordTree.addVariant() on a lower level of the tree");
		}
		
		Word matchWord=getWord(match);
		Word word=getWord(variant.txt);
		
		
		if (matchWord==null)
		{
			//unable to find word
			return false;
		}
		
		if (word==matchWord)
			word=null;
		
		if (word!=null)
		{
			mergeWords(matchWord, word);
		}
		
		if (matchWord.add(variant))
		{
			VariantHolder holder=new VariantHolder();
			holder.variant=variant;
			addHolder(holder);
		}
		
		return true;
	}
	
	private void addHolder(VariantHolder holder)
	{
		if (hasSplit && holder.variant.txt.length()>prefix.length())
		{
			char c=holder.variant.txt.charAt(prefix.length());
			
			if (c<'a' || c>'z')
			{
				if (reportErrors)
					WidapMind.errorMsg("bad character '"+c+"' (not lower case letter) in variant '"+holder.variant.txt+"' found in WordTree.addHolderToTree().");
				
				return;
			}
			
			int loc=c-'a';
			
			if (letters[loc]==null)
				letters[loc]=new WordTree(prefix+c);
			
			letters[loc].addHolder(holder);
			
			wordsBelow++;
		}
		else
		{
			holder.nxt=listStrt;
			listStrt=holder;
			listLng++;
			
			if (!hasSplit)
			{
				if (allWordsSame && holder.nxt!=null && !holder.variant.txt.equals(holder.nxt.variant.txt))
					allWordsSame=false;
				
				if (listLng>variantsBeforeSplit && !allWordsSame)
					splitIntoLetters();
			}
		}
	}
	
	private String convertToLetters(String input) //converts a string to all lowercase letters, removes all other characters
	{
		input=input.toLowerCase();
		
		for (int i=0; i<input.length(); i++)
		{
			char c;
			while (i<input.length() && ((c=input.charAt(i))<'a' || c>'z'))
			{
				input=(i<=0? "" : input.substring(0, i))+(i>=input.length()? "" : input.substring(i+1));
			}
		}
		
		return input;
	}
	
	private void splitIntoLetters()
	{
		if (allWordsSame || letters!=null)
		{
			if (reportErrors)
				WidapMind.errorMsg("something went wrong in WordTree.splitIntoLetters()");
			return;
		}
		
		letters=new WordTree[26];
		
		VariantHolder holder=listStrt, holderNxt;
		
		listStrt=null;
		listLng=0;
		allWordsSame=true;
		hasSplit=true;
		
		while (holder!=null)
		{
			holderNxt=holder.nxt; //we need to do this so if this holder is reassigned to a lower level, we stay on track
			
			addHolder(holder);
			
			holder=holderNxt;
		}
	}
	
	//returns the word that contains a variant of the input, or null if no such word exists
	//relies on getVariant()
	//assumes that there is only one word with variants that have any one string
	public Word getWord(String input)
	{
		//getVariant() does this for us
		//input=convertToLetters(input);
		
		ArrayList<Word.Variant> variants=getVariants(input);
		
		//getVariantInternal(input, variants);
		
		if (variants.size()==0)
			return null;
		else
			return variants.get(0).word; //we can just return the word of the first variant because we assume that all variants with the same text belong to the same word
	}
	
	//returns a string containing the version of the input word with a given part of speech
	//has several things to try, and always returns something, even if its just the original word
	public String switchPOS(String input, Word.POS pos)
	{
		Word word=getWord(input);
		ArrayList<Word.Variant> list;
		
		//first we check if the dictionary has the variant we want. If it has multiple, we just return the first one
		if (word!=null && (list=word.getVariants(pos)).size()>0)
			return list.get(0).txt;
			
			//assume we are dealing with an input noun singular
		else if (pos==Word.POS.NOUN_PL)
		{
			return makePlural(input);
		}
		
		else if (pos==Word.POS.NOUN)
		{
			return makeSingular(input);
		}
		
		else if (pos==Word.POS.VB_PR)
		{
			return input+"ing";
		}
		
		//if all else fails, return the word back unchanged
		else
			return input;
	}
	
	public String makePlural(String input)
	{
		ArrayList<Word.Variant> variants=getVariants(input, Word.POS.NOUN_PL);
		
		if (variants.size()>0)
			return variants.get(0).txt;
		
		else if (input.endsWith("y"))
			return input.substring(0, input.length()-1)+"ies";
		
		else if (input.endsWith("s"))
			return input+"es";
		
		else
			return input+"s";
	}
	
	public String makeSingular(String input)
	{
		ArrayList<Word.Variant> variants=getVariants(input, Word.POS.NOUN);
		
		if (variants.size()>0)
			return variants.get(0).txt;
		
		if (input.endsWith("ies") && input.length()>3)
			return input.substring(0, input.length()-3)+"y";
		
		else if (input.endsWith("ses") && input.length()>2)
			return input.substring(0, input.length()-2);
		
		else if (input.endsWith("s") && input.length()>1)
			return input.substring(0, input.length()-1);
		
		else
			return input;
	}
	
	//returns the variants of the input word with a specific part of speech
	//if it cant find the input word, or the correct variant within, it will return null
	public ArrayList<Word.Variant> getVariants(String input, Word.POS pos)
	{
		Word word=getWord(input);
		
		if (word==null)
			return new ArrayList<>();
		else
			return word.getVariants(pos);
	}
	
	//an easier to use wrapper for getVariantInternal()
	//does things that should only be done once, not once for every layer of the tree
	public ArrayList<Word.Variant> getVariants(String inptWord)
	{
		inptWord=convertToLetters(inptWord);
		ArrayList<Word.Variant> list=new ArrayList<>();
		getVariantsInternal(inptWord, list);
		return list;
	}
	
	//compiles a list of variants that exactly match the input string;
	//input is expected to already have been converted to lower case letters and this list is expected to already be initialized
	//this function calls itself on the next level of the tree if necessary
	private void getVariantsInternal(String input, ArrayList<Word.Variant> list)
	{
		if (!hasSplit || input.length()<=prefix.length())
		{
			VariantHolder holder=listStrt;
			
			while (holder!=null)
			{
				if (holder.variant.txt.equals(input))
				{
					list.add(holder.variant);
				}
				
				holder=holder.nxt;
			}
		}
		else
		{
			char c=input.charAt(prefix.length());
			int loc=c-'a';
			if (letters[loc]!=null)
				letters[loc].getVariantsInternal(input, list);
		}
	}
	
	//merges two words, a trickier thing then you might think considering the complexity of the data structuring
	//don't mess with unless you have to
	//uses removeHolder
	private void mergeWords(Word dest, Word src)
	{
		for (int i=0; i<src.variants.size(); i++)
		{
			if (!dest.add(src.variants.get(i)))
			{
				//delete holder of variant
				removeHolder(src.variants.get(i));
			}
		}
	}
	
	//removes the holder for a variant so GC can get rid of it
	//this function calls itself on the next level of the tree if necessary
	//this function appears to never be called, which is odd but everything seems to work so I'm not investigating
	private void removeHolder(Word.Variant variant)
	{
		if (!hasSplit || variant.txt.length()<=prefix.length())
		{
			VariantHolder holder=listStrt;
			
			if (listStrt.variant==variant)
			{
				listStrt=listStrt.nxt;
				listLng--;
			}
			
			while (holder.nxt!=null)
			{
				if (holder.nxt.variant==variant)
				{
					holder.nxt=holder.nxt.nxt;
					listLng--;
					return;
				}
				
				holder=holder.nxt;
			}
			
			if (reportErrors)
				WidapMind.errorMsg("could not find holder I was trying to remove for variant '"+variant.txt+"'.");
		}
		else
		{
			char c=variant.txt.charAt(prefix.length());
			int loc=c-'a';
			if (letters[loc]!=null)
				letters[loc].removeHolder(variant);
			
			wordsBelow--;
		}
	}
	
	//simply returns the number of words
	public int getWordNum()
	{
		return listLng+wordsBelow;
	}
	
	public void check()
	{
		int i=0;
		
		if (listLng>variantsBeforeSplit && !allWordsSame)
		{
			WidapMind.errorMsg("word tree with prefix '"+prefix+"' should be split but wasn't.");
		}
		
		VariantHolder holder=listStrt;
		while (holder!=null)
		{
			if (!holder.variant.txt.startsWith(prefix))
				WidapMind.errorMsg("word '"+holder.variant.txt+"' is in the tree with the prefix '"+prefix+"'.");
			
			if (holder.variant.word==null)
				WidapMind.errorMsg("variant '"+holder.variant.txt+"' is not linked to a ward.");
			
			holder=holder.nxt;
			i++;
		}
		
		if (i!=listLng)
		{
			WidapMind.errorMsg("WordTree with prefix '"+prefix+"' thinks it has "+listLng+" variants in it's list but it actually has "+i+".");
		}
		
		if (hasSplit==(letters==null))
		{
			WidapMind.errorMsg("WordTree letters initialization does not match hasSplit bool.");
		}
		
		if (hasSplit)
		{
			int wordSum=0;
			
			for (WordTree node : letters)
			{
				if (node!=null)
				{
					node.check();
					wordSum+=node.getWordNum();
				}
			}
			
			if (wordSum!=wordsBelow)
				WidapMind.errorMsg("WordTree wordsBelow is inaccurate");
		}
	}
	
	private class VariantHolder
	{
		Word.Variant variant;
		VariantHolder nxt;
	}
}