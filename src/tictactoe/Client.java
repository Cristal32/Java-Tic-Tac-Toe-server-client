package tictactoe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Client extends JFrame{
	private String role;
	
	private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    static final String MARK_X = "X";
	static final String MARK_O = "O";
    private JButton[] tiles;
    private boolean isMyTurn;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    //constructor
    public Client(String serverAddress, int port) {
        try {
        	//connect to server
            socket = new Socket(serverAddress, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to the server.");
            
            // Read player's role (X or O) from the server
            role = reader.readLine();
            System.out.println("my role is " + role);
            isMyTurn = role.equals("X"); //if my role is X it's my turn first to play

            //initiate board
            initComponents();

            // Start a thread to listen for updates from the server
            new Thread(() -> {
                try {
                    while (true) {
                    	if(!isMyTurn) {
                    		Arrays.stream(tiles).forEach(t -> t.setEnabled(false));
                    	}
                    	
                    	//receive a message from server
                        String message = reader.readLine();
                        
                        if(message.equals("Your turn!")) {
                        	isMyTurn = true;
                        	Arrays.stream(tiles).forEach(t -> t.setEnabled(true));
                        }else {
                        	updateBoard(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Initialize the board 
    private void initComponents() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setLayout(new GridLayout(3, 3));

        tiles = new JButton[9];
        for (int i = 0; i < 9; i++) {
            tiles[i] = new JButton();
            tiles[i].setFont(new Font("Arial", Font.BOLD, 125));
            tiles[i].setFocusable(false);
            tiles[i].addActionListener((ActionListener) new TileActionListener(i));
            this.add(tiles[i]);
        }

        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Player " + role);
        this.setResizable(false);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
    
    //update the board
    private void updateBoard(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] marks = message.split(",");
            for (int i = 0; i < marks.length; i++) {
                tiles[i].setText(marks[i]);
                if(!tiles[i].getText().isEmpty() && marks[i].equals(MARK_O)) { //if a tile is not empty and it's O, make it blue
                	tiles[i].setForeground(Color.BLUE);
                }
            }
            checkState();
        });
    }
    
    // Check the state of the board if there is line of 3 marks of the same nature
    protected void checkState() {
		if(checkMark(MARK_X)) {
			return;
		}
		
		if(checkMark(MARK_O)) {
			return;
		}
		
		if(checkDraw()) {
			return;
		}
	}
    
    //check if there is a draw
    protected boolean checkDraw() {
		int i = 0;
		while(!tiles[i].getText().isEmpty()) {
			if(i == tiles.length - 1) {
				Arrays.stream(tiles).forEach(t -> t.setEnabled(false));
				break;
			}
			i++;
		}
		
		return i == tiles.length - 1;
	}
    
    //check if there is a line of 3 of the mark, return true if it exists
    protected boolean checkMark(String mark) {
		boolean isDone = false;
		
		//Horizontal
		isDone = checkDirection(0, 1, 2, mark);
		if(!isDone) { isDone = checkDirection(3, 4, 5, mark); }
		if(!isDone) { isDone = checkDirection(6, 7, 8, mark); }
		
		//Vertical
		if(!isDone) { isDone = checkDirection(0, 3, 6, mark); }
		if(!isDone) { isDone = checkDirection(1, 4, 7, mark); }
		if(!isDone) { isDone = checkDirection(2, 5, 8, mark); }
		
		//Diagonal
		if(!isDone) { isDone = checkDirection(0, 4, 8, mark); }
		if(!isDone) { isDone = checkDirection(2, 4, 6, mark); }
		
		return isDone;
	}
    
    //check if 3 positions have the same mark
    protected boolean checkDirection(int posA, int posB, int posC, String mark) {
		if(tiles[posA].getText().equals(mark) && tiles[posB].getText().equals(mark) && tiles[posC].getText().equals(mark)) {
			setWinner(posA, posB, posC, mark);
			return true;
		}
		
		return false;
	}
    
    //if there's a winning line, make it green and disable all buttons
    protected void setWinner(int posA, int posB, int posC, String mark) {
    	if(role.equals(mark)) {
    		tiles[posA].setBackground(Color.GREEN);
    		tiles[posB].setBackground(Color.GREEN);
    		tiles[posC].setBackground(Color.GREEN);
    	}else {
    		tiles[posA].setBackground(Color.RED);
    		tiles[posB].setBackground(Color.RED);
    		tiles[posC].setBackground(Color.RED);
    	}
    	
		Arrays.stream(tiles).forEach(t -> t.setEnabled(false));
	}

    //when clicking on a tile
    private class TileActionListener implements ActionListener {
        private int index;

        //constructor
        public TileActionListener(int index) {
            this.index = index;
        }

        //implemented actionPerformed method
        @Override
        public void actionPerformed(ActionEvent e) {
            if (tiles[index].getText().isEmpty()) {
                if (role.equals(MARK_X)) {
                    tiles[index].setForeground(Color.BLACK);
                    tiles[index].setText(MARK_X);
                } else {
                    tiles[index].setForeground(Color.BLUE);
                    tiles[index].setText(MARK_O);
                }

                // Send the updated board state to the server
                StringBuilder boardState = new StringBuilder();
                for (JButton tile : tiles) {
                    boardState.append(tile.getText()).append(",");
                }
                writer.println(boardState.toString()); //sends move to client handler/server
                
                isMyTurn  = false; //once the move done, have to wait for a respond from server that it's my turn
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 12345);
    }
}