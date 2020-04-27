import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server extends Thread {
	private ServerSocket serverSocket;
	private Set<ServerHandler> serverThreadThreads = new HashSet<ServerHandler>();
	int countConnection = 0;

	public Server(String portNumb) throws IOException {
		serverSocket = new ServerSocket(Integer.valueOf(portNumb));
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket s = serverSocket.accept();

				if (s.isConnected()) {
					countConnection++;
				}
				ServerHandler serverThreadThread = new ServerHandler(s, this);
				serverThreadThreads.add(serverThreadThread);
				serverThreadThread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isEnoughPlayers(int players) {
		return (countConnection == players) ? true : false;
	}

	public void sendMessage(String message) {
		try {
			serverThreadThreads.forEach(t -> t.getPrintWriter().println(message));
		} catch (Exception e) {
			System.out.println("Server: " + e);
			e.printStackTrace();
		}
	}

	public Set<ServerHandler> getServerThreadThread() {
		return serverThreadThreads;
	}
}
