package server;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        Database.loadAdmins();
        Database.loadAccounts();
        Database.loadCategories();
        Database.loadApprovedRecords();
        Database.loadSubmittedRecords();

        ServerSocket server = new ServerSocket(4444);
        server.setReuseAddress(true);
        try {
            while (true) {
                Socket socket = server.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (Exception e) { e.printStackTrace(); }
        server.close();
    }

    private record ClientHandler(Socket socket) implements Runnable {
        @Override
        public void run() {
            PrintWriter toClient = null;
            BufferedReader fromClient = null;
            try {
                toClient = new PrintWriter(socket.getOutputStream(), true);
                fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                boolean loggedIn = false;
                String username = null;

                String line;
                while ((line = fromClient.readLine()) != null) {
                    if (line.equals("register")) {
                        toClient.println(Database.register(fromClient.readLine(), fromClient.readLine()));
                    }
                    else if (line.equals("login")) {
                        String uN = fromClient.readLine();
                        String response = Database.login(uN, fromClient.readLine());
                        if (response.equals("Login Successful")) {
                            loggedIn = true;
                            username = uN;
                        }
                        toClient.println(response);
                    }
                    else if (line.equals("getCategories")) {
                        HashSet<String> categories = Database.getCategories();
                        toClient.println(categories.size());
                        for (String category : categories)
                            toClient.println(category);
                    }
                    else if (line.equals("submitRecord")) {
                        if (loggedIn)
                            toClient.println(Database.submitRecord(username, fromClient.readLine(), fromClient.readLine(), fromClient.readLine()));
                        else
                            toClient.println("Please login");
                    }
                    else if (line.equals("isAdmin")) {
                        if (loggedIn)
                            toClient.println(Database.isAdmin(username));
                        else
                            toClient.println("Please login");
                    }
                    else if (line.equals("reviewRecord")) {
                        if (loggedIn) {
                            if (Database.isAdmin(username)) {
                                String[] record = Database.reviewRecord();
                                if (record != null) {
                                    toClient.println("Submitted Record:");
                                    for (int i = 0; i < 4; i++)
                                        toClient.println(record[i]);

                                    String review = fromClient.readLine();
                                    int score = 0;
                                    if (review.equals("accept"))
                                        score = Integer.parseInt(fromClient.readLine());
                                    Database.reviewRecord(review, score);
                                }
                                else
                                    toClient.println("No submitted records or record review in progress");
                            }
                            else
                                toClient.println("User lacks admin permissions");
                        }
                        else
                            toClient.println("Please login");
                    }
                    // TODO: Register more inputs and send appropriate outputs to client
                }
            } catch (IOException ignored) {} catch (InterruptedException e) { e.printStackTrace(); }
            if (toClient != null)
                toClient.close();
            if (fromClient != null) {
                try { fromClient.close(); } catch (IOException e) { e.printStackTrace(); }
            }
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}
