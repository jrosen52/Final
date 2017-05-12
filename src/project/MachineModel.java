package project;

import java.util.Map;
import java.util.TreeMap;

public class MachineModel 
{
	private CPU cpu = new CPU();
	private Memory memory = new Memory();
	private HaltCallback callback;
	public Map<Integer, Instruction> IMAP = new TreeMap<>();
	private Code code = new Code();
	private Job currentJob;
	
	Job[] jobs = new Job[4];
	
	public Job getCurrentJob() {
		return currentJob;
	}
	
	public void changeToJob(int i)
	{
		if(i < 0 || i > 3)
		{
			throw new IllegalArgumentException("Bad code");
		}
		else if(i != getCurrentJob().getId())
		{
			getCurrentJob().setCurrentPC(cpu.getpCounter());
			getCurrentJob().setCurrentAcc(cpu.getAccum());
			currentJob = jobs[i];
			cpu.setAccum(getCurrentJob().getCurrentAcc());
			cpu.setpCounter(getCurrentJob().getCurrentPC());
			cpu.setMemBase(getCurrentJob().getStartmemoryIndex());			
		}
		
		
	}
	
	public void setCode(int index, int op, int indirLvl, int arg)
	{
		code.setCode(index, op, indirLvl, arg);
	}
	
	public Code getCode()
	{
		return code;
	}
	
	public MachineModel() 
	{
		this(() -> System.exit(0));
	}
	
