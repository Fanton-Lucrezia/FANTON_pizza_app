import com.google.gson.Gson;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
           /* saveToFile(pizze); //aggiorna il file
            catch(IOException e) {
                System.out.println("Errore");
                Pizza[] backup = loadFromFile();
                for(Pizza p : backup){
                    System.out.println(p);
                }
            }*/
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
