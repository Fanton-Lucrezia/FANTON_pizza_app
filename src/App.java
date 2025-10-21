import com.google.gson.Gson;
import de.vandermeer.asciitable.AsciiTable;
import okhttp3.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class App {
    public OkHttpClient client;
    public Gson gson;
    public String url;
    public String nomeFile;
    public MediaType JSON;

    public App() {
        client = new OkHttpClient();
        gson = new Gson();
        url = "https://crudcrud.com/api/7526064be3c14395a6b35ff3be7e578e";
        nomeFile = "pizze.json";
        JSON = MediaType.get("application/json; charset=utf-8");
    }

    public void menu() {
        Scanner sc = new Scanner(System.in);
        boolean running= true;
        while (running) {

            System.out.println("\n    MENU PIZZERIA    ");
            System.out.println("1. Visualizza menù");
            System.out.println("2. Visualizza singola pizza");
            System.out.println("3. Crea pizza");
            System.out.println("4. Aggiorna pizza");
            System.out.println("5. Elimina Pizza");

            System.out.println("0. Esci");
            System.out.print("Scegli il n.: ");

            int operation = -1;
            try {
                operation = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("Inserisci un numero");
                sc.nextLine();
                continue;
            }
            //int operation = sc.nextInt();

            if (operation < 0 || operation > 5) {
                System.out.println("Sbagliato riprova");
                continue;
            }
            System.out.println("Hai selezionato: " + operation);
            switch (operation) {
                case 1:
                    try {
                        Pizza[] pizze = getAllPizze();
                        AsciiTable asciiTable = new AsciiTable();
                        asciiTable.addRule();
                        asciiTable.addRow("Nome", "Ingredienti", "Prezzo", "ID");
                        asciiTable.addRule();
                        for (Pizza pizza : pizze) {
                            asciiTable.addRow(pizza.nome, pizza.ingredienti, pizza.prezzo, pizza._id);
                            asciiTable.addRule();
                        }
                        System.out.println(asciiTable.render());
                    } catch (Exception e) {
                        System.out.println("Errore su get: " + e.getClass());
                    }
                    break;
                case 2:
                    try {
                        System.out.print("Inserisci l'ID: ");
                        String op = sc.nextLine();
                        Pizza pizza = getSinglePizza(op);
                        AsciiTable asciiTable = new AsciiTable();
                        asciiTable.addRule();
                        asciiTable.addRow("Nome", "Ingredienti", "Prezzo", "ID");
                        asciiTable.addRule();
                        asciiTable.addRow(pizza.nome, pizza.ingredienti, pizza.prezzo, pizza._id);
                        asciiTable.addRule();
                        System.out.println(asciiTable.render());

                    } catch (Exception e) {
                        System.out.println("Inserisci un ID alphanumerico");
                        sc.nextLine();
                        continue;
                    }
                    //int operation = sc.nextInt();
                    break;
                case 3:
                    try {
                        System.out.print("Nome pizza: ");
                        String nome = sc.nextLine();
                        System.out.print("Ingredienti: ");
                        String ingredienti = sc.nextLine();
                        System.out.print("Prezzo (es. 7.5): ");
                        String prezzoInput = sc.nextLine();
                        Double prezzo;
                        try {
                            prezzo = Double.parseDouble(prezzoInput);
                        } catch (NumberFormatException e) {
                            System.out.println("Prezzo non valido. Operazione annullata.");
                            break;
                        }
                        createPizza(nome, ingredienti, prezzo);
                    } catch (Exception e) {
                        System.out.println("Errore durante la creazione: " + e.getMessage());
                    }
                    break;
                case 4:
                    try {
                        System.out.println("Inserire l'ID della pizza da aggiornare: ");
                        String id= sc.nextLine();
                        updatePizza(id);
                    } catch (Exception e) {
                        System.out.println("Errore: " + e.getMessage());
                    }
                    break;
                case 5:
                    try {
                        System.out.println("Inserire l'ID della pizza da eliminare: ");
                        String id= sc.nextLine();
                        deletePizza(id);
                    } catch (Exception e) {
                        System.out.println("Errore: " + e.getMessage());
                    }
                    break;
                case 0:
                    running = false;
                    System.out.println("Arrivederci e alla prossima!");
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    public Pizza[] getAllPizze() {
        Request request = new Request.Builder()
                .url(url + "/pizze/")
                .build();
        Pizza[] pizze = null;

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Errore get: " + response);
            //DESERIALIZZA IL JSON DEL BODY
            //Gson gson = new Gson();
            pizze = gson.fromJson(response.body().string(), Pizza[].class);
            if (pizze == null || pizze.length == 0) {
                System.out.println("Nessuna pizza sul server remoto");
            } else {
                for (Pizza p : pizze) {
                    System.out.println(p);
                }
            }
            //System.out.println(response.body().string());
            //saveToFile(pizze); //aggiorna il file

        } catch (IOException e) {
            System.out.println("Errore" + e.getMessage());
        }
        return pizze;
    }

    public Pizza getSinglePizza(String id) {
        Request request = new Request.Builder()
                .url(url + "/pizze/"+id)
                .build();
        Pizza pizza = null;

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Errore get: " + response);
            //DESERIALIZZA IL JSON DEL BODY
            //Gson gson = new Gson();
            pizza = gson.fromJson(response.body().string(), Pizza.class);
            if (pizza == null ) {
                System.out.println("Nessuna pizza sul server remoto");
            } else {

                System.out.println(pizza);
            }
            //System.out.println(response.body().string());

        } catch (IOException e) {
            System.out.println("Errore" + e.getMessage());
        }
        return pizza;
    }

    public void createPizza(String nome, String ingredienti, Double prezzo) {
        Pizza pizza = new Pizza(nome, ingredienti, prezzo);
        //JSON
        String json = gson.toJson(pizza);

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url + "/pizze/")
                .post(body)
                .build();
        //MediaType JSON = MediaType.get("application/json; charset=utf-8");
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Errore: " + response);
                return;
            }
            //DESERIALIZZA IL JSON DEL BODY
            Pizza nuovaPizza = gson.fromJson(response.body().string(), Pizza.class);
            System.out.println("Nuova pizza creata: " +  nuovaPizza);
        }
        catch (IOException e){
            System.out.println("Errore" + e.getMessage());
        }
    }


    public void updatePizza(String id) {
        Pizza unupdatedPizza = getSinglePizza(id);
        if (unupdatedPizza == null) {
            System.out.println("Non esiste nessuna pizza con questo id");
            return;
        }
        Scanner sc = new Scanner(System.in);
        System.out.println("Modifica " + unupdatedPizza.nome + " in: ");
        String nomeNew = sc.nextLine();
        String nome = nomeNew.isEmpty() ? unupdatedPizza.nome : nomeNew;
        System.out.println("Modifica " + unupdatedPizza.ingredienti + " in: ");
        String ingredientiNew = sc.nextLine();
        String ingredienti = ingredientiNew.isEmpty() ? unupdatedPizza.ingredienti : ingredientiNew;
        System.out.println("Modifica " + unupdatedPizza.prezzo + " in: ");
        String prezzoNew = sc.nextLine(); //line perchè con double non potrei usare isEmpty
        Double prezzo = prezzoNew.isEmpty() ? unupdatedPizza.prezzo : Double.parseDouble(prezzoNew);

        //similea crea pizza
        Pizza updatedPizza = new Pizza(nome, ingredienti, prezzo);
        String json = gson.toJson(updatedPizza);

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url + "/pizze/" +id)
                .put(body)
                .build();
        //MediaType JSON = MediaType.get("application/json; charset=utf-8");
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Errore1: " + response);
                return;
            }
            //DESERIALIZZA IL JSON DEL BODY
            System.out.println("Pizza aggiornata");
        }
        catch (IOException e){
            System.out.println("Errore2" + e.getMessage());
        }

    }

    public void deletePizza(String id) {
        Request request = new Request.Builder()
                .url(url + "/pizze/" +id)
                .delete()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Errore1: " + response);
                return;
            }
            //DESERIALIZZA IL JSON DEL BODY
            System.out.println("Pizza eliminata");
        }
        catch (IOException e){
            System.out.println("Errore2" + e.getMessage());
        }
    }

    public void testSqlite(){
        String DB_URL = "jdbc:sqlite:pizza.db";

        try {
            java.sql.Connection conn = DriverManager.getConnection(DB_URL);
            if(conn != null){
                System.out.println("Connessione al DB Sqlite avvenuta con successo!");
            }

            //""" per non dover concatenare
            String sqlCreateTable = """    
                CREATE TABLE IF NOT EXISTS pizza (
                    id VARCHAR(50) PRIMARY KEY,
                    nome VARCHAR(50),
                    ingredienti VARCHAR(20),
                    prezzo DOUBLE);
                """;

            Statement statement = conn.createStatement();
            statement.executeUpdate(sqlCreateTable);
            System.out.println("La tabella pizze è stata creata con successo");

            String sqlIsert= " INSERT INTO pizza VALUES(?,?,?,?); ";

            PreparedStatement preparedStatement = conn.prepareStatement(sqlIsert);
            preparedStatement.setString(1, "abcdef");
            preparedStatement.setString(2, "Margherta");
            preparedStatement.setString(3, "Mozzarella, Pomodoro");
            preparedStatement.setDouble(4, 6);
            preparedStatement.execute();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    public void run() {
        testSqlite();
        return;
        //menu();
    }
}