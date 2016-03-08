package net.widap.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DictLoaderFormat1
{
	private WordReferenceListElem wordReferenceListStrt=null; //a list of words that reference other words
	// the dictionary builds this up and then processes all of them when it has all the referenced words in
	static final boolean reportErrors=true;
	private WordTree dict;
	
	DictLoaderFormat1(String filename, WordTree newDict)
	{
		double endTime, strtTime=System.currentTimeMillis()/1000.0;

		dict=newDict;

		int wordCountAtStart=dict.getWordNum(), wordCountAtEnd;

		WidapMind.message("loading words from '"+filename+"'...");

		try
		{
			FileInputStream fileInputStream=new FileInputStream(new File(filename));
			InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream, "UTF8");
			BufferedReader in=new BufferedReader(inputStreamReader);

			String line, entry="";
			int i=0;

			in.read(); //java adds an extra space to the first line for some reason. this fixes it.

			while ((line=in.readLine())!=null && i<1000000)
			{
				if (!line.equals("")) //add this line on to the definition we are building up
				{
					if (line.charAt(2)!=' ' || line.charAt(3)==' ')
						WidapMind.errorMsg("in loadFromFile using format 1, file did not have correct indenting");

					//there are 3 spaces at the beginning of each line, we only need one of them
					entry=entry+line.substring(entry.equals("")? 3 : 2);
				}
				else //a double line break has occurred and so a definition has finished
				{
					entry=entry.toLowerCase();

					processFormat1Entry(entry);

					entry="";

					i++;
				}
			}

            /*while (elem!=null)
            {
                ArrayList<Word.Variant> variant;
                if ((variant=getWordVariant(elem.variantToMatch)).size()!=0)
                {
                    variant.get(0).word.add(elem.variant);
                    loadedVariationCount++;
                }
                elem=elem.nxt;
            }*/

			in.close();

			endTime=System.currentTimeMillis()/1000.0;
			WidapMind.message("took "+String.format("%.2f", endTime-strtTime)+"s to load "+i+" entries. processing reference list...");

			processReferenceList();
		}
		catch(Exception e)
		{
			WidapMind.errorMsg("exception in DictLoaderFormat1(): "+e.getMessage()+"; stack trace:");
			e.printStackTrace();
		}

		wordCountAtEnd=dict.getWordNum();

		WidapMind.message("checking the word tree...");
		dict.check();
		WidapMind.message("done with check.");

		endTime=System.currentTimeMillis()/1000.0;

		WidapMind.message("loaded "+(wordCountAtEnd-wordCountAtStart)+" words in "+String.format("%.2f", endTime-strtTime)+"s.");
	}
	
	private void processFormat1Entry(String entry)
	{
		try
		{
			String wordTxt, typeTxt, flagTxt, definTxt;
			int perenStrt, perenEnd; //the start and end of the parentheses

			perenStrt=entry.indexOf(" (")+1;
			perenEnd=entry.indexOf(")", perenStrt);

			if (perenStrt==-1 || perenEnd==-1 || perenStrt<2)
			{
				if (reportErrors)
					WidapMind.errorMsg("improper parentheses in dictionary entry:\n"+entry+"\n");

				return;
			}

			wordTxt=entry.substring(0, perenStrt-1);

			if (wordTxt.charAt(0)==' ' || wordTxt.endsWith(" "))
			{
				if (reportErrors)
					WidapMind.errorMsg("improper spacing in dictionary entry:\n"+entry+"\n");

				return;
			}

			typeTxt=entry.substring(perenStrt+1, perenEnd);

			if (perenEnd+1<entry.length())
				definTxt=entry.substring(perenEnd+2);
			else
				definTxt="";

			int locStrt=0, locEnd;

			while (locStrt<=typeTxt.length())
			{
				Word.POS pos;
				String txt;

				locEnd=typeTxt.indexOf(" & ", locStrt);
				if (locEnd==-1)
				{
					locEnd=typeTxt.length();

					if (locEnd>0 && typeTxt.charAt(locEnd-1)==' ')
					{
						locEnd--;
					}
				}

				flagTxt=typeTxt.substring(locStrt, locEnd);

				locStrt=locEnd+3;

				txt=wordTxt;

				pos=getPOS(flagTxt);

				ArrayList<String> wordsToMatch=findIfReference(definTxt);

				if (wordsToMatch.size()>0)
				{
					WordReferenceListElem reference=new WordReferenceListElem();
					reference.variantToMatch=wordsToMatch;
					reference.variant=new Word.Variant(txt, pos);
					reference.nxt=wordReferenceListStrt;
					wordReferenceListStrt=reference;
				}
				else
				{
					dict.addVariant(txt, pos);
				}
			}
		} catch(Exception e)
		{
			if (reportErrors)
			{
				WidapMind.errorMsg("exception ("+e.getMessage()+") in dictionary entry:\n"+entry+"\n");
				e.printStackTrace();
			}
		}
	}
	
	private Word.POS getPOS(String input)
	{
		//common

		if (input.equals("n."))
			return Word.POS.NOUN; //noun

		else if (input.equals("v."))
			return Word.POS.VB; //verb

		else if (input.equals("v. i."))
			return Word.POS.VB_I; //verb

		else if (input.equals("v. t.") || input.equals("i.")) //I think i. is imperative which is basically an order; my need to be changed
			return Word.POS.VB_T; //verb

		else if (input.equals("p. p.") || input.equals("imp.")) //there should probably be as distinction between these two but I don't know what imp. is
			return Word.POS.VB_PS; //verb

		else if (input.equals("p. pr."))
			return Word.POS.VB_PR; //verb

		else if (input.equals("a.") || input.equals("p. a.")) //p. a. is Predicate Adjective, not sure what that means so I'm lumping it together with normal adjective
			return Word.POS.ADJ; //adjective

		else if (input.equals("adv."))
			return Word.POS.ADV; //adverb

			//uncommon

		else if (input.equals(""))
			return Word.POS.OTHER;

		else if (input.equals("n") || input.equals("n. sing.") || input.equals("vb. n.")/*verbal noun*/)
			return Word.POS.NOUN; //noun

		else if (input.equals("pl.") || input.equals("n. pl.") || input.equals("n.pl."))
			return Word.POS.NOUN_PL; //plural noun

		else if (input.equals("v.i."))
			return Word.POS.VB_I; //verb

		else if (input.equals("v.t.")) //I think i. is imperative which is basically an order; my need to be changed
			return Word.POS.VB_T; //verb

		else if (input.equals("superl.") || input.equals("a. superl.") || input.equals("superl"))
			return Word.POS.ADJ_SPR; //superlative adjective

		else if (input.equals("prep.") || input.equals("pref.")) //pref is only used by 'by'
			return Word.POS.PREP; //adverb

		else if (input.equals("interj."))
			return Word.POS.INTRJ;

		else if (input.equals("conj."))
			return Word.POS.CONJ;

		else if (input.equals("pron."))
			return Word.POS.PRON;

		else if (input.equals("a") || input.equals("adj.") || input.equals("compar.")) //p. a. is Predicate Adjective, not sure what that means so I'm lumping it together with normal adjective
			return Word.POS.ADJ; //adjective

		else
		{
			//WidapMind.errorMsg(input);
			return Word.POS.OTHER;
		}
	}
	
	private ArrayList<String> findIfReference(String definTxt)
	{
		ArrayList<String> output=new ArrayList<>();
		ArrayList<String> words=new ArrayList<>();
		String start;
		int splitPoint;

		char c;
		while (definTxt.length()>1 && ((c=definTxt.charAt(definTxt.length()-1))=='.' || c==';' || c==' '))
		{
			definTxt=definTxt.substring(0, definTxt.length()-1);
		}

		splitPoint=definTxt.indexOf("; ");

		if (splitPoint==-1)
			start=definTxt;
		else
			start=definTxt.substring(0, splitPoint);

		if (
				TextParser.chkPtrn(start, "of [w]", words) ||
						//TextParser.chkPtrn(start, "[w]", output) ||
						TextParser.chkPtrn(start, "Alt. of [w]", words) ||
						TextParser.chkPtrn(start, "Compar. of [w]", words) ||
						TextParser.chkPtrn(start, "Same as [w]", words) ||
						TextParser.chkPtrn(start, "See [w]", words)
				)
		{
			output.add(words.get(0));
		}
		else if (
				TextParser.chkPtrn(start, "One who [w]", words)
				)
		{
			String str=words.get(0);

			output.add(str);

			if (str.endsWith("ies") && str.length()>3)
			{
				output.add(str.substring(0, str.length()-3)+"y");
			}

			if (str.endsWith("es") && str.length()>2)
			{
				output.add(str.substring(0, str.length()-2));
			}

			if (str.endsWith("s") && str.length()>1)
			{
				output.add(str.substring(0, str.length()-1));
			}
		}
		else if (splitPoint!=-1 && splitPoint+2<definTxt.length())
		{
			return findIfReference(definTxt.substring(splitPoint+2));
		}

		return output;
	}
	
	private void processReferenceList()
	{
		int wordsRemovedCount;

		do
		{
			wordsRemovedCount=0;

			WordReferenceListElem elem=wordReferenceListStrt;
			wordReferenceListStrt=null;

			while (elem!=null)
			{
				WordReferenceListElem nxtElem=elem.nxt;
				boolean variantAdded=false;

				for (int i=0; i<elem.variantToMatch.size() && !variantAdded; i++)
				{
					if (dict.addVariant(elem.variant.txt, elem.variant.pos, elem.variantToMatch.get(i)))
						variantAdded=true;
				}

				if (variantAdded)
				{
					wordsRemovedCount++;
				}
				else
				{
					//it didn't work, so reinsert this entry into the list to try again next time
					elem.nxt=wordReferenceListStrt;
					wordReferenceListStrt=elem;
				}

				elem=nxtElem;
			}
		} while (wordsRemovedCount>0);

        /*if (wordReferenceListStrt!=null && reportErrors)
        { //if there are still unmatched words

            WidapMind.errorMsg("Unmatched words:");

            WordReferenceListElem elem=wordReferenceListStrt;

            while (elem!=null)
            {
                WidapMind.errorMsg("\t" + elem.variant.toString() + " failed to match with " + elem.variantToMatch);

                elem=elem.nxt;
            }
        }*/
	}
	
	public class WordReferenceListElem //used to make a linked list of variants that should go with other words
	{
		Word.Variant variant;
		ArrayList<String> variantToMatch;
		WordReferenceListElem nxt;
	}
}
