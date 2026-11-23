package com.example.myapplication.data

import com.example.myapplication.model.Farm
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import java.util.Locale

object NasugbuData {

    val bounds = BoundingBox(
        14.2280,  // North
        120.8020, // East
        14.0280,  // South
        120.5980  // West
    )

    val center = GeoPoint(14.1150, 120.6950)

    val polygonPoints = listOf(
        GeoPoint(14.2240, 120.6140),
        GeoPoint(14.2120, 120.6950),
        GeoPoint(14.1520, 120.7780),
        GeoPoint(14.1020, 120.8020),
        GeoPoint(14.0620, 120.7760),
        GeoPoint(14.0300, 120.6980),
        GeoPoint(14.0320, 120.6220),
        GeoPoint(14.0720, 120.6180),
        GeoPoint(14.1050, 120.6080),
        GeoPoint(14.1520, 120.6060),
        GeoPoint(14.1980, 120.6040),
        GeoPoint(14.2240, 120.6140)
    )

    fun getConsolidatedFarms(): MutableList<Farm> {
        val rawFarms = listOf(
            // 1. Mga Pangunahing Agricultural, Sugar-Producing, at Lowland Barangays
            Farm(
                name = "Tumalim",
                crop = "Sugarcane, Corn, Vegetables (Eggplant & Tomato), Upland Rice & Banana",
                squareMeters = 3_300_000L,
                lat = 14.08132,
                lng = 120.71877
            ),
            Farm(
                name = "Looc",
                crop = "Rice, Corn, High-Value Vegetables, Coconut & Banana",
                squareMeters = 2_750_000L,
                lat = 14.1672,
                lng = 120.6318
            ),
            Farm(
                name = "Lumbangan",
                crop = "Sugarcane, Irrigated Rice & Vegetables",
                squareMeters = 3_500_000L,
                lat = 14.057888,
                lng = 120.64049
            ),
            Farm(
                name = "Cogunan",
                crop = "Sugarcane, Rice, Corn, Banana, Mango Orchards & Vegetables",
                squareMeters = 3_100_000L,
                lat = 14.06327,
                lng = 120.65818
            ),
            Farm(
                name = "Banilad",
                crop = "Sugarcane, Irrigated Rice, Corn, Mango & Banana",
                squareMeters = 4_200_000L,
                lat = 14.08752,
                lng = 120.76343
            ),
            Farm(
                name = "Bilog",
                crop = "Sugarcane, Rice, Corn, Vegetables & Root Crops",
                squareMeters = 2_400_000L,
                lat = 14.0912,
                lng = 120.6625
            ),
            Farm(
                name = "Bunducan",
                crop = "Sugarcane, Rice, Corn, Coconut & Banana",
                squareMeters = 2_950_000L,
                lat = 14.0945,
                lng = 120.6448
            ),
            Farm(
                name = "Catandaan",
                crop = "Sugarcane, Irrigated Rice, Corn, Vegetables & Fruit-Bearing Trees",
                squareMeters = 2_400_000L,
                lat = 14.08081,
                lng = 120.68275
            ),
            Farm(
                name = "Dayap",
                crop = "Sugarcane, Rice, Corn, Cassava & Vegetables",
                squareMeters = 3_800_000L,
                lat = 14.09640,
                lng = 120.66850
            ),
            Farm(
                name = "Latag",
                crop = "Sugarcane, Lowland Rice, Corn, Banana & Coconut",
                squareMeters = 2_850_000L,
                lat = 14.12073,
                lng = 120.72048
            ),
            Farm(
                name = "Malapad na Bato",
                crop = "Sugarcane, Rice, Corn & Forage/Feed Crops (Livestock)",
                squareMeters = 3_150_000L,
                lat = 14.10499,
                lng = 120.68354
            ),
            Farm(
                name = "Maugat",
                crop = "Sugarcane, Rice, Corn, Vegetables & Mango",
                squareMeters = 2_700_000L,
                lat = 14.0858,
                lng = 120.6825
            ),
            Farm(
                name = "Munting Indang",
                crop = "Sugarcane, Rice, Corn, Coconut & Intercropped Vegetables",
                squareMeters = 2_650_000L,
                lat = 14.0715,
                lng = 120.6695
            ),
            Farm(
                name = "Putat",
                crop = "Sugarcane, Rice, Corn, Banana, Coconut, Cacao & Vegetables",
                squareMeters = 2_600_000L,
                lat = 14.08277,
                lng = 120.65779
            ),
            Farm(
                name = "Reparo",
                crop = "Sugarcane, Irrigated Rice, Corn & Vegetables",
                squareMeters = 2_550_000L,
                lat = 14.07593,
                lng = 120.69450
            ),
            Farm(
                name = "Talangan",
                crop = "Sugarcane, Irrigated Rice, Corn, Watermelon & Fruits",
                squareMeters = 2_300_000L,
                lat = 14.07682,
                lng = 120.63675
            ),
            Farm(
                name = "Utod",
                crop = "Sugarcane, Rice, Corn, Cassava, Saba Banana & Vegetables",
                squareMeters = 3_100_000L,
                lat = 14.11873,
                lng = 120.64721
            ),

            // 2. Mga Upland, Hilly, at Agro-Forestry Barangays
            Farm(
                name = "Aga",
                crop = "High-Value Vegetables (Sayote), Banana, Pineapple, Coffee, Coconut, Fruits & Sugarcane",
                squareMeters = 3_400_000L,
                lat = 14.0938,
                lng = 120.7955
            ),
            Farm(
                name = "Kaylaway",
                crop = "Sugarcane, Coconut, Banana, Pineapple, Coffee, Vegetables, Mango, Avocado & Jackfruit",
                squareMeters = 3_600_000L,
                lat = 14.0965,
                lng = 120.7785
            ),
            Farm(
                name = "Kayrilaw",
                crop = "Coconut, Banana, Corn, Cassava, Sweet Potato, Coffee, Vegetables & Sugarcane",
                squareMeters = 2_950_000L,
                lat = 14.1015,
                lng = 120.7885
            ),
            Farm(
                name = "Mataas na Pulo",
                crop = "Coconut, Banana, Corn, Root Crops, Coffee, Cacao & Vegetables",
                squareMeters = 3_250_000L,
                lat = 14.09136,
                lng = 120.72904
            ),
            Farm(
                name = "Tala",
                crop = "Coconut, Banana, Corn, Vegetables, Root Crops & Agro-Forestry",
                squareMeters = 2_800_000L,
                lat = 14.1185,
                lng = 120.7720
            ),
            Farm(
                name = "Bilaran",
                crop = "Sugarcane, Rice, Corn, Coconut & Fruits",
                squareMeters = 3_000_000L,
                lat = 14.0498,
                lng = 120.6795
            ),
            Farm(
                name = "Bulihan",
                crop = "Sugarcane, Corn, Coconut, Banana & Rainfed Vegetables",
                squareMeters = 2_900_000L,
                lat = 14.16024,
                lng = 120.64716
            ),
            Farm(
                name = "Butucan",
                crop = "Sugarcane, Corn, Coconut, Banana & Local Vegetables",
                squareMeters = 2_500_000L,
                lat = 14.1125,
                lng = 120.6980
            ),
            Farm(
                name = "Camachilihan",
                crop = "Sugarcane, Corn, Coconut, Mango & Banana",
                squareMeters = 2_450_000L,
                lat = 14.0650,
                lng = 120.6920
            ),
            Farm(
                name = "Pantalan",
                crop = "Coconut, Corn, Sugarcane, Irrigated Rice, Banana & Leafy Vegetables",
                squareMeters = 2_250_000L,
                lat = 14.08413,
                lng = 120.63507
            ),

            // 3. Mga Coastal at Tourism-Oriented Barangays
            Farm(
                name = "Calayo",
                crop = "Coconut, Banana, Corn, Vegetables, Root Crops & Coastal Fishery",
                squareMeters = 1_900_000L,
                lat = 14.1518,
                lng = 120.6135
            ),
            Farm(
                name = "Papaya",
                crop = "Coconut, Banana, Mango, Cashew & Root Crops",
                squareMeters = 2_100_000L,
                lat = 14.17407,
                lng = 120.60821
            ),
            Farm(
                name = "Balaytigue",
                crop = "Coconut, Mango, Banana, Cashew & Corn",
                squareMeters = 2_450_000L,
                lat = 14.12995,
                lng = 120.60462
            ),
            Farm(
                name = "Natipuan",
                crop = "Coconut, Banana, Mango, Cashew & Fruit-Bearing Trees",
                squareMeters = 2_350_000L,
                lat = 14.12331,
                lng = 120.62360
            ),
            Farm(
                name = "Bucana",
                crop = "Coconut, Nipa, Backyard Vegetables & Estuarine Fishery",
                squareMeters = 1_150_000L,
                lat = 14.06745,
                lng = 120.62574
            ),
            Farm(
                name = "Wawa",
                crop = "Coconut, Nipa, Backyard Vegetables & Coastal Fishery",
                squareMeters = 1_450_000L,
                lat = 14.08186,
                lng = 120.62444
            ),

            // 4. Mga Urban at Poblacion Barangays (Barangay 1 hanggang Barangay 12)
            Farm(
                name = "Barangay 1 (Poblacion)",
                crop = "Urban Backyard Gardening (Moringa, Chili, Sweet Potato Tops & Ornamentals)",
                squareMeters = 180_000L,
                lat = 14.0748,
                lng = 120.6312
            ),
            Farm(
                name = "Barangay 2 (Poblacion)",
                crop = "Urban/Residential Area (Small Backyard Crops)",
                squareMeters = 160_000L,
                lat = 14.0742,
                lng = 120.6330
            ),
            Farm(
                name = "Barangay 3 (Poblacion)",
                crop = "Urban/Commercial Center (No Commercial Agricultural Production)",
                squareMeters = 150_000L,
                lat = 14.0736,
                lng = 120.6348
            ),
            Farm(
                name = "Barangay 4 (Poblacion)",
                crop = "Urban/Container Gardening (Residential & Commercial Zone)",
                squareMeters = 165_000L,
                lat = 14.0728,
                lng = 120.6315
            ),
            Farm(
                name = "Barangay 5 (Poblacion)",
                crop = "Urban/Commercial Area (No Agricultural Farmland)",
                squareMeters = 145_000L,
                lat = 14.0724,
                lng = 120.6334
            ),
            Farm(
                name = "Barangay 6 (Poblacion)",
                crop = "Urban/Residential Area (No Commercial Crops)",
                squareMeters = 155_000L,
                lat = 14.0720,
                lng = 120.6352
            ),
            Farm(
                name = "Barangay 7 (Poblacion)",
                crop = "Urban/Residential Area (Limited Backyard Crops)",
                squareMeters = 170_000L,
                lat = 14.0712,
                lng = 120.6318
            ),
            Farm(
                name = "Barangay 8 (Poblacion)",
                crop = "Urban/Residential Area (No Agricultural Land)",
                squareMeters = 160_000L,
                lat = 14.0708,
                lng = 120.6336
            ),
            Farm(
                name = "Barangay 9 (Poblacion)",
                crop = "Urban/Commercial Town Center (No Farmland)",
                squareMeters = 150_000L,
                lat = 14.0704,
                lng = 120.6354
            ),
            Farm(
                name = "Barangay 10 (Poblacion)",
                crop = "Urban/Residential Area (No Large-Scale Crops)",
                squareMeters = 175_000L,
                lat = 14.0695,
                lng = 120.6320
            ),
            Farm(
                name = "Barangay 11 (Poblacion)",
                crop = "Urban/Residential Area (Limited Backyard Crops)",
                squareMeters = 165_000L,
                lat = 14.0691,
                lng = 120.6338
            ),
            Farm(
                name = "Barangay 12 (Poblacion)",
                crop = "Urban/Residential Area (Backyard Gardening Only)",
                squareMeters = 185_000L,
                lat = 14.0687,
                lng = 120.6356
            )
        )
        return consolidateByBarangay(rawFarms).toMutableList()
    }

