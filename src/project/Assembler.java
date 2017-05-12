package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Assembler 
{
	public static void assemble(File input, File output, ArrayList<String> errors)
	{	
		ArrayList<String> code = new ArrayList<>();
		ArrayList<String> data = new ArrayList<>();
		
		try (Scanner in = new Scanner(input)) 
		{
			boolean dashed = false;
			while(in.hasNextLine())
			{
				String line = in.nextLine();
				if (line.trim().startsWith("--"))
				{
					dashed = true;
					continue;
				}
				else
				{
					if(dashed == false)
					{
						code.add(line);
					}					
					else
					{
						data.add(line);
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
			int indirLvl;
			if(parts.length == 2)
			{
				indirLvl = 1;
				if(parts[1].startsWith("["))
				{
					indirLvl = 2;
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
				System.out.println(parts[0]);
				int opcode = InstructionMap.opcode.get(parts[0]);
				outText.add(Integer.toHexString(opcode).toUpperCase() + " " + indirLvl + " " + parts[1]);
			}		
			if(parts.length == 1)
			{
				int opcode = InstructionMap.opcode.get(parts[0]);
				outText.add(Integer.toHexString(opcode).toUpperCase() + " 0 0");
			}
		}	
		outText.add("-1");
		outText.addAll(data);
		
		try (PrintWriter out = new PrintWriter(output)){
			for(String s : outText) out.println(s);
		} catch (FileNotFoundException e) {
			errors.add("Cannot create output file");
		}		
	}	
	
	public static void main(String[] args) {
		ArrayList<String> errors = new ArrayList<>();
		assemble(new File("in.pasm"), new File("out.pexe"), errors);		
	}
	
}

