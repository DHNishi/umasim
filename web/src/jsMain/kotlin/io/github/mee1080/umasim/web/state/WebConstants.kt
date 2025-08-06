/*
 * Copyright 2021 mee1080
 *
 * This file is part of umasim.
 *
 * umasim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * umasim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with umasim.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.mee1080.umasim.web.state

import io.github.mee1080.umasim.ai.FactorBasedActionSelector2
import io.github.mee1080.umasim.data.*
import io.github.mee1080.umasim.scenario.Scenario
import io.github.mee1080.umasim.scenario.climax.MegaphoneItem
import io.github.mee1080.umasim.scenario.climax.WeightItem

object WebConstants {

    val notSelected = -1 to null as SupportCard?

    val displayStatusTypeList = listOf(
        StatusType.NONE,
        StatusType.SPEED,
        StatusType.STAMINA,
        StatusType.POWER,
        StatusType.GUTS,
        StatusType.WISDOM,
        StatusType.FRIEND,
        StatusType.GROUP,
    )

    val scenarioList = Scenario.entries.reversed()

    val charaList =
        listOf(Chara.empty()) + Store.charaList.filter { it.rank == 5 && it.rarity == 5 }.sortedBy { it.charaName }

    val supportMap = Store.supportList.groupBy { it.id }

    val displaySupportList = listOf(notSelected) + supportMap.entries
        .map { it.key to it.value.first { card -> card.talent == 4 } }
        .sortedBy { it.second.type.ordinal * 10000000 - it.second.rarity * 1000000 + it.first }

    fun getSupportList(type: StatusType) = displaySupportList.filter { it.second?.type == type }

    fun getRarityText(card: SupportCard) = when (card.rarity) {
        1 -> "R"
        2 -> "SR"
        3 -> "SSR"
        else -> "?"
    }

    val supportTalentList = listOf(0, 1, 2, 3, 4).map { it to it.toString() }

    class SortOrder<T : Comparable<T>>(
        val label: String,
        val descending: Boolean = true,
        val noInfo: Boolean = false,
        val value: SupportCard.() -> T,
    ) : Comparator<Pair<Int, SupportCard?>> {
        fun toInfo(card: SupportCard?) = if (noInfo || card == null) "" else " (${card.value()})"
        override fun compare(a: Pair<Int, SupportCard?>, b: Pair<Int, SupportCard?>): Int {
            val cardA = a.second
            val cardB = b.second
            return if (cardA == null || cardB == null) {
                if (cardA != null) 1
                else if (cardB != null) -1
                else 0
            } else if (descending) {
                cardB.value().compareTo(cardA.value())
            } else {
                cardA.value().compareTo(cardB.value())
            }
        }

        override fun equals(other: Any?): Boolean {
            return label == (other as? SortOrder<*>)?.label
        }

        override fun hashCode(): Int {
            return label.hashCode()
        }
    }

    fun noSpecialUniqueCondition(card: SupportCard) = SpecialUniqueCondition(
        card.type,
        1,
        5,
        0,
        emptyMap(),
        0,
        Status(maxHp = 100, hp = 100),
        0,
        1,
        0,
        0,
        0,
        false,
        0,
    )

    private val SupportCard.noSpecialUniqueCondition get() = noSpecialUniqueCondition(this)

    val withSpecialUniqueCondition
        get() = SpecialUniqueCondition(
            StatusType.NONE,
            5,
            20,
            100,
            (listOf(StatusType.FRIEND) + trainingType).associateWith { 2 },
            1000000,
            Status(maxHp = 120, hp = 30),
            600,
            5,
            10,
            10,
            10,
            true,
            10,
        )

    val supportSortOrder = listOf(
        SortOrder("Default", descending = false, noInfo = true) { type.ordinal * 10000000 - rarity * 1000000 + id },
        SortOrder("ID (mostly newest first)", noInfo = true) { id },
        SortOrder("Name", descending = false, noInfo = true) { name },
        SortOrder("Character name", descending = false, noInfo = true) { chara },
        SortOrder("Initial bond") { initialRelation },
        // TODO handle special unique
        SortOrder("Initial status total") { initialStatus(emptyList()).statusTotal },
        SortOrder("Friend bonus") { friendFactor(noSpecialUniqueCondition) },
        SortOrder("Friend bonus (special unique)") { friendFactor(withSpecialUniqueCondition) },
        SortOrder("Motivation bonus") { motivationFactor(noSpecialUniqueCondition) },
        SortOrder("Motivation bonus (special unique)") { motivationFactor(withSpecialUniqueCondition) },
        SortOrder("Training effect") { trainingFactor(noSpecialUniqueCondition) },
        SortOrder("Training effect (special unique)") { trainingFactor(withSpecialUniqueCondition) },
        SortOrder("Speed bonus") { getBaseBonus(StatusType.SPEED, noSpecialUniqueCondition) },
        SortOrder("Speed bonus (special unique)") { getBaseBonus(StatusType.SPEED, withSpecialUniqueCondition) },
        SortOrder("Stamina bonus") { getBaseBonus(StatusType.STAMINA, noSpecialUniqueCondition) },
        SortOrder("Stamina bonus (special unique)") { getBaseBonus(StatusType.STAMINA, withSpecialUniqueCondition) },
        SortOrder("Power bonus") { getBaseBonus(StatusType.POWER, noSpecialUniqueCondition) },
        SortOrder("Power bonus (special unique)") { getBaseBonus(StatusType.POWER, withSpecialUniqueCondition) },
        SortOrder("Guts bonus") { getBaseBonus(StatusType.GUTS, noSpecialUniqueCondition) },
        SortOrder("Guts bonus (special unique)") { getBaseBonus(StatusType.GUTS, withSpecialUniqueCondition) },
        SortOrder("Wisdom bonus") { getBaseBonus(StatusType.WISDOM, noSpecialUniqueCondition) },
        SortOrder("Wisdom bonus (special unique)") { getBaseBonus(StatusType.WISDOM, withSpecialUniqueCondition) },
        SortOrder("Skill bonus") { getBaseBonus(StatusType.SKILL, noSpecialUniqueCondition) },
        SortOrder("Skill bonus (special unique)") { getBaseBonus(StatusType.SKILL, withSpecialUniqueCondition) },
        SortOrder("Race bonus") { race },
        SortOrder("Fan bonus") { fan },
        SortOrder("Specialty rate") { specialtyRate(0, noSpecialUniqueCondition) / 100.0 },
        SortOrder("Specialty rate (special unique)") { specialtyRate(0, withSpecialUniqueCondition) / 100.0 },
        SortOrder("Hint Lv") { hintLevel },
        SortOrder("Hint rate") { hintFrequency },
        SortOrder("Wisdom friend recovery") { wisdomFriendRecovery(noSpecialUniqueCondition) },
        SortOrder("Wisdom friend recovery (special unique)") { wisdomFriendRecovery(withSpecialUniqueCondition) },
    )

    val trainingTypeList = trainingType.toList()

    val motivationMap = mapOf(3 to "Peak", 2 to "Excellent", 1 to "Good", 0 to "Normal", -1 to "Poor", -2 to "Terrible")

    val trainingInfo = Scenario.entries.associateWith { Store.getTrainingInfo(it) }

    val trainingList = Scenario.entries.associateWith { it.trainingData }

    val simulationModeList = mapOf(
        Scenario.URA to listOf(
            "Speed3 Power3" to { FactorBasedActionSelector2.speedPower.generateSelector() },
            "Speed3 Power3 Mid-distance" to { FactorBasedActionSelector2.speedPowerMiddle.generateSelector() },
            "Speed3 Wisdom3" to { FactorBasedActionSelector2.speedWisdom.generateSelector() },
            "Speed3 Stamina3" to { FactorBasedActionSelector2.speedStamina.generateSelector() },
            "Power3 Wisdom3" to { FactorBasedActionSelector2.aoharuPowerWisdom.generateSelector() },
            "Speed2 Power3 Wisdom1" to { FactorBasedActionSelector2.speed2Power3Wisdom1.generateSelector() },
        ),
        Scenario.AOHARU to listOf(
            "Speed2 Power3 Wisdom1" to { FactorBasedActionSelector2.aoharuSpeed2Power3Wisdom1.generateSelector() },
            "Speed2 Wisdom2 with Rice friend" to { FactorBasedActionSelector2.aoharuSpeed2Power1Wisdom2Friend1Optuna3.generateSelector() },
            "Speed2 Stamina1 Wisdom3" to { FactorBasedActionSelector2.aoharuSpeed2Stamina1Wisdom3.generateSelector() },
            "Speed1 Stamina1 Wisdom1 with Digital friend" to { FactorBasedActionSelector2.aoharuSpeed2Stamina1Power1Wisdom1Friend1Optuna.generateSelector() },
        ),
        Scenario.CLIMAX to listOf(),
        Scenario.GRAND_LIVE to listOf(),
        Scenario.GM to listOf(),
    )

    val displaySimulationModeList =
        simulationModeList.mapValues { it.value.mapIndexed { index, pair -> index to pair.first } }

    val dummyMegaphoneItem = MegaphoneItem("None", 0, 0, 0)

    val shopItemMegaphone = listOf(dummyMegaphoneItem) + Store.Climax.shopItem.filterIsInstance<MegaphoneItem>()

    val dummyWeightItem = WeightItem("None", 0, 0, 0, StatusType.NONE)

    val shopItemWeight = listOf(dummyWeightItem) + Store.Climax.shopItem.filterIsInstance<WeightItem>()

    val shopItemWeightNames = listOf(-1 to "None") + shopItemWeight.mapIndexed { index, item -> index to item.name }

    val raceItem = mapOf(
        "Horseshoe Hammer - Artisan" to 1.2,
        "Horseshoe Hammer - Supreme" to 1.35,
    )

    val uafFestivalBonusValue = mapOf(
        0 to "0",
        1 to "1～4",
        3 to "5～9",
        7 to "10～14",
        12 to "15～19",
        17 to "20～25",
    )

    val uafFestivalBonus = listOf(0, 1, 3, 7, 12, 17)

    val cookCookPoint = listOf(0, 500, 1500, 2500, 5000, 7000, 10000, 12000)

    val cookPhase = mapOf(
        -1 to "None", 0 to "Junior Cuisine", 1 to "Classic Cuisine", 2 to "Senior Cuisine", 3 to "GI Plate",
    )

    val cookResult1 = mapOf(0 to "All satisfied", 1 to "Very satisfied in December", 2 to "Very satisfied in June")

    val cookResult2 = mapOf(0 to "Satisfied", 1 to "Very satisfied", 2 to "Extremely satisfied")
}

fun SupportCard?.displayName(): String {
    return if (this == null) {
        "Not selected"
    } else {
        WebConstants.getRarityText(this) + " " + name
    }
}