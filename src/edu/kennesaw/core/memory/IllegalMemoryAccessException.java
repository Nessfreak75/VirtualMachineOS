package edu.kennesaw.core.memory;

import edu.kennesaw.core.converters.Integral;

public class IllegalMemoryAccessException extends Exception {
    public IllegalMemoryAccessException(int address){
        super(String.format("Illegal access at address %s", Integral.toHex(address)));
    }
}
