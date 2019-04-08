package edu.kennesaw.core.execution;

import edu.kennesaw.core.converters.Hexadecimal;
import edu.kennesaw.core.memory.Memory;
import edu.kennesaw.core.memory.MemoryOverflowException;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessQueue;
import edu.kennesaw.core.processes.ProcessState;
import edu.kennesaw.core.processes.ProcessType;
import edu.kennesaw.core.utils.BitUtils;
import edu.kennesaw.core.utils.Config;
import edu.kennesaw.metrics.Metrics;

import java.io.*;

public class Loader {
    //Control card headers
    private final String[] CARD_HEADERS = {"job", "data", "end"};
    //Fields
    private final File _programFile;
    private final ProcessQueue _jobQueue;
    private final Memory _disk;

    public Loader(String programFilePath, ProcessQueue jobQueue, Memory disk) throws FileNotFoundException{
        _programFile = new File(programFilePath);

        if(!_programFile.exists())
            throw new FileNotFoundException(String.format("File path: %s", programFilePath));

        _jobQueue = jobQueue;
        _disk = disk;
    }

    public void loadJobs() throws IOException, MemoryOverflowException {
        BufferedReader reader = new BufferedReader(new FileReader(_programFile));
        int currentAddress = 0;
        String line;

        while((line = reader.readLine()) != null){
            if(isJobControlCard(line)){
                ProcessControlBlock pcb = createProcessControlBlock(parseControlCard(line));
                //Save the beginning address in pcb
                pcb.diskAddress = currentAddress;

                while(!isEnd(line = reader.readLine())){
                    if(isInstruction(line)){
                        int instruction = parseInstruction(line);

                        _disk.write(currentAddress, BitUtils.getBytes(instruction));
                        currentAddress += Config.WORD_SIZE;
                    }else if(isDataControlCard(line)){
                        int[] controlCard = parseControlCard(line);

                        pcb.inputBufferSize = controlCard[0];
                        pcb.outputBufferSize = controlCard[1];
                        pcb.temporaryBufferSize = controlCard[2];
                    }
                }
                _jobQueue.add(pcb);
                //Metrics log
                Metrics.job().register(pcb.jobId, pcb.priority);
            }
        }

    }

    private ProcessControlBlock createProcessControlBlock(int[] jobControlCard){
        ProcessControlBlock pcb = new ProcessControlBlock();

        pcb.jobId = jobControlCard[0];
        pcb.textSize = jobControlCard[1];
        pcb.priority = jobControlCard[2];

        pcb.type = ProcessType.CPU;
        pcb.state = ProcessState.NEW;

        return pcb;
    }

    private boolean isInstruction(String line){
        return line.startsWith("0x");
    }
    private boolean isJobControlCard(String line){
        return line.toLowerCase().contains(CARD_HEADERS[0]);
    }
    private boolean isDataControlCard(String line){
        return line.toLowerCase().contains(CARD_HEADERS[1]);
    }
    private boolean isEnd(String line){
        return line.toLowerCase().contains(CARD_HEADERS[2]);
    }
    private int parseInstruction(String line){
        return Hexadecimal.toInt(line);
    }

    /* Job control card structure:
     * [0] => job id
     * [1] => text size (words)
     * [2] => priority
     * Data control card structure:
     * [0] => input buffer size (words)
     * [1] => output buffer size (words)
     * [2] => temp buffer size (words)
     */
    private int[] parseControlCard(String line){
        int[] controlCard = new int[3];
        int currentCardIndex = 0;

        line = line.replaceFirst("//", "");

        for(String component : line.split(" ")){
            if(!component.isEmpty() && !isControlHeader(component))
                controlCard[currentCardIndex++] = Hexadecimal.toInt(component);
        }

        return controlCard;
    }

    private boolean isControlHeader(String text){
        for(String header : CARD_HEADERS)
            if(header.equals(text.toLowerCase()))
                return true;

        return false;
    }

}
