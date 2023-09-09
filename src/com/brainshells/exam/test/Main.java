package com.brainshells.exam.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final int K = 5;
    private final Path imageDirectory;
    private final List<MeanMapping> nameMap;
    private final List<MeanMapping> typeMap;

    public Main(Path directory) {
        imageDirectory = directory;
        var train = new Train();
        train.run();
        this.nameMap = train.getNameMap();
        this.typeMap = train.getTypeMap();
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
        int filesCount = 0, errors = 0;
        try (var files = Files.walk(imageDirectory)) {
            var images = files.filter(Files::isRegularFile).toList();
            for (Path imagePath : images) {
                try (var is = Files.newInputStream(imagePath)) {
                    var cardsInImage = imageTranscription(ImageIO.read(is));
                    System.out.printf("%s - %s%n", imagePath.getFileName(), cardsInImage);
                    filesCount++;
                    if (!imagePath.getFileName().toString().startsWith(cardsInImage)) {
                        errors++;
                    }
                }
            }
            System.out.printf("Files %d, Errors %d, Rate %f%n", filesCount, errors, (errors * 100.0) / filesCount);
        } catch (IOException e) {
            System.err.printf("Couldn't read target directory%n%s", e.getMessage());
        }
    }

    private String imageTranscription(BufferedImage image) {
        return Stream.of(image)
              .map(CardUtils::takeCardsZone)
              .map(CardUtils::splitToCards)
              .flatMap(Collection::stream)
              .filter(CardUtils::cardIsValid)
              .map(CardUtils::convertToBlackAndWhite)
              .<String>mapMulti((card, consumer) -> {
                  consumer.accept(getCardName(card));
                  consumer.accept(getCardType(card));
              }).collect(Collectors.joining());

    }

    private String getCardName(BufferedImage card) {
        var hash = CardUtils.getCardNameHash(card);
        return findKNearest(hash, nameMap);
    }

    private String getCardType(BufferedImage card) {
        var hash = CardUtils.getCardTypeHash(card);
        return findKNearest(hash, typeMap);
    }


    private String findKNearest(List<Double> hash, List<MeanMapping> map) {
        TreeMap<Double, String> kNear = new TreeMap<>();
        String ans = "";
        for (MeanMapping e : map) {
            kNear.put(e.distanceMincowski(hash, 2.0), e.value());
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
