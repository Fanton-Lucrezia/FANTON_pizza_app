import com.google.gson.Gson;
import okhttp3.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class App
{
    public OkHttpClient client;
    public Gson gson;
    public String nomeFile;
    public MediaType JSON;

    public App(){
        client = new OkHttpClient();
        gson = new Gson();
        nomeFile = "pizze.json";
        JSON = MediaType.parse("application/json; charset=utf-8");
    }

    public void doGet() throws IOException{
        Request request = new Request.Builder()
                .url("https://crudcrud.com/api/5dfb54bea3524604af943f92764499b2/pizze/")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Errore get: " + response);

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
            System.out.println("Errore" + e.getMessage());
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

    public void doPost(Pizza pizza) throws IOException {
        String json = gson.toJson(pizza);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url("https://crudcrud.com/api/5dfb54bea3524604af943f92764499b2/pizze/")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful())
                throw new IOException("Errore post: " + response.code() + (respBody.isEmpty() ? "" : " - " + respBody));
            System.out.println("Pizza aggiunta sul server remoto.");
            //aggiorno backup locale (fetchRemotePizze fa una nuova richiesta)
            try {
                Pizza[] pizze = fetchRemotePizze();
                saveToFile(pizze);
            } catch (IOException e) {
                System.out.println("Errore. Impossibile aggiornare il backup remoto dopo POST: " + e.getMessage());
            }
        }
    }

    public void doDelete(String id) throws IOException {
        Request request = new Request.Builder()
                .url("https://crudcrud.com/api/5dfb54bea3524604af943f92764499b2/pizze/" + id)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) throw new IOException("Errore DELETE: " + response.code() + (respBody.isEmpty() ? "" : " - " + respBody));
            System.out.println("Pizza eliminata dal server remoto.");
            try {
                Pizza[] pizze = fetchRemotePizze();
                saveToFile(pizze);
            } catch (IOException e) {
                System.out.println("Impossibile aggiornare il backup dopo DELETE: " + e.getMessage());
            }
        }
    }

    //restituisce l'array di pizze dal server
    private Pizza[] fetchRemotePizze() throws IOException {
        Request request = new Request.Builder()
                .url("https://crudcrud.com/api/5dfb54bea3524604af943f92764499b2/pizze/")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String bodyStr = response.body().string();
            Pizza[] pizze = gson.fromJson(bodyStr, Pizza[].class);
            return pizze != null ? pizze : new Pizza[0];
        }
    }
    //array di pizze su file JSON locale
    public void saveToFile(Pizza[] pizze) {
        try (FileWriter writer = new FileWriter(nomeFile)) {
            gson.toJson(pizze, writer);
            System.out.println("Backup salvato su " + nomeFile);
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio del backup: " + e.getMessage());
        }
    }

    //carica dal file JSON locale (array vuoto se non esiste)
    public Pizza[] loadFromFile() {
        try (FileReader reader = new FileReader(nomeFile)) {
            Pizza[] pizze = gson.fromJson(reader, Pizza[].class);
            System.out.println("Dati caricati dal backup locale (" + nomeFile + ").");
            return pizze != null ? pizze : new Pizza[0];
        } catch (IOException e) {
            //array vuoto se file non trovato o ci sono errori
            return new Pizza[0];
        }
    }

    public void run()
    {
        /*//System.out.printf("Ciao");
        try {
            doGet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(a);
        */
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n    MENU PIZZERIA    ");
            System.out.println("1. Visualizza menù");
            System.out.println("2. Nuova pizza");
            System.out.println("3. Elimina Pizza");

            System.out.println("0. Esci");
            System.out.print("Scegli il n.: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    try {
                        doGet();
                    } catch (IOException e) {
                        System.out.println("Errore durante la visualizzazione del menù: " + e.getMessage());
                    }
                    break;

                case "2":
                    try {
                        Pizza pizza = new Pizza();
                        System.out.print("Nome: ");
                        pizza.nome = scanner.nextLine();
                        System.out.print("Ingredienti (separati da virgola): ");
                        pizza.ingredienti = scanner.nextLine();
                        System.out.print("Prezzo (es. 6.5): ");
                        try {
                            pizza.prezzo = Double.parseDouble(scanner.nextLine());
                        } catch (NumberFormatException nfe) {
                            System.out.println("Prezzo non valido. Operazione annullata.");
                            break;
                        }

                        //provo a inviare al server remoto; se fallisce, salvo solo sul backup locale
                        try {
                            doPost(pizza);
                        } catch (IOException e) {
                            System.out.println("Impossibile salvare su server remoto: " + e.getMessage());
                            // salvo localmente: prendo backup esistente e aggiungo la pizza
                            Pizza[] existing = loadFromFile();
                            Pizza[] newArr = Arrays.copyOf(existing, existing.length + 1);
                            newArr[newArr.length - 1] = pizza;
                            saveToFile(newArr);
                            System.out.println("Pizza salvata solo nel backup locale.");
                        }

                    } catch (Exception e) {
                        System.out.println("Errore durante l'inserimento della pizza: " + e.getMessage());
                    }
                    break;

                case "3":
                    try {
                        //provo prima a prendere la lista remota
                        Pizza[] pizze;
                        try {
                            pizze = fetchRemotePizze();
                        } catch (IOException e) {
                            System.out.println("Impossibile contattare il server remoto, userò il backup locale.");
                            pizze = loadFromFile();
                        }

                        if (pizze.length == 0) {
                            System.out.println("Nessuna pizza trovata.");
                            break;
                        }

                        //mostro lista numerata
                        for (int i = 0; i < pizze.length; i++) {
                            System.out.println((i + 1) + " - " + pizze[i].nome + " (" + pizze[i].prezzo + "€)");
                        }
                        System.out.print("Seleziona il numero della pizza da eliminare: ");
                        int index;
                        try {
                            index = Integer.parseInt(scanner.nextLine()) - 1;
                        } catch (NumberFormatException nfe) {
                            System.out.println("Input non valido.");
                            break;
                        }

                        if (index < 0 || index >= pizze.length) {
                            System.out.println("Indice non valido.");
                            break;
                        }

                        String id = pizze[index].ID;
                        if (id != null && !id.isEmpty()) {
                            //provo a eliminare dal server remoto
                            try {
                                doDelete(id);
                            } catch (IOException e) {
                                System.out.println("Impossibile eliminare dal server remoto: " + e.getMessage());
                                //rimuovo dal backup locale
                                Pizza[] backup = loadFromFile();
                                if (backup.length > index) {
                                    Pizza[] newArr = new Pizza[backup.length - 1];
                                    int k = 0;
                                    for (int j = 0; j < backup.length; j++) {
                                        if (j == index) continue;
                                        newArr[k++] = backup[j];
                                    }
                                    saveToFile(newArr);
                                    System.out.println("Pizza rimossa dal backup locale.");
                                } else {
                                    System.out.println("Impossibile rimuovere dal backup (indice fuori range).");
                                }
                            }
                        } else {
                            //se non abbiamo id (pizze provenienti dal backup locale potrebbero non avere ID),
                            //rimuoviamo la pizza solo dal backup locale
                            Pizza[] backup = loadFromFile();
                            if (backup.length > index) {
                                Pizza[] newArr = new Pizza[backup.length - 1];
                                int k = 0;
                                for (int j = 0; j < backup.length; j++) {
                                    if (j == index) continue;
                                    newArr[k++] = backup[j];
                                }
                                saveToFile(newArr);
                                System.out.println("Pizza rimossa dal backup locale (non era presente _id remoto).");
                            } else {
                                System.out.println("Impossibile rimuovere: indice fuori range nel backup.");
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("Errore durante l'eliminazione: " + e.getMessage());
                    }
                    break;

                case "0":
                    running = false;
                    System.out.println("Arrivederci!");
                    break;

                default:
                    System.out.println("Scelta non valida.");
            }
        }

        scanner.close();
    }
}
