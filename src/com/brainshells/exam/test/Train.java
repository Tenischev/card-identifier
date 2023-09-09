package com.brainshells.exam.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

record ValuesMapping(List<Double> values, String letter){
    public Double distanceMinkowski(List<Double> parts, double p) {
        double sum = 0;
        for (int i = 0; i < parts.size(); i++) {
            sum += Math.pow(parts.get(i) - values.get(i), p);
        }
        return Math.pow(sum, 1.0 / p);
    }
}

public class Train {

    private static final Path TRAIN_DIR = Path.of("imgs_marked");

    private final Set<ValuesMapping> nameVectors = new HashSet<>();
    private final Set<ValuesMapping> typeVectors = new HashSet<>();

    public void run() {
        try (var files = Files.walk(TRAIN_DIR)) {
            var images = files.filter(Files::isRegularFile).toList();
            for (Path imagePath : images) {
                try (var is = Files.newInputStream(imagePath)) {
                    var fileName = imagePath.getFileName().toString();
                    var cards = CardUtils.getCardImages(ImageIO.read(is));
                    int ind = 0, cardId = 0;
                    while (fileName.charAt(ind) != '.') {
                        StringBuilder nameBuilder = new StringBuilder();
                        while (fileName.charAt(ind) != 'c' && fileName.charAt(ind) != 'd' &&
                                fileName.charAt(ind) != 'h' && fileName.charAt(ind) != 's') {
                            nameBuilder.append(fileName.charAt(ind++));
                        }
                        String type = String.valueOf(fileName.charAt(ind++));
                        addCard(nameBuilder.toString(), type, cards.get(cardId++));
                    }
                }
            }
        } catch (IOException e) {
            System.err.printf("Couldn't read train directory%n%s", e.getMessage());
        }
    }

    public Set<ValuesMapping> getNameVectors() {
        return nameVectors;
    }

    public Set<ValuesMapping> getTypeVectors() {
        return typeVectors;
    }

    private void addCard(String name, String type, BufferedImage card) {
        var nameVector = CardUtils.calcCardNameVector(card);
        var typeVector = CardUtils.calcCardTypeVector(card);
        nameVectors.add(new ValuesMapping(nameVector, name));
        typeVectors.add(new ValuesMapping(typeVector, type));
    }
}
