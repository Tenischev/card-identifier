package com.brainshells.exam.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

record MeanMapping(List<Double> mean, String value){
    public Double distanceMincowski(List<Double> parts, double p) {
        double sum = 0;
        for (int i = 0; i < parts.size(); i++) {
            sum += Math.pow(parts.get(i) - mean.get(i), p);
        }
        return Math.pow(sum, 1.0 / p);
    }
}

public class Train {

    private static final Path TRAIN_DIR = Path.of("imgs_marked");

    private final Map<String, Set<List<Double>>> nameMeans = new HashMap<>();
    private final Map<String, Set<List<Double>>> typeMeans = new HashMap<>();

    public void run() {
        try (var files = Files.walk(TRAIN_DIR)) {
            var images = files.filter(Files::isRegularFile).toList();
            for (Path imagePath : images) {
                try (var is = Files.newInputStream(imagePath)) {
                    var fileName = imagePath.getFileName().toString();
                    var cards = getCardImages(ImageIO.read(is));
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
                } catch (Exception e) {
                    System.err.println(imagePath.getFileName());
                }
            }
        } catch (IOException e) {
            System.err.printf("Couldn't read target directory%n%s", e.getMessage());
        }
    }

    public List<MeanMapping> getNameMap() {
        List<MeanMapping> mapping = new ArrayList<>();
        nameMeans.forEach((key, value) -> value.forEach(meanParts -> mapping.add(new MeanMapping(meanParts, key))));
        return mapping;
    }

    public List<MeanMapping> getTypeMap() {
        List<MeanMapping> mapping = new ArrayList<>();
        typeMeans.forEach((key, value) -> value.forEach(meanParts -> mapping.add(new MeanMapping(meanParts, key))));
        return mapping;
    }

    private void addCard(String name, String type, BufferedImage card) {
        var nameHash = CardUtils.getCardNameHash(card);
        var typeHash = CardUtils.getCardTypeHash(card);
        if (!nameMeans.containsKey(name)) {
            nameMeans.put(name, new HashSet<>());
        }
        nameMeans.get(name).add(nameHash);
        if (!typeMeans.containsKey(type)) {
            typeMeans.put(type, new HashSet<>());
        }
        typeMeans.get(type).add(typeHash);
    }

    private List<BufferedImage> getCardImages(BufferedImage image) {
        return Stream.of(image)
                     .map(CardUtils::takeCardsZone)
                     .map(CardUtils::splitToCards)
                     .flatMap(Collection::stream)
                     .filter(CardUtils::cardIsValid)
                     .map(CardUtils::convertToBlackAndWhite)
                     .toList();
    }
}
