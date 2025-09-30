public class Pizza {
    public String nome;
    public String ingredienti;
    public double prezzo;


    @Override
    public String toString() {
        return "Nome: " + nome + "\nIngredianti: " + ingredienti + "\nPrezzo: " + prezzo + "\n";
    }
}