    fun matchesCropCategory(farm: Farm, filterKey: String): Boolean {
        val cropLower = farm.crop.lowercase(Locale.US)
        return when (filterKey) {
            "SUGARCANE" -> cropLower.contains("sugarcane")
            "RICE" -> cropLower.contains("rice")
            "COCONUT" -> cropLower.contains("coconut") || cropLower.contains("nipa")
            "FRUIT" -> cropLower.contains("pineapple") || cropLower.contains("mango") ||
                    cropLower.contains("banana") || cropLower.contains("coffee") ||
                    cropLower.contains("avocado") || cropLower.contains("jackfruit") ||
                    cropLower.contains("cashew") || cropLower.contains("fruit")
            "VEG" -> cropLower.contains("corn") || cropLower.contains("vegetable") ||
                    cropLower.contains("cassava") || cropLower.contains("sayote") ||
                    cropLower.contains("eggplant") || cropLower.contains("tomato") ||
                    cropLower.contains("root crops") || cropLower.contains("moringa")
            else -> true
        }
    }

    private fun consolidateByBarangay(records: List<Farm>): List<Farm> {
        val grouped = LinkedHashMap<String, MutableList<Farm>>()
        for (record in records) {
            val normalizedKey = record.name.trim()
                .removePrefix("Brgy.")
                .removePrefix("Barangay ")
                .trim()
                .lowercase(Locale.US)
            grouped.getOrPut(normalizedKey) { mutableListOf() }.add(record)
        }

        return grouped.values.map { entries ->
            val primary = entries.first()
            val cleanName = primary.name.trim()
                .removePrefix("Brgy.")
                .trim()

            val totalSqm = entries.sumOf { it.squareMeters }

            val uniqueCrops = LinkedHashSet<String>()
            for (item in entries) {
                item.crop.split(",", "&", "/")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { cropName ->
                        if (uniqueCrops.none { it.equals(cropName, ignoreCase = true) }) {
                            uniqueCrops.add(cropName)
                        }
                    }
            }

            val combinedCropText = when (uniqueCrops.size) {
                0 -> primary.crop
                1 -> uniqueCrops.first()
                else -> {
                    val list = uniqueCrops.toList()
                    list.dropLast(1).joinToString(", ") + " & " + list.last()
                }
            }

            Farm(
                name = cleanName,
                crop = combinedCropText,
                squareMeters = totalSqm,
                lat = primary.lat,
                lng = primary.lng,
                mergedParcels = entries.size
            )
        }
    }
}