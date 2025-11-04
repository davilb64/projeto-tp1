import com.github.oscar0812.pokeapi.models.pokemon.Pokemon;

import java.util.Random;

public class Teste {

    public static void main(String[] args) {
        Random random = new Random();
        int limite = 154;
        int int_random = random.nextInt(limite);
        Pokemon pokemon = Pokemon.getById(int_random);
        String sprite = pokemon.getSprites().getFrontDefault();
        System.out.printf(sprite);
    }
}
