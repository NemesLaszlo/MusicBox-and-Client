package musicbox;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class MusicBox {
    public static void main(String[] args) throws InterruptedException, IOException {
            Accepter server = new Accepter();
            server.start();
    }
}

class Accepter extends Thread {

    private ServerSocket serverSocket;
    public ArrayList<Client> clients;
    public HashMap<String, ArrayList<Input>> songs;
    public HashMap<String, ArrayList<String>> lyrics;
    public static AtomicInteger counter = new AtomicInteger(0);
    public HashMap<Integer, ArrayList<Integer>> nowPlaying;

    public Accepter() throws IOException {
        this.serverSocket = new ServerSocket(40000);
        this.clients = new ArrayList<>();
        this.songs = new HashMap<>();
        this.lyrics = new HashMap<>();
        this.nowPlaying = new HashMap<>();
    }

    @Override
    public void run() {
        System.out.println("Starting accepter");
        while (true) {
            try {
                Socket s = serverSocket.accept();
                Client client = new Client(s, clients, songs, lyrics);
                client.nowPlaying = this.nowPlaying;
                client.start();
                synchronized (clients) {
                    clients.add(client);
                }
                System.out.println(s.getRemoteSocketAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

class Client extends Thread {

    private Socket s;
    private ArrayList<Client> clients;
    private HashMap<String, ArrayList<Input>> songs;
    private HashMap<String, ArrayList<String>> lyrics;
    private Scanner in;
    private PrintWriter out;
    public HashMap<Integer, ArrayList<Integer>> nowPlaying;

    public Client(Socket socket, ArrayList<Client> clients, HashMap<String, ArrayList<Input>> songs,
        HashMap<String, ArrayList<String>> lyrics) throws IOException {
        this.s = socket;
        this.clients = clients;
        this.in = new Scanner(socket.getInputStream());
        this.out = new PrintWriter(socket.getOutputStream());
        this.songs = songs;
        this.lyrics = lyrics;
    }

    @Override
    public void run() {
        try {
            while (in.hasNextLine()) {
                String[] cmd = in.nextLine().split(" ");
                try{
                    switch (cmd[0]) {
                    case "add":
                        add(cmd[1]);
                        break;
                    case "addlyrics":
                        addlyrics(cmd[1]);
                        break;
                    case "play":
                        playSong(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), cmd[3]);
                        break;
                    case "change":
                        synchronized (nowPlaying) {
                            int ind = Integer.parseInt(cmd[1]);
                            nowPlaying.get(ind).set(0, Integer.parseInt(cmd[2]));
                            if (cmd.length > 3) {
                                nowPlaying.get(ind).set(1, Integer.parseInt(cmd[3]));
                            } else {
                                nowPlaying.get(ind).set(1, 0);
                            }
                        }
                        break;
                    case "stop":
                        synchronized (nowPlaying) {
                            int ind = Integer.parseInt(cmd[1]);
                            nowPlaying.get(ind).set(2, -1);
                        }
                        break;
                    default:
                        System.out.println("Unknown command");
                        break;
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        System.out.println("Client disconnected");
        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void close() throws Exception {
        out.close();
        in.close();
        s.close();
    }
    
    public void add(String title) {
        out.println("Give the sounds:");
        out.flush();
        ArrayList<Input> inputs = new ArrayList<>();
        String[] input = in.nextLine().split(" ");
        int i = 0;
        while (i < input.length) {
            String note = input[i++];
            String tmp = input[i++];
            if (tmp.contains(";")) {
                Repeat rep = new Repeat(Integer.parseInt(tmp.split(";")[0]), Integer.parseInt(tmp.split(";")[1]));
                inputs.add(rep);
            } else {
                Sound sound = new Sound(note, Integer.parseInt(tmp));
                inputs.add(sound);
            }
        }
        synchronized(songs) {
            songs.put(title, inputs);
        }
        synchronized(lyrics) {
            lyrics.put(title, new ArrayList<>());
        }
        System.out.println("Song added: " + title);
    }
    
    public void addlyrics(String title) {
        out.println("Give the lyrics parts:");
        out.flush();
        String[] tmp = in.nextLine().split(" ");
        List<String> list = Arrays.asList(tmp);
        synchronized(lyrics) {
            lyrics.put(title, new ArrayList<>(list));
        }
        System.out.println("Lyrics added for song: " + title);
    }

    public void playSong(int tempo, int trans, String title) throws InterruptedException {
        boolean  songcheck = false;
        synchronized(songs) {
            songcheck = songs.containsKey(title);
        }
        if (songcheck == false) {
            out.println("FIN");
            out.flush();
            return;
        }

        int j = Accepter.counter.incrementAndGet();
        out.println("Playing " + j);
        out.flush();
        synchronized (nowPlaying) {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(tempo);
            list.add(trans);
            list.add(0);
            nowPlaying.put(j, list);
            System.out.println(
                    nowPlaying.get(j).get(0) + " " + nowPlaying.get(j).get(1) + " " + nowPlaying.get(j).get(2) + " ");
        }
        ArrayList<Input> inputs = new ArrayList<>();
        synchronized(songs) {
            inputs = songs.get(title);
        }
        for (Input sound : inputs) {
            if (nowPlaying.get(j).get(2) == -1) {
                break;
            }
            playSound(sound, title, j);
        }
        out.println("FIN");
        out.flush();
    }

    public void playSound(Input sound, String title, int num) throws InterruptedException {
        if (sound instanceof Repeat) {
            int ind;
            ArrayList<Input> sounds;
            synchronized(songs) {
                ind = songs.get(title).indexOf(sound);
                sounds = songs.get(title);
            }
            //int ind = songs.get(title).indexOf(sound);
            for (int k = 0; k < ((Repeat) sound).times; ++k) {
                for (int i = ind - ((Repeat) sound).previous; i < ind; ++i) {
                    if (sounds.get(i) instanceof Sound && !((Sound) sounds.get(i)).note.equals("R"))
                        playSound(sounds.get(i), title, num);
                }
            }
        } else {
           
            if (nowPlaying.get(num).get(2) == -1) {
                return;
            }
            
            if (((Sound) sound).note.equals("R")) {
                out.println(((Sound) sound).note);
                out.flush();
                Thread.sleep(nowPlaying.get(num).get(0) * ((Sound) sound).length);
                return;
            }
            String note = transpone(((Sound) sound).note, nowPlaying.get(num).get(1));
            out.print(note);
            ArrayList<String> lyricss = new ArrayList<>();
            synchronized(lyrics) {
                lyricss = this.lyrics.get(title);
            }
            String lyrics = lyricss.size() > nowPlaying.get(num).get(2)
                    ? lyricss.get(nowPlaying.get(num).get(2))
                    : "???";
            nowPlaying.get(num).set(2, nowPlaying.get(num).get(2) + 1);
            out.print(" " + lyrics);
            out.println();
            out.flush();
            Thread.sleep(nowPlaying.get(num).get(0) * ((Sound) sound).length);
        }
    }
    
    public String transpone(String baseNote, int trans){
        int[] note = { 69, 71, 60, 62, 64, 65, 67 };
        int nval = -1;
        String[] tmp = baseNote.split(" ");

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
            nval += trans;
            return convertMidiValueToNote(nval, true);
        }
        return "";
    }

    public String convertMidiValueToNote(int value, boolean preferHigher) {
        HashMap<String, Integer> baseNotes = new HashMap<String, Integer>() {{
            put("C", 60);
            put("C#/Db", 61);
            put("D", 62);
            put("D#/Eb", 63);
            put("E", 64);
            put("F", 65);
            put("F#/Gb", 66);
            put("G", 67);
            put("G#/Ab", 68);
            put("A", 69);
            put("A#/Bb", 70);
            put("B", 71);
        }};

        String baseNote;
        Optional<String> note = baseNotes.keySet().stream().filter(x -> baseNotes.get(x) == ((value % 12) + 60)).findFirst();
        if (note.isPresent()) {
            baseNote = note.get();
            if (baseNote.contains("/")) {
                baseNote = baseNote.split("/")[preferHigher ? 0 : 1];
            }
        } else {
            throw new IllegalArgumentException("Should never occur...");
        }
        String midi = baseNote;


        int octave = (value / 12) - 5;
        if (octave != 0)
            midi += String.format("/%d", octave);
        return midi;
    }
}