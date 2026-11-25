package com.sulitbites;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/** Keeps the data in a Firebase Realtime Database (REST), with a local file as backup.
    The same database layout is used by the earlier JavaScript version, so old data still works. */
public final class FirebaseRepository implements Repository {
    private final String base; private final FileRepository backup;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "firebase-sync"); t.setDaemon(true); return t; });
    private Map<String, String> snapshot = new TreeMap<>();   // what the online database is known to hold
    private volatile boolean synced;                           // pushing is allowed only after a successful download

    public FirebaseRepository(String baseUrl, FileRepository backup) { this.base = baseUrl.trim().replaceAll("/+$", ""); this.backup = backup; }

    @Override public void load(Store store) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(base + "/.json")).timeout(Duration.ofSeconds(15)).GET().build(), HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() != 200) throw new IOException("HTTP " + r.statusCode());
                Object root = Json.parse(r.body().isBlank() ? "null" : r.body());
                Codec.fill(store, root); snapshot = Codec.flat(store); synced = true;
                System.out.println("Loaded data from Firebase: " + snapshot.size() + " records.");
                return;
            } catch (IOException | InterruptedException | RuntimeException e) {
                System.err.println("Firebase load attempt " + attempt + " failed: " + e.getMessage());
                try { Thread.sleep(1500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        System.err.println("WARNING: Firebase is not reachable. Using the local backup and NOT uploading, so online data is never overwritten.");
        backup.load(store);
    }
    @Override public void save(Store store) {
        backup.save(store);
        if (!synced) return;
        Map<String, String> now = Codec.flat(store);
        worker.submit(() -> push(now));
    }
    private void push(Map<String, String> now) {
        Map<String, Object> patch = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : now.entrySet()) if (!e.getValue().equals(snapshot.get(e.getKey()))) patch.put(e.getKey(), Json.parse(e.getValue()));
        for (String k : snapshot.keySet()) if (!now.containsKey(k)) patch.put(k, null);
        if (patch.isEmpty()) return;
        try {
            HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(base + "/.json")).timeout(Duration.ofSeconds(15))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(Json.write(patch))).build(), HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() / 100 != 2) throw new IOException("HTTP " + r.statusCode());
            snapshot = now;
        } catch (IOException | InterruptedException e) { System.err.println("Firebase upload failed (will retry on the next change): " + e.getMessage()); }
    }
    /** For tests: wait until pending uploads are done. */
    void flush() throws Exception { worker.submit(() -> { }).get(20, TimeUnit.SECONDS); }
}
