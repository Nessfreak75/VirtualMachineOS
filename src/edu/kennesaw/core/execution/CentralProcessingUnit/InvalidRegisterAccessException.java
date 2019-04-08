package edu.kennesaw.core.execution.CentralProcessingUnit;

public class InvalidRegisterAccessException extends Exception {
    public InvalidRegisterAccessException(int register){
        super(String.format("The register index %d is not valid.", register));
    }
}
