package net.tfminecraft.cooking.husbandry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HusbandryRepositoryTest {

    @Test
    void batchUpsertInsertsAndUpdates(@TempDir Path tempDir) {
        File dbFile = tempDir.resolve("husbandry.db").toFile();
        HusbandryRepository repository = HusbandryRepository.open(dbFile);
        try {
            HusbandryAnimal first = animal(UUID.randomUUID(), "COW", "Bess", 12);
            HusbandryAnimal second = animal(UUID.randomUUID(), "SHEEP", "Wool", 4);
            HusbandryAnimal third = animal(UUID.randomUUID(), "PIG", "Ham", 8);
            repository.upsertAnimals(List.of(first, second, third));

            assertEquals(12, repository.getAnimal(first.uuid()).orElseThrow().care());
            assertEquals("Wool", repository.getAnimal(second.uuid()).orElseThrow().name());
            assertEquals("PIG", repository.getAnimal(third.uuid()).orElseThrow().type());

            first.setCare(40);
            first.setGenetics(250);
            first.setHungrySince(1_700_000_000_000L);
            first.setCareUpRemainderSeconds(123);
            first.setCareDownRemainderSeconds(45);
            repository.upsertAnimals(Arrays.asList(first, null));

            HusbandryAnimal reloaded = repository.getAnimal(first.uuid()).orElseThrow();
            assertEquals(40, reloaded.care());
            assertEquals(250, reloaded.genetics());
            assertEquals(1_700_000_000_000L, reloaded.hungrySince());
            assertEquals(123, reloaded.careUpRemainderSeconds());
            assertEquals(45, reloaded.careDownRemainderSeconds());
            assertEquals(4, repository.getAnimal(second.uuid()).orElseThrow().care());
            assertTrue(repository.exists(third.uuid()));
        } finally {
            repository.close();
        }
    }

    @Test
    void missingRevisionResetsToWildAndStamps(@TempDir Path tempDir) {
        File dbFile = tempDir.resolve("husbandry.db").toFile();
        HusbandryRepository repository = HusbandryRepository.open(dbFile);
        try {
            HusbandryAnimal animal = animal(UUID.randomUUID(), "SHEEP", "Bess", 40);
            animal.setGenetics(900);
            animal.setNeutered(true);
            animal.setMatureAt(12_345L);
            animal.setCareUpRemainderSeconds(9);
            animal.setCareDownRemainderSeconds(3);
            repository.upsertAnimal(animal);

            Random random = new Random(1L);
            int expectedGenes = new Random(1L).nextInt(HusbandryConfig.initialGeneticMax() + 1);
            int reset = repository.resetStaleStats("1", random);
            assertEquals(1, reset);

            HusbandryAnimal reloaded = repository.getAnimal(animal.uuid()).orElseThrow();
            assertEquals(expectedGenes, reloaded.genetics());
            assertEquals(0, reloaded.care());
            assertEquals(0, reloaded.careUpRemainderSeconds());
            assertEquals(0, reloaded.careDownRemainderSeconds());
            assertEquals("1", reloaded.statsRevision());
            assertEquals("Bess", reloaded.name());
            assertTrue(reloaded.neutered());
            assertEquals(12_345L, reloaded.matureAt());
        } finally {
            repository.close();
        }
    }

    @Test
    void matchingRevisionIsUnchanged(@TempDir Path tempDir) {
        File dbFile = tempDir.resolve("husbandry.db").toFile();
        HusbandryRepository repository = HusbandryRepository.open(dbFile);
        try {
            HusbandryAnimal animal = animal(UUID.randomUUID(), "COW", "Bess", 40);
            animal.setGenetics(250);
            animal.setStatsRevision("1");
            repository.upsertAnimal(animal);

            int reset = repository.resetStaleStats("1", new Random(1L));
            assertEquals(0, reset);

            HusbandryAnimal reloaded = repository.getAnimal(animal.uuid()).orElseThrow();
            assertEquals(250, reloaded.genetics());
            assertEquals(40, reloaded.care());
            assertEquals("1", reloaded.statsRevision());
        } finally {
            repository.close();
        }
    }

    @Test
    void bumpedRevisionResetsMatchingRows(@TempDir Path tempDir) {
        File dbFile = tempDir.resolve("husbandry.db").toFile();
        HusbandryRepository repository = HusbandryRepository.open(dbFile);
        try {
            HusbandryAnimal animal = animal(UUID.randomUUID(), "PIG", "Ham", 80);
            animal.setGenetics(700);
            animal.setStatsRevision("1");
            repository.upsertAnimal(animal);

            Random random = new Random(2L);
            int expectedGenes = new Random(2L).nextInt(HusbandryConfig.initialGeneticMax() + 1);
            int reset = repository.resetStaleStats("2", random);
            assertEquals(1, reset);

            HusbandryAnimal reloaded = repository.getAnimal(animal.uuid()).orElseThrow();
            assertEquals(expectedGenes, reloaded.genetics());
            assertEquals(0, reloaded.care());
            assertEquals("2", reloaded.statsRevision());
            assertEquals("Ham", reloaded.name());
        } finally {
            repository.close();
        }
    }

    private static HusbandryAnimal animal(UUID uuid, String type, String name, int care) {
        HusbandryAnimal animal = new HusbandryAnimal(uuid, type, name);
        animal.setCare(care);
        animal.setGenetics(100);
        animal.setLastProcessedAt(1_234L);
        animal.setLoadedVisitStart(2_000L);
        return animal;
    }
}
