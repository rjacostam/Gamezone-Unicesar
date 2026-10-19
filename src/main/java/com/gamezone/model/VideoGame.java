package com.gamezone.model;

import com.gamezone.format.AmountFormat;

import java.util.Arrays;
import java.util.List;

/**
 * Video game sold by the store. On top of the data inherited from Product it
 * keeps the platform it runs on, its genre and its recommended age rating.
 */
public class VideoGame extends Product implements ProductCatalogRow {
    private String platform;
    private String genre;
    private String ageRating;

    /**
     * Creates a video game with its own attributes.
     * @param id product identifier
     * @param title product title
     * @param price unit price
     * @param stock available stock
     * @param platform platform the game runs on
     * @param genre genre of the game
     * @param ageRating recommended age rating
     */
    public VideoGame(String id, String title, double price, int stock,
                     String platform, String genre, String ageRating) {
        super(id, title, price, stock);
        this.platform = platform;
        this.genre = genre;
        this.ageRating = ageRating;
    }

    /**
     * Builds the description of this video game with its own attributes next to
     * the common data. The price is shown formatted with thousands separators
     * and two decimals so every product line looks the same in the menu.
     * @return description ready to be shown to the user
     */
    @Override
    public String getDescription() {
        return "Videojuego [" + getId() + "] " + getTitle()
                + " | Plataforma: " + platform
                + " | Genero: " + genre
                + " | Edad: " + ageRating
                + " | Precio: " + AmountFormat.moneyInComputerLocale(getPrice())
                + " | Stock: " + getStock();
    }

    /**
     * Returns the category of this product, so a promotion can target video
     * games only.
     * @return VIDEOGAME
     */
    @Override
    public String getCategory() {
        return "VIDEOGAME";
    }

    /**
     * Returns the three columns that only a video game has, in the order the
     * catalogue file expects them.
     * @return platform, genre and age rating of this game
     */
    @Override
    public List<String> getExtraFields() {
        return Arrays.asList(platform, genre, ageRating);
    }

    /**
     * Returns the platform this game runs on.
     * @return platform of the game
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Changes the platform this game runs on.
     * @param platform new platform
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Returns the genre of this game.
     * @return genre of the game
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Changes the genre of this game.
     * @param genre new genre
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Returns the recommended age rating of this game.
     * @return age rating of the game
     */
    public String getAgeRating() {
        return ageRating;
    }

    /**
     * Changes the recommended age rating of this game.
     * @param ageRating new age rating
     */
    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }
}
