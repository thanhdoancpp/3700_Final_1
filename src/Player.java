import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.json.*;

public class Player {
	private static int MAX_PLAYERS = 3;
	public static ArrayList<String> mySymbol = new ArrayList<String>();
	private String[] symbols = { "Rock", "Scissors", "Paper" };
	public String rock = symbols[0];
	public String scissors = symbols[1];
	public String paper = symbols[2];
	public static int gameNum = 3;
	public ArrayList<Socket> socketList = new ArrayList<Socket>();
	public ArrayList<Client> manageThread = new ArrayList<Client>();
	public ArrayList<String> opponentGesture = new ArrayList<String>();
	public int totalScore = 0;
	
	public static void main(String[] args) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("Enter number of games: ");
		Scanner sc = new Scanner(System.in);
		gameNum = sc.nextInt();

		System.out.print("Enter a player ID (1, 2, 3): ");

		String player = sc.next();
		String name = null;
		String port = null;
		
		// hard code
		if (player.equals("1")) {
			name = "P1";
			port = "1111";
		} else if (player.equals("2")) {
			name = "P2";
			port = "2222";
		} else if (player.equals("3")) {
			name = "P3";
			port = "3333";
		}

		Server serverThread = new Server(port);
		serverThread.start();

		System.out.println(name + " is ready.");

		new Player().spawClient(bufferedReader, name, serverThread);

		sc.close();
	}

	public void spawClient(BufferedReader bufferedReader, String username, Server serverThread)
			throws Exception {
		System.out.println("Press enter when all players are ready.");
		String input = bufferedReader.readLine();

		// hard code for connection port
		if (username.equals("P1")) {
			input = "2:2222 3:3333";
		} else if (username.equals("P2")) {
			input = "1:1111 3:3333";
		} else if (username.equals("P3")) {
			input = "1:1111 2:2222";
		}

		String[] inputValues = input.split(" ");
		InetAddress ip = InetAddress.getByName("localhost");

		Socket socket = null;
		for (int i = 0; i < inputValues.length; i++) {
			String[] address = inputValues[i].split(":");

			try {
				socket = new Socket(ip, Integer.valueOf(address[1]));
				Client t = new Client(socket, username);
				manageThread.add(t);
				t.start();
			} catch (Exception e) {
				System.out.println("Socket error:" + e);
				e.printStackTrace();
			}
		}

		sendData(username, serverThread);
	}

	public void sendData(String username, Server serverThread) {
		try {
			int round = 1;

			while (round <= gameNum) {
				Random rd = new Random();

				String message = symbols[rd.nextInt(3)];
				mySymbol.add(message);

				while (!serverThread.isEnoughPlayers(MAX_PLAYERS - 1)) {
//					System.out.println(username + " waiting for enough players.");
					Thread.sleep(100);
				}

				System.out.println("----- Round " + round + " sends " + message);

				StringWriter stringWriter = new StringWriter();
				Json.createWriter(stringWriter).writeObject(Json.createObjectBuilder().add("running", "Running")
						.add("username", username).add("round", round).add("message", message).build());

				serverThread.sendMessage(stringWriter.toString());

				for (Client thread : manageThread) {
					while (!thread.getIsReceived()) {
//						System.out.println(username + " waiting for all players receiving the data.");
						Thread.sleep(100);
					}
					thread.setIsReceived(false);
					opponentGesture.add(thread.getClientLog().get(round - 1));
				}

				totalScore += calcScore(message, opponentGesture);
				opponentGesture = new ArrayList<String>();

				System.out.println("Score after round " + round + ": " + totalScore);

				round++;
			}

			System.out.println("Finish my turn.");
			System.out.println("Final score: " + totalScore);

			StringWriter stringWriter = new StringWriter();
			Json.createWriter(stringWriter)
					.writeObject(Json.createObjectBuilder().add("username", username).add("done", "Exit").build());
			serverThread.sendMessage(stringWriter.toString());

			System.exit(0);
		} catch (Exception e) {
			System.out.println("Player: " + e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public int calcScore(String myGesture, ArrayList<String> opponents) {
		int score = 0;

		for (String oppGesture : opponents) {
			score += compare(myGesture, oppGesture);
		}

		if (score < 0) {
			score = 0;
		}

		return score;
	}

	// false is loss, true is win
	private int compare(String gesture, String opponent) {
		if (gesture.equals(opponent)) {
			return 0;
		} else if ((gesture.equals(rock) && opponent.equals(scissors))
				|| (gesture.equals(paper) && opponent.equals(rock))
				|| (gesture.equals(scissors) && opponent.equals(paper))) {
			return 1;
		} else {
			return -1;
		}
	}
}
