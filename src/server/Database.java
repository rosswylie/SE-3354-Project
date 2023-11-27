package server;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.HashSet;

public class Database {
    private static HashSet<String> usernames = new HashSet<>(), categories = new HashSet<>();

    public static void setUsernames(HashSet<String> usernames){
        Database.usernames = usernames;
    }

    public static void setCategories(HashSet<String> categories){
        Database.categories = categories;
    }

    public static HashSet<String> getCategories(){
        return categories;
    }

    public static String submitRecord(String username, String category, String description, String proof){
        if (!usernames.contains(username) || username.contains("\n"))
            return "Error Validating Username";
        if (!categories.contains(category) || category.contains("\n"))
            return "Invalid Category";
        if (description.length() < 5 || description.length() > 1000 || description.contains("\n"))
            return "Invalid Description";
        if (!(proof.contains("youtube.com") || proof.contains("youtu.be") || proof.contains("drive.google.com")) || proof.contains("\n"))
            return "Invalid Proof";

        try {
            File file = new File("Submitted Records.txt");
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter("Submitted Records.txt", true));
            writer.append(username);
            writer.append("\n");
            writer.append(category);
            writer.append("\n");
            writer.append(description);
            writer.append("\n");
            writer.append(proof);
            writer.append("\n\n");
            writer.close();
            return "Record Submitted";
        } catch (Exception e) {
            return "Error Submitting Record, Try Again Later";
        }
    }
}