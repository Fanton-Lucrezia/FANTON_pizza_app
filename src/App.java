import com.google.gson.Gson;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class App
{
    public OkHttpClient client;
    public Gson gson;
    public String nomeFile;

    public App(){
        client = new OkHttpClient();
        gson = new Gson();
        nomeFile = "pizze.json";
    }

    public void doGet() throws IOException{
        Request request = new Request.Builder()
                .url("https://crudcrud.com/api/5dfb54bea3524604af943f92764499b2/pizze/")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            //DESERIALIZZA IL JSON DEL BODY
            //Gson gson = new Gson();
            Pizza[] pizze = gson.fromJson(response.body().string(), Pizza[].class);
            if (pizze == null || pizze.length == 0) {
                System.out.println("Nessuna pizza sul server remoto");
            } else {
                for(Pizza p : pizze){
                    System.out.println(p);
                }
            }

            //System.out.println(response.body().string());
            saveToFile(pizze); //aggiorna il file

        } catch (IOException e) {
            System.out.println("Errore");
            Pizza[] backup = loadFromFile();
            if (backup.length == 0) {
                System.out.println("Nessun dato in backup.");
            } else {
                for (Pizza p : backup) {
                    System.out.println(p);
                }
            }
        }
    }

    //array di pizze su file JSON locale
    public void saveToFile(Pizza[] pizze) {
        try (FileWriter writer = new FileWriter("pizze.json")) {
            gson.toJson(pizze, writer);
            System.out.println("Backup salvato su " + "pizze.json");
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio del backup: " + e.getMessage());
        }
    }

    //carica dal file JSON locale (array vuoto se non esiste)
    public Pizza[] loadFromFile() {
        try (FileReader reader = new FileReader("pizze.json")) {
            Pizza[] pizze = gson.fromJson(reader, Pizza[].class);
            System.out.println("Dati caricati dal backup locale (" + "pizze.json" + ").");
            return pizze != null ? pizze : new Pizza[0];
        } catch (IOException e) {
            //array vuoto se file non trovato o ci sono errori
            return new Pizza[0];
        }
    }

    public void run()
    {
        //System.out.printf("Ciao");
        try {
            doGet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(a);
    }
}
