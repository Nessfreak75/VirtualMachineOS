package edu.kennesaw.core.utils;

public abstract class Instruction {

    public enum OpCode{
        RD, WR, ST, LW, MOV, ADD, SUB, MUL, DIV, AND, OR, MOVI, ADDI, MULI, DIVI, LDI, SLT, SLTI, HLT, NOP, JMP, BEQ, BNE, BEZ, BNZ, BGZ, BLZ
    }
    public enum Type{
        ARITHMETIC, BRANCH, JUMP, IO
    }

    public static Type getType(int instruction) throws InvalidInstructionTypeException{
        int typeCode = BitUtils.getBits(instruction, 2, 30);

        switch (typeCode){
            case 0:
                return Type.ARITHMETIC;
            case 1:
                return Type.BRANCH;
            case 2:
                return Type.JUMP;
            case 3:
                return Type.IO;
            default:
                throw new InvalidInstructionTypeException(typeCode);
        }
    }
    public static OpCode getOpCode(int instruction) throws InvalidOpCodeException{
        int opCode = BitUtils.getBits(instruction, 6, 24);

        switch(opCode){
            case  0:
                return OpCode.RD;
            case  1:
                return OpCode.WR;
            case  2:
                return OpCode.ST;
            case  3:
                return OpCode.LW;
            case  4:
                return OpCode.MOV;
            case  5:
                return OpCode.ADD;
            case  6:
                return OpCode.SUB;
            case  7:
                return OpCode.MUL;
            case  8:
                return OpCode.DIV;
            case  9:
                return OpCode.AND;
            case  10:
                return OpCode.OR;
            case  11:
                return OpCode.MOVI;
            case  12:
                return OpCode.ADDI;
            case  13:
                return OpCode.MULI;
            case  14:
                return OpCode.DIVI;
            case  15:
                return OpCode.LDI;
            case  16:
                return OpCode.SLT;
            case  17:
                return OpCode.SLTI;
            case  18:
                return OpCode.HLT;
            case  19:
                return OpCode.NOP;
            case  20:
                return OpCode.JMP;
            case  21:
                return OpCode.BEQ;
            case  22:
                return OpCode.BNE;
            case  23:
                return OpCode.BEZ;
            case  24:
                return OpCode.BNZ;
            case  25:
                return OpCode.BGZ;
            case  26:
                return OpCode.BLZ;
            default:
                throw new InvalidOpCodeException(opCode);
        }

    }

}
