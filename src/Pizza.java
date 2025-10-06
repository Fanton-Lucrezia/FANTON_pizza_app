public class Pizza {
    public String nome;
    public String ingredienti;
    public double prezzo;
    public String ID;


    @Override
    public String toString() {
        return "Nome: " + nome + "\nIngredianti: " + ingredienti + "\nPrezzo: " + prezzo + "\n";
    }
}
