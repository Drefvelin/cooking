package net.tfminecraft.cooking.farming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.block.Block;

public final class FarmAreaScanner {

    private FarmAreaScanner() {}

    public static List<Block> findCropBlocks(Block centre, int radius, Collection<Material> cropMaterials) {
        if (centre == null || cropMaterials == null || cropMaterials.isEmpty()) {
            return List.of();
        }
        if (!cropMaterials.contains(centre.getType())) {
            return List.of();
        }
        if (radius <= 0) {
            return List.of(centre);
        }
        return findAdjacentMaterials(cropMaterials, centre, radius, true);
    }

    private static List<Block> findAdjacentMaterials(
            Collection<Material> materials,
            Block centre,
            int radius,
            boolean addCentre) {
        int diameter = radius + radius + 1;
        int centreIndex = gridIndex(diameter, radius, 0, 0);
        Block[] adjacent = new Block[diameter * diameter];
        if (addCentre) {
            adjacent[centreIndex] = centre;
        }

        // +x axis
        {
            Block previous = centre;
            int i = 1;
            while (i <= radius) {
                Block foundAdjacent = locateAdjacentInColumn(materials, centre, i, 0, previous).orElse(null);
                if (foundAdjacent == null) {
                    break;
                }
                adjacent[gridIndex(diameter, radius, i, 0)] = foundAdjacent;
                previous = foundAdjacent;
                i++;
            }
        }
        // -x axis
        {
            Block previous = centre;
            int i = -1;
            while (i >= -radius) {
                Block foundAdjacent = locateAdjacentInColumn(materials, centre, i, 0, previous).orElse(null);
                if (foundAdjacent == null) {
                    break;
                }
                adjacent[gridIndex(diameter, radius, i, 0)] = foundAdjacent;
                previous = foundAdjacent;
                i--;
            }
        }
        // +z axis
        {
            Block previous = centre;
            int k = 1;
            while (k <= radius) {
                Block foundAdjacent = locateAdjacentInColumn(materials, centre, 0, k, previous).orElse(null);
                if (foundAdjacent == null) {
                    break;
                }
                adjacent[gridIndex(diameter, radius, 0, k)] = foundAdjacent;
                previous = foundAdjacent;
                k++;
            }
        }
        // -z axis
        {
            Block previous = centre;
            int k = -1;
            while (k >= -radius) {
                Block foundAdjacent = locateAdjacentInColumn(materials, centre, 0, k, previous).orElse(null);
                if (foundAdjacent == null) {
                    break;
                }
                adjacent[gridIndex(diameter, radius, 0, k)] = foundAdjacent;
                previous = foundAdjacent;
                k--;
            }
        }
        // +x, +z corners
        {
            int i = 1;
            while (i <= radius) {
                int k = 1;
                while (k <= radius) {
                    Block p1 = adjacent[gridIndex(diameter, radius, i - 1, k)];
                    Block p2 = adjacent[gridIndex(diameter, radius, i, k - 1)];
                    if (p1 == null && p2 == null) {
                        k++;
                        continue;
                    }
                    Block foundAdjacent = locateAdjacentInColumn(materials, centre, i, k, p1, p2).orElse(null);
                    if (foundAdjacent == null) {
                        k++;
                        continue;
                    }
                    adjacent[gridIndex(diameter, radius, i, k)] = foundAdjacent;
                    k++;
                }
                i++;
            }
        }
        // -x, +z corners
        {
            int i = -1;
            while (i >= -radius) {
                int k = 1;
                while (k <= radius) {
                    Block p1 = adjacent[gridIndex(diameter, radius, i + 1, k)];
                    Block p2 = adjacent[gridIndex(diameter, radius, i, k - 1)];
                    if (p1 == null && p2 == null) {
                        k++;
                        continue;
                    }
                    Block foundAdjacent = locateAdjacentInColumn(materials, centre, i, k, p1, p2).orElse(null);
                    if (foundAdjacent == null) {
                        k++;
                        continue;
                    }
                    adjacent[gridIndex(diameter, radius, i, k)] = foundAdjacent;
                    k++;
                }
                i--;
            }
        }
        // -x, -z corners
        {
            int i = -1;
            while (i >= -radius) {
                int k = -1;
                while (k >= -radius) {
                    Block p1 = adjacent[gridIndex(diameter, radius, i + 1, k)];
                    Block p2 = adjacent[gridIndex(diameter, radius, i, k + 1)];
                    if (p1 == null && p2 == null) {
                        k--;
                        continue;
                    }
                    Block foundAdjacent = locateAdjacentInColumn(materials, centre, i, k, p1, p2).orElse(null);
                    if (foundAdjacent == null) {
                        k--;
                        continue;
                    }
                    adjacent[gridIndex(diameter, radius, i, k)] = foundAdjacent;
                    k--;
                }
                i--;
            }
        }
        // +x, -z corners
        {
            int i = 1;
            while (i <= radius) {
                int k = -1;
                while (k >= -radius) {
                    Block p1 = adjacent[gridIndex(diameter, radius, i - 1, k)];
                    Block p2 = adjacent[gridIndex(diameter, radius, i, k + 1)];
                    if (p1 == null && p2 == null) {
                        k--;
                        continue;
                    }
                    Block foundAdjacent = locateAdjacentInColumn(materials, centre, i, k, p1, p2).orElse(null);
                    if (foundAdjacent == null) {
                        k--;
                        continue;
                    }
                    adjacent[gridIndex(diameter, radius, i, k)] = foundAdjacent;
                    k--;
                }
                i++;
            }
        }

        List<Block> adjacentSet = new ArrayList<>();
        for (Block block : adjacent) {
            if (block != null) {
                adjacentSet.add(block);
            }
        }
        return adjacentSet;
    }

    private static int gridIndex(int diameter, int radius, int i, int k) {
        return ((i + radius) * diameter) + (k + radius);
    }

    private static Optional<Block> locateAdjacentInColumn(
            Collection<Material> materials,
            Block centre,
            int i,
            int k,
            Block... adjacents) {
        for (Block adjacent : adjacents) {
            if (adjacent == null) {
                continue;
            }
            int j = adjacent.getY() - centre.getY();
            Block above = centre.getRelative(i, j + 1, k);
            if (materials.contains(above.getType())) {
                return Optional.of(above);
            }
            Block beside = centre.getRelative(i, j, k);
            if (materials.contains(beside.getType())) {
                return Optional.of(beside);
            }
            if (!beside.isPassable()) {
                continue;
            }
            Block below = centre.getRelative(i, j - 1, k);
            if (materials.contains(below.getType())) {
                return Optional.of(below);
            }
        }
        return Optional.empty();
    }
}
