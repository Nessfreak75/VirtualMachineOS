package edu.kennesaw.core.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Dump {
    static final String _fileName = "core-dump.txt";

    public static void coreDump(String header, int[] data){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(_fileName, true));

            bw.newLine();
            bw.append(header);

            for(int i = 0; i < data.length; i++){
                bw.newLine();

                String hexAddress = String.format("0x%1$02X",4 * i);
                String hexData = String.format("0x%1$02X",data[i]);

                bw.append(String.format("%s - %s ", hexAddress, hexData));
            }

            bw.newLine();
            bw.append("-----end core dump-----");
            bw.close();

        }catch(Exception e){
            System.out.println("Cannot dump data: " + e.getMessage());
        }
    }

}
