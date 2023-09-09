package com.brainshells.exam.test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CardUtils {

    public static final int CARD_W = 72;
    public static final int MAX_CARDS = 5; // by interface size I assume that it has maximum 5 cards on table
    public static final int CARD_ZONE_X = 142;
    public static final int CARD_ZONE_Y = 585;
    public static final int CARD_ZONE_W = CARD_W * MAX_CARDS;
    public static final int CARD_ZONE_H = 675 - CARD_ZONE_Y;
    public static final int CARD_NAME_H = 615 - CARD_ZONE_Y;
    private static final int SQRT_VECTOR_LEN = 3;

    public static BufferedImage takeCardsZone(BufferedImage fullImage) {
        return fullImage.getSubimage(CARD_ZONE_X, CARD_ZONE_Y, CARD_ZONE_W, CARD_ZONE_H);
    }

    public static List<BufferedImage> splitToCards(BufferedImage cardsZone) {
        List<BufferedImage> cards = new ArrayList<>();
        for (int i = 0; i < MAX_CARDS; i++) {
            cards.add(cardsZone.getSubimage(CARD_W * i, 0, CARD_W, CARD_ZONE_H));
        }
        return cards;
    }

    public static boolean cardIsValid(BufferedImage card) {
        // should be white or gray zone for card, otherwise it's a desk
        return card.getRGB(CARD_W * 2 / 3, CARD_ZONE_H / 5) - 0xff787878 >= 0;
    }

    public static BufferedImage convertToBlackAndWhite(BufferedImage nameZone) {
        for (int i = 0; i < nameZone.getWidth(); i++) {
            for (int j = 0; j < nameZone.getHeight(); j++) {
                var color = nameZone.getRGB(i, j);
                nameZone.setRGB(i, j, color == 0xff787878 || color == 0xffffffff ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
            }
        }
        return nameZone;
    }

    public static List<Double> getCardNameHash(BufferedImage card) {
        var nameZone = card.getSubimage(5, 5, CARD_W / 2, CARD_NAME_H - 5);
        return getMeanVector(nameZone);
    }

    public static List<Double> getCardTypeHash(BufferedImage card) {
        var typeZone = card.getSubimage(5, CARD_NAME_H, CARD_W - 17, CARD_ZONE_H - CARD_NAME_H - 5);
        return getMeanVector(typeZone);
    }

    private static List<Double> getMeanVector(BufferedImage image) {
        List<Double> vector = new ArrayList<>();
        var w = image.getWidth() / SQRT_VECTOR_LEN;
        var h = image.getHeight() / SQRT_VECTOR_LEN;
        for (int i = 0; i < SQRT_VECTOR_LEN; i++) {
            for (int j = 0; j < SQRT_VECTOR_LEN; j++) {
                vector.add(getMean(image.getSubimage(i * w, j * h, w, h)));
            }
        }
        return vector;
    }

    private static double getMean(BufferedImage image) {
        double mean = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                mean += image.getRGB(i, j) == Color.BLACK.getRGB() ? 1 : 0;
            }
        }
        mean /= (image.getHeight() * image.getWidth());
        return mean;
    }
}
