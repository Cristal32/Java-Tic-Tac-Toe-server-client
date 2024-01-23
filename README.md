# Tic Tac Toe Java game

This is a simple Tic Tac Toe game in Java, that uses the concepts of multithreadind and sockets. 

![image](https://github.com/Cristal32/Java-Tic-Tac-Toe-server-client/assets/114748477/a918aa66-e8f9-4b46-9f48-73580136728b)

## To use test this game

If you want to test this game locally:

1. Clone the repository:
```shell
git clone https://github.com/Cristal32/Java-Tic-Tac-Toe-server-client.git
```
3. Run the server: Server.java
4. Run 2 instances of the client (for a game between you and your friend): Client.java
5. Have fun

Win scenario:
![image](https://github.com/Cristal32/Java-Tic-Tac-Toe-server-client/assets/114748477/e9c340d7-f0a0-4df3-b60b-a93aa0614399)

Draw scenario:
![image](https://github.com/Cristal32/Java-Tic-Tac-Toe-server-client/assets/114748477/d9b6d4bf-6401-4598-815b-67257dde9270)

## Private salon
You may have noticed another folder called salon, it was for another assignment to create a private salon where users have to get authenticated in order to join and see what other members say. 

Client 1 is successfully authenticated and sends a message: 
![image](https://github.com/Cristal32/Java-Tic-Tac-Toe-server-client/assets/114748477/98847361-f9ac-418a-8bf0-0f2a04b095d6)

Client 2 doesn't give the correct credentials and is therefore not allowed to join:
![image](https://github.com/Cristal32/Java-Tic-Tac-Toe-server-client/assets/114748477/eae7669e-c9fc-4e04-ad36-58ea09dfcc60)

Client 3 is successfully authenticated and can see the message from Client 1
![image](https://github.com/Cristal32/Java-Tic-Tac-Toe-server-client/assets/114748477/49a44f47-525a-457e-aa05-05b9786c7904)

The salon's messages get stored in a separate file: 'salon_messages.txt'
![image](https://github.com/Cristal32/Java-Tic-Tac-Toe-server-client/assets/114748477/58092c8e-e58b-4961-8dc7-4ec5469dd857)


