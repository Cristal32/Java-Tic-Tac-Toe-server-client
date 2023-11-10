package salon;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345); 
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner in = new Scanner(System.in);

            Thread receiveThread = new Thread(() -> {
                try {
                    Scanner serverIn = new Scanner(socket.getInputStream());
                    while (serverIn.hasNextLine()) {
                        System.out.println(serverIn.nextLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            String input;
            do {
                input = in.nextLine();
                out.println(input);
                out.flush();
            } while (!input.equals("/quit"));

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
