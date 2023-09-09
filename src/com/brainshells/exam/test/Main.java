package com.brainshells.exam.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Main {

    private static final int K = 5; // The K for KNN
    private final Path imageDirectory;
    private final Set<ValuesMapping> nameSet;
    private final Set<ValuesMapping> typeSet;

    public Main(Path directory) {
        imageDirectory = directory;
        // start training
        var train = new Train();
        train.run();
        this.nameSet = train.getNameVectors();
        this.typeSet = train.getTypeVectors();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please provide path to the directory as an argument");
        } else {
            Path imageSource = Path.of(args[0]);
            if (Files.isDirectory(imageSource)) {
                new Main(imageSource).run();
            } else {
                System.err.printf("%s is not a directory%n", args[0]);
            }
        }
    }

    private void run() {
//        int filesCount = 0, errors = 0;
        try (var files = Files.walk(imageDirectory)) {
            var images = files.filter(Files::isRegularFile).toList();
            for (Path imagePath : images) {
                try (var is = Files.newInputStream(imagePath)) {
                    var cardsInImage = imageTranscription(ImageIO.read(is));
                    System.out.printf("%s - %s%n", imagePath.getFileName(), cardsInImage);
//                    filesCount++;
//                    if (!imagePath.getFileName().toString().startsWith(cardsInImage)) {
//                        errors++;
//                    }
                }
            }
//            System.out.printf("Files %d, Errors %d, Rate %f%n", filesCount, errors, (errors * 100.0) / filesCount);
        } catch (IOException e) {
            System.err.printf("Couldn't read target directory%n%s", e.getMessage());
        }
    }

    private String imageTranscription(BufferedImage image) {
        return CardUtils.getCardImages(image).stream()
              .<String>mapMulti((card, consumer) -> {
                  consumer.accept(getCardName(card));
                  consumer.accept(getCardType(card));
              }).collect(Collectors.joining());

    }

    private String getCardName(BufferedImage card) {
        var values = CardUtils.calcCardNameVector(card);
        return findByKNearest(values, nameSet);
    }

    private String getCardType(BufferedImage card) {
        var values = CardUtils.calcCardTypeVector(card);
        return findByKNearest(values, typeSet);
    }

    private String findByKNearest(List<Double> values, Set<ValuesMapping> valueSet) {
        TreeMap<Double, String> kNear = new TreeMap<>();
        String ans = "";
        for (ValuesMapping e : valueSet) {
            kNear.put(e.distanceMinkowski(values, 2.0), e.letter());
            if (kNear.size() > K) {
                kNear.pollLastEntry();
            }
        }
        Map<String, Integer> counter = new HashMap<>();
        int max = 0;
        while (kNear.size() > 0) {
            var k = kNear.pollFirstEntry().getValue();
            counter.put(k, 1 + (counter.get(k) == null ? 0 : counter.get(k)));
            if (max < counter.get(k)) {
                max = counter.get(k);
                ans = k;
            }
        }
        return ans;
    }
}
