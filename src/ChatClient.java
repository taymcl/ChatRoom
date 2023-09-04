import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

public class ChatClient{

    private static final AtomicReference<PrintWriter> currentWriter = new AtomicReference<>(null);


    public static void main(String[] args) throws Exception {
        String addr = args[0];
        int port = Integer.parseInt(args[1]);
        String username = args[2];
        Thread keyboardHandler = new Thread(ChatClient::handleKeyboardInput);
        keyboardHandler.start();
        while (true) {
            try (Socket socket = new Socket(addr, port)){
                println("CONNECTED!");
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.println(username);
                writer.flush();
                currentWriter.set(writer);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    receiveMsg(line);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Thread.sleep(1_000);
            }
        }
    }

    private static void receiveMsg(String line) {
        String[] parts = line.split(":", 2);
        String content = parts[1];
        String username = parts[0];
        System.out.printf("%s: %s\n", username, content);
    }

    private static void sendMsg(String line) {
        try {
            while (true) {
                try {
                    PrintWriter writer = currentWriter.get();
                    writer.println(line);
                    writer.flush();
                    break;
                } catch (Exception ex) {
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException ex) {
            //This should never happen.
            ex.printStackTrace();
        }
    }

    private static void handleKeyboardInput() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while (true) {
            try {
                while ((line = reader.readLine()) != null) {
                    sendMsg(line);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void println(Object o) {
        System.err.println(o);
    }

}
