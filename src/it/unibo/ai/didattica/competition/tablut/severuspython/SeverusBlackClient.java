package it.unibo.ai.didattica.competition.tablut.severuspython;

import java.io.IOException;
import java.net.UnknownHostException;


public class SeverusBlackClient {

	public static void main(String[] args) throws UnknownHostException, ClassNotFoundException, IOException {
        String[] array = new String[]{"BLACK", "60", "localhost", "debug"};
        if (args.length>0){
            array = new String[]{"BLACK", args[0]};
        }
        SeverusClient.main(array);
    }
}
