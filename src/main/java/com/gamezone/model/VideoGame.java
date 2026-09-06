package com.gamezone.model;

/**
 * Videojuego de la tienda.
 */
public class VideoGame extends Product {
    private String platform;
    private String genre;
    private String ageRating;

    /**
     * Crea un videojuego nuevo.
     * @param id id
     * @param title title
     * @param price price
     * @param stock stock
     * @param platform platform
     * @param genre genre
     * @param ageRating age rating
     */
    public VideoGame(String id, String title, double price, int stock,
                     String platform, String genre, String ageRating) {
        super(id, title, price, stock);
        this.platform = platform;
        this.genre = genre;
        this.ageRating = ageRating;
    }

    /** @return platform */
    public String getPlatform() { return platform; }
    /** @return genre */
    public String getGenre() { return genre; }
    /** @return age rating */
    public String getAgeRating() { return ageRating; }

    /**
     * Full game description.
     * @return description
     */
    @Override
    public String getFullDescription() {
        return getId() + " | " + getTitle() + " | $" + getPrice()
            + " | stock:" + getStock() + " | " + platform + " / " + genre + " / " + ageRating;
    }
}
