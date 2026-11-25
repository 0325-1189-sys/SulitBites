package com.sulitbites;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Starts the website. Settings come from environment variables: PORT, DATA_DIR and FIREBASE_URL (optional). */
public final class Main {
    private Main() { }
    public static void main(String[] args) throws Exception {
        String dir = System.getenv().getOrDefault("DATA_DIR", "data"), firebase = System.getenv("FIREBASE_URL");
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
        Path file = Paths.get(dir, "db.json");
        Repository repo = firebase == null || firebase.isBlank() ? new FileRepository(file) : new FirebaseRepository(firebase, new FileRepository(file));
        System.out.println(repo instanceof FirebaseRepository ? "Storage: Firebase Realtime Database (with a local backup)" : "Storage: local file " + file.toAbsolutePath());
        int actual = new SulitBitesWebApp(new Store(repo)).start(port);
        System.out.println("Sulit Bites is running at http://localhost:" + actual);
    }
}
