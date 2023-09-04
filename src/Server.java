import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private final ArrayList<ClientHandler> clients;
    private ServerSocket server;
    private boolean done;

    public Server() {
        clients = new ArrayList<>();
        done = false;
    }
    @Override
    public void run() {
        try {
            server = new ServerSocket(8080); // put this port number in arg of client
            ExecutorService pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ClientHandler handler = new ClientHandler(client);
                clients.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            try {
                shutdown();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void broadcast(String message){
        for (ClientHandler ch : clients){
            if (ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() throws IOException {
        done = true;
        if (!server.isClosed()){
            server.close();
        }
        for (ClientHandler ch : clients){
            ch.shutdown();
        }
    }

    class ClientHandler implements Runnable {
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String username = in.readLine();
                System.out.println(username + " connected");
                String message;
                while ((message = in.readLine()) != null){
                    broadcast(username + ": " + message);
                }
            } catch (IOException e){
                try {
                    shutdown();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }

        public void shutdown() throws IOException {
            in.close();
            out.close();
            if (!client.isClosed()){
                client.close();
            }
        }

    }
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}

