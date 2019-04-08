package edu.kennesaw.core.execution;

import edu.kennesaw.core.execution.CentralProcessingUnit.Cpu;
import edu.kennesaw.core.execution.CentralProcessingUnit.InvalidRegisterAccessException;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.utils.Config;

public class Dispatcher {

    public ProcessControlBlock switchContext(Cpu cpu, ProcessControlBlock newPcb) throws  InvalidRegisterAccessException{
        ProcessControlBlock oldPcb = cpu.getProcess();

        if(oldPcb != null)
            savePcb(cpu, oldPcb);

        loadPcb(cpu, newPcb);
        cpu.setProcess(newPcb);

        return oldPcb;
    }

    private void savePcb(Cpu cpu, ProcessControlBlock pcb) throws InvalidRegisterAccessException {
        pcb.programCounter = cpu.getProgramCounter();

        for(int i = 0; i < Config.REGISTER_COUNT; i++)
            pcb.registers[i] = cpu.getRegisterValue(i);

    }
    private void loadPcb(Cpu cpu, ProcessControlBlock pcb) throws InvalidRegisterAccessException{
        cpu.setProgramCounter(pcb.programCounter);

        for(int i = 0; i < Config.REGISTER_COUNT; i++)
            cpu.setRegisterValue(i, pcb.registers[i]);

    }
}
