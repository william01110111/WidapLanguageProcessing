package net.widap.nlp;

import java.util.ArrayList;

/**
 *  Created by william on 2/6/16.
 */
public class Word
{
    public enum POS //Parts of Speech
    {
        NOUN, //person, place, thing or type
        NOUN_PL, //plural noun
        PRON, //pronoun
        VB, //unspecified verb (action)
        VB_I, //intransitive verb, no direct object
        VB_T, //always has direct object
        VB_PS, //past participle 'eaten', 'made', found'
        VB_PR, //present participle 'eating', 'making', 'finding'
        ADJ, //describes a noun
        ADJ_SPR, //superlative; very general adjective like 'large', 'rude' and 'best'
        ADV, //adverb, describes a verb
        PREP, //preposition, links two things like 'inside' or 'during'
        INTRJ, //interjection; 'hay' 'yo'
        CONJ, //conjunction; 'neither', 'and', 'if'
        OTHER //things that are not any other part of speech, also has special uses depending on context
    }
    
    //static int POSNum=POS.values().length;

    public ArrayList<Variant> variants;

    Word()
    {
        variants=new ArrayList<>();
    }

    public boolean add(String newTxt, POS newPOS)
    {
        return add(new Variant(newTxt, newPOS, this));
    }

    public boolean add(Variant inpt)
    {
        boolean exactMatch=false;

        for (int i=0; i<variants.size() && !exactMatch; i++)
        {
            if (variants.get(i).pos.equals(inpt.pos) && variants.get(i).txt.equals(inpt.txt))
                exactMatch=true;
        }

        if (!exactMatch)
        {
            variants.add(inpt);
            inpt.word=this;
            return true;
        }
        else
        {
            //this may help the garbage collector decide what to delete. i'm not sure.
            inpt.word=null;
            return false;
        }
    }

    public void check() //checks object for internal errors
    {
        for (int i=0; i<variants.size(); ++i)
        {
            if (!variants.get(i).txt.equals(variants.get(i).txt.toLowerCase()))
                WidapMind.errorMsg("Some text upper case in a word variant");

            if (variants.get(i).word!=this)
                WidapMind.errorMsg("a variant was not assigned to the word that owns it");
        }
    }

	//returns a list of all the variants of this word with a given part of speech, returns an empty list if it can't find any
	public ArrayList<Variant> getVariants(POS pos)
	{
		ArrayList<Variant> list=new ArrayList<>();

		for (int i=0; i<variants.size(); i++)
		{
			if (variants.get(i).pos==pos)
				list.add(variants.get(i));
		}

		return list;
	}

    public ArrayList<POS> getPOS(String input)
    {
        input=input.toLowerCase();

        ArrayList<POS> ary=new ArrayList<>();
        Variant j;

        for (int i=0; i<variants.size(); i++)
        {
            j=variants.get(i);
            if (input.equals(j.txt))
                ary.add(j.pos);
        }

        return ary;
    }

    public static class Variant
    {
        public String txt;
        public POS pos;
        public Word word;

        Variant(String newTxt, POS newPos, Word newWord)
        {
            txt=newTxt.toLowerCase();
            pos=newPos;
            word=newWord;
        }

        Variant(String newTxt, POS newPos)
        {
            txt=newTxt.toLowerCase();
            pos=newPos;
            word=null;
        }

        Variant()
        {
            txt=null;
            pos=null;
            word=null;
        }

        public String toString()
        {
            return txt + " (" + pos.name() + ")";
        }
    }
}
