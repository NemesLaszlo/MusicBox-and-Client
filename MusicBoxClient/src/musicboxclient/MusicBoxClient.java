package musicboxclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

public class MusicBoxClient {

    public static void main(String[] args) {
        String hostname =args[0];
        int serverPort = Integer.parseInt(args[1]);

        try (
                Socket echoSocket = new Socket(hostname, serverPort);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream());
                Scanner in = new Scanner(echoSocket.getInputStream());
                Scanner stdIn = new Scanner(System.in);    
            )
        {
                WriterToServer wts = new WriterToServer(echoSocket, out, stdIn);
                ReaderFromServer rfs = new ReaderFromServer(echoSocket, in);
                wts.start();
                rfs.start();
                
                
                try{
                   wts.join(); 
                   rfs.join(); 
                }catch(InterruptedException e) {
                    System.out.println("InterruptedException");
                }   
                
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ReaderFromServer extends Thread {
    private final Socket socket;
    private final Scanner input;

    public ReaderFromServer(Socket socket, Scanner input) {
        this.socket = socket;
        this.input = input;
    }

    @Override
    public void run() {
        while(input.hasNextLine()) {
            String line = input.nextLine();
            String[] str = line.split(" ");
            System.out.println(line);
            if(str[0].equals("Playing")) {
                try(Synthesizer synth = MidiSystem.getSynthesizer()){
                        synth.open();
                        MidiChannel channel = synth.getChannels()[0];
                        int[] note = { 69, 71, 60, 62, 64, 65, 67 };
                        int nval = -1;
                        while (input.hasNextLine()) {
                            if(nval != -1){
                                channel.noteOff(nval);
                            }
                            String[] tmp = input.nextLine().split(" ");

                            if(tmp[0].equals("FIN")){
                                System.out.println(tmp[0]);
                                Thread.sleep(2000);
                                break;
                            } else {
                                System.out.println(tmp[0] + " " + tmp[1]);
                            }

                            int c = tmp[0].charAt(0) - 65;
                            if(c > -1 && c < 7){
                                nval = note[c];
                                if(tmp[0].indexOf('#') != -1){
                                    nval++;
                                } else if (tmp[0].indexOf('b') != -1){
                                    nval--;
                                }
                                int ind = tmp[0].indexOf('/');
                                if(ind != -1){
                                    nval += 12 * Character.getNumericValue(tmp[0].charAt(ind+1));
                                }
                                //System.out.println(nval);
                                channel.noteOn(nval,1000);
                            }
                        }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
            
    }
          
}

class WriterToServer extends Thread {
    private final Socket socket;
    private final PrintWriter output;
    private final Scanner fromconsole;

    public WriterToServer(Socket socket, PrintWriter output, Scanner fromconsole) {
        this.socket = socket;
        this.output = output;
        this.fromconsole = fromconsole;
    }

    @Override
    public void run() {
        while(true) {
            String msg = fromconsole.nextLine();
            output.println(msg);
            output.flush();
        }   
    }
}



