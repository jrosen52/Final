package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Assembler2 
{
	public static void assemble(File input, File output, ArrayList<String> errors)
	{	
		ArrayList<String> code = new ArrayList<>();
		ArrayList<String> data = new ArrayList<>();
		ArrayList<String> inText = new ArrayList<>();
		
		try (Scanner in = new Scanner(input)) 
		{
			while(in.hasNextLine())
			{
				String line = in.nextLine();
				inText.add(line);
			}
			while(inText.get(inText.size()-1).trim().length() == 0)
			{
				inText.remove(inText.size()-1);
			}
			boolean blankLine = false;
			for(int i = 0; i < inText.size(); i++)
			{
				if(inText.get(i).trim().length() == 0)
				{
					blankLine = true;
				}
				else if(blankLine)
				{
					String error1 = "Error: line " + i + " is a blank line";
					errors.add(error1);
					blankLine = false;
				}
				if(inText.get(i).length() > 0 && (inText.get(i).charAt(0) == ' ' || inText.get(i).charAt(0) == '\t'))
				{
					String error2 = "Error: line " + (i+1) + " starts with white space";
					errors.add(error2);
				}
				int sep = 0;
				if(inText.get(i).trim().toUpperCase().startsWith("--"))
				{	
					sep++;
					if(sep > 1)
					{
						String error3 = "Error: line " + i + " has a duplicate data separator";
						errors.add(error3);
					}
					else if(inText.get(i).trim().replace("-","").length()!=0)
					{
						String error3 = "Error: line " + i + " has a bad data separator";
						errors.add(error3);
					}
				}				
			}
			boolean dashed = false;
			for(int i = 0; i < inText.size(); i++)
			{
				if (inText.get(i).trim().startsWith("--"))
				{
					dashed = true;
					continue;
				}
				else if(inText.get(i).trim().length() > 0)
				{
					if(dashed == false)
					{
						code.add(inText.get(i));
					}					
					else
					{
						data.add(inText.get(i));
					}
				}
				
			}
		}
		catch (FileNotFoundException e) {
			errors.add("Input file does not exist");
				return;
		}
		
		ArrayList<String> outText = new ArrayList<>();
		for(String line: code)
		{
			String[] parts = line.trim().split("\\s+");
			if(InstructionMap.noArgument.contains(parts[0]) && parts.length != 1)
			{
				String error2 = "Error: line " + (inText.indexOf(line)+1) + " this mnemonic cannot take arguments";
				errors.add(error2);
			}
			if(!InstructionMap.noArgument.contains(parts[0]) && parts.length == 1 && InstructionMap.opcode.containsKey(parts[0]))
			{
				String error3 = "Error: line " + (inText.indexOf(line)+1) + " this mnemonic is missing an argument";
				errors.add(error3);
			}
			if(parts.length >= 3)
			{
				String error4 = "Error: line " + (inText.indexOf(line)+1) + " this mnemonic has more than one argument";
				errors.add(error4);
			}
			int indirLvl;
			if(parts.length == 2)
			{
				indirLvl = 1;
				if(parts[1].startsWith("["))
				{
					indirLvl = 2;
					if(!InstructionMap.indirectOK.contains(parts[0]))
					{
						String error5 = "Error: line " + (inText.indexOf(line)+1) + " has an illegal argument";
						errors.add(error5);
					}
					if(InstructionMap.indirectOK.contains(parts[0]) && !parts[1].endsWith("]"))
					{
						String error6 = "Error: line " + (inText.indexOf(line)+1) + " does not end with ]";
						errors.add(error6);
					}
					parts[1] = parts[1].substring(1, parts[1].length()-1);
				}
				if(parts[0].endsWith("I"))
				{
					indirLvl = 0;
				}
				if(parts[0].endsWith("A"))
				{
					indirLvl = 3;
				}
				int arg = 0; 
				if(InstructionMap.sourceCodes.contains(parts[0].toUpperCase()) && !InstructionMap.sourceCodes.contains(parts[0]))
				{
					String error1 = "Error: line " + (inText.indexOf(line)+1) + " does not have the instruction mnemonic in upper case";
					errors.add(error1);
				}
				else
				{
					try {
						int opcode = InstructionMap.opcode.get(parts[0]);
						outText.add(Integer.toHexString(opcode).toUpperCase() + " " + indirLvl + " " + parts[1]);
					}
					catch (NullPointerException e) {
						errors.add("Error: line " + (inText.indexOf(line)+1) + " has an illegal mnemonic");
					}
					System.out.println(parts[0]);
					try {
						arg = Integer.parseInt(parts[1],16);
					} catch (NumberFormatException e) {
						errors.add("Error: line " + (inText.indexOf(line)+1) + " argument is not a hex number");
					}
				}
			}		
			if(parts.length == 1)
			{
				try
				{
					System.out.println(parts[0]);
					int opcode = InstructionMap.opcode.get(parts[0]);
					outText.add(Integer.toHexString(opcode).toUpperCase() + " 0 0");
				}
				catch (NullPointerException e) {
					errors.add("Error: line " + (inText.indexOf(line)+1) + " has an illegal mnemonic");
				}
			}
		}	
		for(String line: data)
		{
			String[] parts = line.trim().split("\\s+");
			if(parts.length != 2)
			{
				errors.add("Error: line " + (inText.indexOf(line)+1) + " data format does not consist of two numbers");
			}	
			
			int arg = 0;
			try {
				arg = Integer.parseInt(parts[0],16);
			} catch (NumberFormatException e) {
				errors.add("Error: line " + (inText.indexOf(line)+1) + " data address is not a hex number");
			}
			if(parts.length == 2)
			{
				try {
					arg = Integer.parseInt(parts[1],16);
				} catch (NumberFormatException e) {
					errors.add("Error: line " + (inText.indexOf(line)+1) + " data value is not a hex number");
				}
			}
		}
		outText.add("-1");
		outText.addAll(data);
		
		if(errors.size() <= 0)
		{
			try (PrintWriter out = new PrintWriter(output)){
				for(String s : outText) out.println(s);
			} catch (FileNotFoundException e) {
				errors.add("Cannot create output file");
			}	
		}
	}	
}

