package edu.kennesaw.core.execution.CentralProcessingUnit;

import edu.kennesaw.core.converters.Hexadecimal;
import edu.kennesaw.core.memory.*;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessState;
import edu.kennesaw.core.utils.BitUtils;
import edu.kennesaw.core.utils.Config;
import edu.kennesaw.core.utils.Dump;
import edu.kennesaw.core.utils.Instruction;
import edu.kennesaw.metrics.Metrics;

import java.util.Objects;

public class Cpu implements Runnable{
    //Fields
    private final int _id;
    private volatile boolean _exitFlag = false;
    private volatile int _executionDelay;
    private volatile ProcessControlBlock _process;
    private volatile int[] _registers;
    private volatile int _programCounter;
    private volatile Cache _cache;
    private volatile MemoryManagementUnit _ram;
    private volatile boolean _isIdle = true;
    //Getters & setters
    public int getId(){ return _id; }
    public ProcessControlBlock getProcess() { return _process; }
    public void setProcess(ProcessControlBlock value) {
        _process = value;
        _isIdle = false;
        Metrics.cpu().jobAssigned(_id, value.jobId);
    }

    public int getExecutionDelay(){return _executionDelay;}
    public void setExecutionDelay(int value) { _executionDelay = value; }
    public int getProgramCounter() { return _programCounter; }
    public void setProgramCounter(int value) { _programCounter = value; }

    public Cpu(int id, MemoryManagementUnit ram) throws MemoryInitializationException{
        _id = id;
        _registers = new int[Config.REGISTER_COUNT];
        _ram = ram;
        _cache = new Cache(128);
        //Metrics log
        Metrics.memory().register(_cache, String.format("CPU#%d cache", id));
    }

    /**
     * Indicates whether this CPU is working on a process.
     * @return True if the CPU is not working otherwise false.
     */
    public boolean isIdle(){
        return _isIdle;
    }
    public boolean isProcessDone() { return _process.state == ProcessState.TERMINATED; }
    /**
     * Tells the CPU to shut down its execution thread.
     */
    public void exit(){
        _exitFlag = true;
    }

    @Override
    public void run() {
        while(!_exitFlag){
            if(_process == null || _process.state != ProcessState.RUNNING) continue;

            System.out.println(String.format("Working on process %d with status %s on CPU#%d.", _process.jobId, _process.state, _id));

            executionLoop();
        }
        _exitFlag = false;
        System.out.println(String.format("Terminating CPU#%d.", _id));
    }
    private void executionLoop(){
        String instructionText = "";

        int executionResult = 0;
        try{
            int instruction = fetch();

            instructionText = Integer.toHexString(instruction);

            System.out.println("Executing instruction: 0x" + Integer.toHexString(instruction));

            Instruction.Type type = Instruction.getType(instruction);
            Instruction.OpCode opCode = Instruction.getOpCode(instruction);

            executionResult = execute(type, opCode, instruction);

            //This sleeps the thread after every simulated CPU cycle.
            //This helps to make difference between single-threaded vs. multi-threaded clearer.
            if(_executionDelay > 0)
                Thread.sleep(_executionDelay);

            if(executionResult == 1 || _process.textSize * Config.WORD_SIZE == _programCounter){
                evictCache();
                dumpProcessMemory();
                _process.state = ProcessState.TERMINATED;
                _isIdle = true;

            }
        }catch(Exception e){
            //If any exception is thrown during execution, then
            //terminate the process.

            System.out.println(String.format("Exception thrown in process %d (0x%s). Process was terminated. Exception: %s", instructionText,  _process.jobId, e.getMessage()));
            _process.state = ProcessState.TERMINATED;
        }
    }
    private int fetch() throws IllegalMemoryAccessException {
        int instruction = _ram.read(_programCounter, _process.pageTable);

        _programCounter += Config.WORD_SIZE;

        return instruction;
    }
    //0 - continue program
    //1 - terminated program
    private int execute(Instruction.Type type, Instruction.OpCode opCode, int instruction) throws IllegalMemoryAccessException, MemoryOverflowException, InterruptedException{
        int s1reg, s2reg, dreg, breg, address, reg1, reg2;
        s1reg = s2reg = dreg = breg = address = reg1 = reg2 = 0;

        //this switch needs to cover all type enums
        //see Instruction class for enum values and project file for functionality.
        switch (type){
            case ARITHMETIC:
                s1reg = BitUtils.getBits(instruction, 4, 20);
                s2reg = BitUtils.getBits(instruction, 4, 16);
                dreg = BitUtils.getBits(instruction, 4, 12);
                break;
            case BRANCH:
                breg = BitUtils.getBits(instruction, 4, 20);
                dreg = BitUtils.getBits(instruction, 4, 16);
                address = BitUtils.getBits(instruction, 16);
                break;
            case JUMP:
                address = BitUtils.getBits(instruction, 24);
                break;
            case IO:
                Metrics.job().ioMade(_process.jobId);
                reg1 = BitUtils.getBits(instruction, 4, 20);
                reg2 = BitUtils.getBits(instruction, 4, 16);
                address = BitUtils.getBits(instruction, 16);
                break;
        }
        //this switch needs to cover all opCode enums
        //see Instruction class for enum values and project file for functionality.
        //possibly needs to change for caching.
        switch (opCode) {
            case RD:
                //_registers[reg1] = reg2 == 0 ? memoryRead(address) : memoryRead(_registers[reg2]);
                _registers[reg1] = address == 0 ? memoryRead(_registers[reg2]) : memoryRead(address);
                break;
            case WR:
                if(address == 0)
                    memoryWrite(_registers[reg2], _registers[reg1]);
                else
                    memoryWrite(address, _registers[reg1]);
                break;
            case ST:
                memoryWrite(_registers[dreg], _registers[breg]);
                break;
            case LW:
                _registers[dreg] = memoryRead(_registers[breg] + address);
                break;
            case MOV:
                _registers[s1reg] = _registers[s2reg];
                break;
            case ADD:
                _registers[dreg] = _registers[s1reg] + _registers[s2reg];
                break;
            case SUB:
                _registers[dreg] = _registers[s1reg] - _registers[s2reg];
                break;
            case MUL:
                _registers[dreg] = _registers[s1reg] * _registers[s2reg];
                break;
            case DIV:
                _registers[dreg] = _registers[s1reg] / _registers[s2reg];
                break;
            case AND:
                //Might be wrong - instruction not clear
                _registers[dreg] = _registers[s1reg] & _registers[s2reg];
                break;
            case OR:
                //Might be wrong - instruction not clear
                _registers[dreg] = _registers[s1reg] | _registers[s2reg];
                break;
            case MOVI:
                _registers[dreg] = address;
                break;
            case ADDI:
                _registers[dreg] += address;
                break;
            case MULI:
                _registers[dreg] *= address;
                break;
            case DIVI:
                _registers[dreg] /= address;
                break;
            case LDI:
                _registers[dreg] = address;
                break;
            case SLT:
                _registers[dreg] = _registers[s1reg] < _registers[s2reg] ? 1 : 0;
                break;
            case SLTI:
                _registers[dreg] = _registers[s1reg] < address ? 1 : 0;
                break;
            case HLT:
                return 1;
            case NOP:
                //Do nothing here
                break;
            case JMP:
                _programCounter = address;
                break;
            case BEQ:
                if(_registers[breg] == _registers[dreg])
                    _programCounter = address;
                break;
            case BNE:
                if(_registers[breg] != _registers[dreg])
                    _programCounter = address;
                break;
            case BEZ:
                if(_registers[breg] == 0)
                    _programCounter = address;
                break;
            case BNZ:
                if(_registers[breg] != 0)
                    _programCounter = address;
                break;
            case BGZ:
                if(_registers[breg] > 0)
                    _programCounter = address;
                break;
            case BLZ:
                if(_registers[breg] < 0)
                    _programCounter = address;
                break;
        }
        return 0;
    }

