package com.brainshells.exam.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Test util class to split original image set to test and train sets
 */
public class SetDivider {

    static Path trainDir = Path.of("train_dir");
    static Path testDir = Path.of("test_dir");
    static Path mainDir = Path.of("imgs_marked");

    public static void main(String[] args) {
        var rand = new Random();
        try (var files = Files.walk(mainDir)) {
            files.forEach(file -> {
                try {
                    if (rand.nextDouble() < 0.75) {
                        Files.copy(file, trainDir.resolve(file.getFileName()));
                    } else {
                        Files.copy(file, testDir.resolve(file.getFileName()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