	public MachineModel(HaltCallback callback) 
	{
		this.callback = callback;
		
		//INSTRUCTION MAP entry for "ADD"
		IMAP.put(0x3, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in ADD instruction");
			}
			if(level > 0) {
				IMAP.get(0x3).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				cpu.setAccum(cpu.getAccum() + arg);
				cpu.incrPC();
			}
		});
		
		//INSTRUCTION MAP entry for "SUB"
		IMAP.put(0x4, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in SUB instruction");
			}
			if(level > 0) {
				IMAP.get(0x4).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				cpu.setAccum(cpu.getAccum() - arg);
				cpu.incrPC();
			}
		});	
		
		//INSTRUCTION MAP entry for "MUL"
		IMAP.put(0x5, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in MUL instruction");
			}
			if(level > 0) {
				IMAP.get(0x5).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				cpu.setAccum(cpu.getAccum() * arg);
				cpu.incrPC();
			}
		});	
		
		//INSTRUCTION MAP entry for "DIV"
		IMAP.put(0x6, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in DIV instruction");
			}
			if(level > 0) {
				IMAP.get(0x6).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				if(arg == 0)
				{
				throw new DivideByZeroException(
						"Cannot divide by zero");
				}
				else
				{
					cpu.setAccum(cpu.getAccum() / arg);
					cpu.incrPC();
				}
			}
		});	
		
		//INSTRUCTION MAP entry for "NOP"
		IMAP.put(0x0, (arg, level) -> {
				cpu.incrPC();
		});
	
		//INSTRUCTION MAP entry for "LOD"
		IMAP.put(0x1, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in LOD instruction");
			}
			if(level > 0) {
				IMAP.get(0x1).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				cpu.setAccum(arg);
				cpu.incrPC();
			}
		});
		
		//INSTRUCTION MAP entry for "STO"
		IMAP.put(0x2, (arg, level) -> {
			if(level < 1 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in STO instruction");
			}
			if(level > 1) {
				IMAP.get(0x2).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				memory.setData(cpu.getMemBase()+arg, cpu.getAccum());
				cpu.incrPC();
			}
		});
		
		//INSTRUCTION MAP entry for "AND"
		IMAP.put(0x7, (arg, level) -> {
			if(level < 0 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in AND instruction");
			}
			if(level > 0) {
				IMAP.get(0x7).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				if(cpu.getAccum() != 0 && arg != 0)
				{
					cpu.setAccum(1);
				}
				else
				{
					cpu.setAccum(0);
				}
				cpu.incrPC();
			}
		});
		
		//INSTRUCTION MAP entry for "NOT"
		IMAP.put(0x8, (arg, level) -> {
				if(level != 0)
				{
					throw new IllegalArgumentException(
							"level must be 0");
				}
				if(cpu.getAccum() != 0)
				{
					cpu.setAccum(0);
				}
				else
				{
					cpu.setAccum(1);
				}
				cpu.incrPC();
		});
		
		//INSTRUCTION MAP entry for "CMPL"
		IMAP.put(0x9, (arg, level) -> {
			if(level < 1 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in ADD instruction");
			}
			if(level > 1) {
				IMAP.get(0x9).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				if(memory.getData(cpu.getMemBase()+arg) < 0)
				{
					cpu.setAccum(1);
				}
				else
				{
					cpu.setAccum(0);
				}
				cpu.incrPC();
			}
		});
		
		//INSTRUCTION MAP entry for "CMPZ"
		IMAP.put(0xA, (arg, level) -> {
			if(level < 1 || level > 2) {
				throw new IllegalArgumentException(
					"Illegal indirection level in ADD instruction");
			}
			if(level > 1) {
				IMAP.get(0xA).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			} else {
				if(memory.getData(cpu.getMemBase()+arg) == 0)
				{
					cpu.setAccum(1);
				}
				else
				{
					cpu.setAccum(0);
				}
				cpu.incrPC();
			}
		});
		
		//INSTRUCTION MAP entry for "JUMP"
		IMAP.put(0xB, (arg, level) -> {
			if(level < 0 || level > 2 && level != 3) {
				throw new IllegalArgumentException(
					"Illegal indirection level in ADD instruction");
			}
			if(level == 3)
			{
				int arg1 = memory.getData(cpu.getMemBase()+arg);
				cpu.setpCounter(arg1 + currentJob.getStartcodeIndex());
			}
			else if(level > 0) {
				IMAP.get(0xB).execute(memory.getData(cpu.getMemBase()+arg), level-1);
			}
			else {
				cpu.setpCounter(arg + cpu.getpCounter());   
			}
		});
		
		//INSTRUCTION MAP entry for "JUMPZ"
		IMAP.put(0xC, (arg, level) -> {
			if(level < 0 || level > 2 && level != 3) {
				throw new IllegalArgumentException(
					"Illegal indirection level in ADD instruction");
			}
			if(level == 3)
			{
				if(cpu.getAccum() == 0)
				{
					int arg1 = memory.getData(cpu.getMemBase()+arg);
					cpu.setpCounter(arg1 + currentJob.getStartcodeIndex());
				}
				else
				{
					cpu.incrPC();
				}
			}
			else if(level > 0) {
				IMAP.get(0xC).execute(memory.getData(cpu.getMemBase()+arg), level-1);
				
			} 
			else {
				if(cpu.getAccum() == 0)
				{
					cpu.setpCounter(arg + cpu.getpCounter()); 
				}
				else
				{
					cpu.incrPC();
				}
			}
		});
		
		//INSTRUCTION MAP entry for "HALT"
		IMAP.put(0xF, (arg, level) -> {
			callback.halt();			
		});
		
		for(int i = 0; i <=3; i++)
		{
			jobs[i] = new Job();
			jobs[i].setId(i);
			jobs[i].setStartcodeIndex(i*Code.CODE_MAX/4);
			jobs[i].setStartmemoryIndex(i*Memory.DATA_SIZE/4);
			jobs[i].getCurrentState().enter();
		}
		currentJob = jobs[0];
				
	}

	public int getAccum() {
		return cpu.getAccum();
	}

	public void setAccum(int accum) {
		cpu.setAccum(accum);
	}

	public int getpCounter() {
		return cpu.getpCounter();
	}

	public void setpCounter(int pCounter) {
		cpu.setpCounter(pCounter);
	}

	public int getMemBase() {
		return cpu.getMemBase();
	}

	public void setMemBase(int memBase) {
		cpu.setMemBase(memBase);
	}

	public void incrPC() {
		cpu.incrPC();
	}

	public int getData(int index) {
		return memory.getData(index);
	}
	
	public int[] getData()
	{
		return memory.getData();
	}

	public void setData(int index, int value) {
		memory.setData(index, value);
	}

	public int hashCode() {
		return memory.hashCode();
	}

	public boolean equals(Object obj) {
		return memory.equals(obj);
	}

	public String toString() {
		return memory.toString();
	}
	
	public States getCurrentState()
	{
		return currentJob.getCurrentState();
	}
	
	public void setCurrentState(States currentState)
	{
		currentJob.setCurrentState(currentState);
	}
	
	public Instruction get(int x)
	{
		return IMAP.get(x);
	}
	
	public void clearJob()
	{
		memory.clear(currentJob.getStartmemoryIndex(), currentJob.getStartmemoryIndex()+Memory.DATA_SIZE/4);
		code.clear(currentJob.getStartcodeIndex(), currentJob.getStartcodeIndex()+currentJob.getCodeSize());
		setAccum(0);
		setpCounter(currentJob.getStartcodeIndex());
		currentJob.reset();		
	}
	
	void step()
	{
		try
		{
			int pc = cpu.getpCounter();
			if(currentJob.getStartcodeIndex() > pc || pc > currentJob.getStartcodeIndex()+currentJob.getCodeSize())
			{
				throw new CodeAccessException("Not in current job");
			}
			int opcode = code.getOp(pc);
			int indirLvl = code.getIndirLvl(pc);
			int arg = code.getArg(pc);
			get(opcode).execute(arg, indirLvl);
					
		}
		catch(Exception e)
		{
			callback.halt();
			throw e;
		}
	}
	
	public int getChangedIndex()
	{
		return memory.getChangedIndex();
	}
	
}
