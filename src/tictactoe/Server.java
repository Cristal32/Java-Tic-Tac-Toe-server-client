package tictactoe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private ServerSocket serverSocket;
    private List<PlayerHandler> playersList;
    private int NbrPlayers = 0;
    private int currentPlayer;

    public Server() {
    	playersList = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("Server is running. Waiting for players...");
            while (true) {
            	if(playersList.size() < 2) { //only 2 clients can play
            		Socket clientSocket = serverSocket.accept();
                    PlayerHandler playerHandler = new PlayerHandler(clientSocket, this);
                    playersList.add(playerHandler);
                    new Thread(playerHandler).start();
                    
                    if(playersList.size() == 1) {
            			System.out.println("Player X connected :");
            		}else {
            			System.out.println("Player O connected :");
            		}
            	}
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //calls the send method on all client handler threads, so all client threads send a message to their respected client
    public void broadcastMessage(String message) {
        for (PlayerHandler player : playersList) {
            player.sendMessage(message);
        }
    }
    
    //new instance of the server and so initiates it
    public static void main(String[] args) {
        new Server();
    }
    
    //client thread management
    public class PlayerHandler implements Runnable {
        private Socket clientSocket;
        private Server server;
        private BufferedReader reader;
        private PrintWriter writer;
        private int playerIndex;

        //contructor
        public PlayerHandler(Socket socket, Server server) {
            this.clientSocket = socket;
            this.server = server;
            
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                
                //send player's role (X or O)
                synchronized (server) {
                    playerIndex = NbrPlayers++;
                    String role = (playerIndex % 2 == 0) ? "X" : "O";
                    writer.println(role);
                }
                
            } catch (IOException e) { e.printStackTrace(); }
        }

        //run method
        @Override
        public void run() {
            try {
                // Send the initial state of the board to the client
                String initialBoardState = getInitialBoardState();
                writer.println(initialBoardState);

                // Receive and process moves from the player
                while (true) {
                    String move = reader.readLine(); //receive move from the player
                    if (move != null) {
                    	server.broadcastMessage(move); // server broadcasts the move to all players
                    	
                    	//then it's the other player's turn, send a message to them so they can play
                    	int otherPlayerIndex = (playerIndex + 1) % 2;
	                	PlayerHandler otherPlayer = server.playersList.get(otherPlayerIndex);
	                    otherPlayer.sendMessage("Your turn!");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (writer != null) writer.close();
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //get the empty string for the initial value of the cells
        private String getInitialBoardState() {
            StringBuilder initialBoardState = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                initialBoardState.append(",");
            }
            return initialBoardState.toString();
        }

        //send a message to the respective client
        public void sendMessage(String message) {
            writer.println(message);
        }
    }
}