public class Pizza {
    public String nome;
    public String ingredienti;
    public double prezzo;
    public String _id;

    public Pizza(String nome, String ingredienti, double prezzo) {
        this.nome = nome;
        this.ingredienti = ingredienti;
        this.prezzo = prezzo;
    }


    @Override
    public String toString() {
        return "Nome: " + nome + "\nIngredianti: " + ingredienti + "\nPrezzo: " + prezzo + "\nID: " + _id;
    }

}
