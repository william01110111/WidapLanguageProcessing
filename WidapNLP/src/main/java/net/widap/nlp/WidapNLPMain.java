package net.widap.nlp;

import java.util.Scanner;

public class WidapNLPMain
{
	
	static boolean runTerm=true; //if to run the interactive terminal, quits after init otherwise
	static String lastPrintType=null;
	
	public static void main(String args[])
	{
		WidapMind mind=new WidapMind();
		
		Scanner reader=new Scanner(System.in);
		String input, output;
		
		new DictLoaderFormat1("assets/azdictionary.txt", mind.dict);
		
		print("[WidapMind started]\n", "status");
		
		if (runTerm)
		{
			print("WidapMind: Hello\n", "mind");
			
			while (!mind.getQuit())
			{
				print("User: ", "user");
				
				input=reader.nextLine();
				
				output=mind.parse(input);
				
				print("WidapMind: "+output, "mind");
			}
		}
		
		print("[WidapMind quit]\n", "status");
	}
	
	public static void print(String str, String type)
	{
		if (lastPrintType!=null && !type.equals(lastPrintType))
			System.out.println();
		
		lastPrintType=type;
		
		System.out.print(str);
	}
}
