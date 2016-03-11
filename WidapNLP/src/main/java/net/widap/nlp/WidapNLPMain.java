package net.widap.nlp;

import java.util.Scanner;

public class WidapNLPMain
{
	
	static boolean runTerm=true; //if to run the interactive terminal, quits after init otherwise
	
	public static void main(String args[])
	{
		WidapMind mind=new WidapMind();
		
		Scanner reader=new Scanner(System.in);
		String input, output="";
		
		new DictLoaderFormat1("assets/azdictionary.txt", mind.dict);
		
		System.out.println("\n[WidapMind started]");
		
		if (runTerm)
		{
			System.out.println("\nWidapMind: Hello");
			
			while (!mind.getQuit())
			{
				System.out.print("\nUser: ");
				
				input=reader.nextLine();
				
				output=mind.parse(input);
				
				System.out.print("\nWidapMind: "+output);
			}
		}
		
		System.out.println("\n[WidapMind quit]");
	}
}
