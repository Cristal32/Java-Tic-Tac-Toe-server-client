package salon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Serveur {
	private static List<PrintWriter> clientWriters = new ArrayList<>();
	private static List<PrintWriter> authenticatedClientWriters = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Serveur de chat démarré.");
        
        // Vérifier et créer le fichier de messages privés ---------------------------------
	    File privateMessagesFile = new File("salon_messages.txt");
	    if (!privateMessagesFile.exists()) {
	        try {
	            privateMessagesFile.createNewFile();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    // -------------------------------------------------------------------------------
        
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(12345); // Port du serveur (modifiable selon vos besoins)
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //thread client
    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private static int clientCount = 0;
    	private int clientId;

    	//constructeur
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            clientId = ++clientCount;
        }

        //methode run
        public void run() {
            try {
                Scanner in = new Scanner(clientSocket.getInputStream()); //creation d'un objet Scanner pour lire les donnees venant de InputStream du Socket client
                out = new PrintWriter(clientSocket.getOutputStream(), true); //creation d'un objet PrintWriter pour ecrire des donnees dans le OutputStream du clientSocket, true signifie que le PrintWriter doit vider son tampon a chaque appel de println
                out.println("Client" + clientId + " : "); //envoie une 1ere ligne de texte au client
                out.flush(); //vide le tampon du printWriter
                synchronized (clientWriters) {
                    clientWriters.add(out); //ajoute out a la liste partagee ClientWriters, synchrnized donc protegee contre les acces concurrents
                }
                
                //authenticate client --------------------------------------------------
                out.println("Veuillez vous authentifier. Nom d'utilisateur : ");
                String username = in.nextLine();
                out.println("Mot de passe : ");
                String password = in.nextLine();

                while (!authenticateUser(username, password)) {
                    out.println("Échec de l'authentification. Veuillez réessayer.");
                    out.println("Nom d'utilisateur : ");
                    username = in.nextLine();
                    out.println("Mot de passe : ");
                    password = in.nextLine();
                }
                
                out.println("Authentification réussie! Bienvenu au salon!");
                out.flush();
                while (true) {
                    String message = in.nextLine();
                    if (message.equals("/quit")) {
                        break;
                    }
                	broadcastMessage(message, out, clientId);
                }
                // ----------------------------------------------------------------------------------------------------

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                out.flush();
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //comparer les credetials avec le contenu du fichiers de mots de passes
        public boolean authenticateUser(String username, String password) {
            try (BufferedReader br = new BufferedReader(new FileReader("credentials.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String storedUsername = parts[0];
                        String storedPassword = parts[1];

                        // Comparer le nom d'utilisateur et le mot de passe
                        if (storedUsername.equals(username) && storedPassword.equals(password)) {
                        	authenticatedClientWriters.add(out);
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private static void broadcastMessage(String message, PrintWriter sender, int clientId) {
        synchronized (authenticatedClientWriters) {
            for (PrintWriter writer : authenticatedClientWriters) {
                if (writer != sender) {
                	String msg = "Client" + clientId + ": " + message;
                	writer.println(msg);
                    writer.flush();
                    
                 // Enregistrez les messages privés dans le fichier
                    savePrivateMessage(msg);
                }
            }
        }
    }
    
    private static void savePrivateMessage(String message) {
        try (PrintWriter fileWriter = new PrintWriter(new FileWriter("salon_messages.txt", true))) {
            fileWriter.println(message);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
  
}