    private int memoryRead2(int address) throws IllegalMemoryAccessException{

        return _ram.read(address, _process.pageTable);
    }
    private void memoryWrite2(int address, int data) throws MemoryOverflowException{
        _ram.write(address, data, _process.pageTable);
    }

    //The reason for the following two functions is if we change it
    //to use cache in the future. This way we can only change it here
    //and not at every single instruction.
    private void memoryWrite(int address, int data) {
        if(!_cache.write(address, data)){
            _cache.cache(address, data);
            _cache.write(address, data);
        }
    }

    private int memoryRead(int address) throws IllegalMemoryAccessException{
        int data = _cache.read(address);

        if(data == -1) {
            //If nothing is found then read it from memory and add it to cache.
            data = _ram.read(address, _process.pageTable);
            _cache.cache(address, data);
        }
        return data;
    }
    private void evictCache() throws MemoryOverflowException{
        for(Cache.AddressDataPair pair : _cache.getDirty()){
            int address = pair.getAddress();
            int data = pair.getData();

            try {
                _ram.write(address, data, _process.pageTable);
            }catch(Exception e){
                System.out.println();
            }
        }
        //_ram.write(pair.getAddress(), pair.getData(), _process.pageTable);
        _cache.flush();
    }
    private void dumpProcessMemory(){
        int[] data = new int[_process.pageTable.length * _ram.getPageSize() / Config.WORD_SIZE];

        try {
            for (int i = 0; i < data.length; i++)
                data[i] = _ram.read(4 * i, _process.pageTable);
        }catch(Exception e){

        }finally{
            Dump.coreDump("Core dump for process: " + _process.jobId, data);
        }

    }
    /*
    Memory read no cache
    private int memoryRead(int address) throws IllegalMemoryAccessException{
        return _ram.read(address, _process.pageTable);
    }
*/
    //Start register control
    public int getRegisterValue(int register) throws InvalidRegisterAccessException{
        if(register >= 0 && register < _registers.length)
            return _registers[register];
        else
            throw new InvalidRegisterAccessException(register);
    }
    public void setRegisterValue(int register, int value) throws InvalidRegisterAccessException{
        if(register >= 0 && register < _registers.length)
            _registers[register] = value;
        else
            throw new InvalidRegisterAccessException(register);
    }
    //End register control
}