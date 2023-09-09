package com.brainshells.exam.test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CardUtils {

    private static final int CARD_W = 72;
    private static final int MAX_CARDS = 5; // by interface size I assume that it has maximum 5 cards on a desk
    private static final int CARD_ZONE_X = 142;
    private static final int CARD_ZONE_Y = 585;
    private static final int CARD_ZONE_W = CARD_W * MAX_CARDS;
    private static final int CARD_ZONE_H = 675 - CARD_ZONE_Y;
    private static final int CARD_NAME_H = 615 - CARD_ZONE_Y;
    private static final int SQRT_VECTOR_LEN = 3;

    /**
     * Extract images of cards on a desk (from a middle) from the screen. Card images are converted to binary (black and
     * white) color code.
     *
     * @param screen the game screen
     * @return list of card images
     */
    public static List<BufferedImage> getCardImages(BufferedImage screen) {
        return Stream.of(screen)
                     .map(CardUtils::takeCardsZone)
                     .map(CardUtils::splitToCards)
                     .flatMap(Collection::stream)
                     .filter(CardUtils::cardIsValid)
                     .map(CardUtils::convertToBlackAndWhite)
                     .toList();
    }

    /**
     * Calculate vector of values for name section of the card. Name section of the card is top left quad of image
     * bounded by middle of the card and {@value #CARD_NAME_H}.
     *
     * @param card the image of card
     * @return list representing vector values
     */
    public static List<Double> calcCardNameVector(BufferedImage card) {
        var nameZone = card.getSubimage(5, 5, CARD_W / 2, CARD_NAME_H - 5);
        return getMeanVector(nameZone);
    }

    /**
     * Calculate vector of values for type section of the card. Type section of the card is bottom half of image
     * started from {@value #CARD_NAME_H}.
     *
     * @param card the image of card
     * @return list representing vector values
     */
    public static List<Double> calcCardTypeVector(BufferedImage card) {
        var typeZone = card.getSubimage(5, CARD_NAME_H, CARD_W - 17, CARD_ZONE_H - CARD_NAME_H - 5);
        return getMeanVector(typeZone);
    }

    private static BufferedImage takeCardsZone(BufferedImage screen) {
        return screen.getSubimage(CARD_ZONE_X, CARD_ZONE_Y, CARD_ZONE_W, CARD_ZONE_H);
    }

    private static List<BufferedImage> splitToCards(BufferedImage cardsZone) {
        List<BufferedImage> cards = new ArrayList<>();
        for (int i = 0; i < MAX_CARDS; i++) {
            cards.add(cardsZone.getSubimage(CARD_W * i, 0, CARD_W, CARD_ZONE_H));
        }
        return cards;
    }

    private static boolean cardIsValid(BufferedImage card) {
        return card.getRGB(CARD_W * 2 / 3, CARD_ZONE_H / 5) - 0xff787878 >= 0;
    }

    private static BufferedImage convertToBlackAndWhite(BufferedImage image) {
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                var color = image.getRGB(i, j);
                image.setRGB(i, j, color == 0xff787878 || color == 0xffffffff ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
            }
        }
        return image;
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