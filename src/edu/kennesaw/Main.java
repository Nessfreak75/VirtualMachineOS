package edu.kennesaw;

import edu.kennesaw.core.execution.OsDriver;
import edu.kennesaw.core.utils.Config;

public class Main {

    public static void main(String[] args) {
        OsDriver driver;

        try{
            driver = new OsDriver(4, 1024 * Config.WORD_SIZE, 2048 * Config.WORD_SIZE);
            driver.powerOn();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        System.out.println("Program ended.");
    }
}