package com.example.data.banks

import com.example.domain.model.Bank

object BankCatalog {
    val banks: List<Bank> = listOf(
        Bank(id = "100000000004", name = "Т-Банк", shortName = "Т-Банк", isPopular = true),
        Bank(id = "100000000111", name = "СберБанк", shortName = "Сбер", isPopular = true),
        Bank(id = "100000000005", name = "ВТБ", shortName = "ВТБ", isPopular = true),
        Bank(id = "100000000008", name = "Альфа-Банк", shortName = "Альфа", isPopular = true),
        Bank(id = "100000000001", name = "Газпромбанк", shortName = "Газпромбанк", isPopular = true),
        Bank(id = "100000000010", name = "ПСБ (Промсвязьбанк)", shortName = "ПСБ", isPopular = true),
        Bank(id = "100000000020", name = "Россельхозбанк", shortName = "РСХБ", isPopular = true),
        Bank(id = "100000000007", name = "Райффайзен Банк", shortName = "Райффайзен", isPopular = true),
        Bank(id = "100000000013", name = "Совкомбанк", shortName = "Совкомбанк", isPopular = true),
        Bank(id = "100000000267", name = "Ozon Банк", shortName = "Ozon Банк", isPopular = true),
        Bank(id = "100000000275", name = "Яндекс Банк", shortName = "Яндекс Банк", isPopular = true),
        Bank(id = "100000000017", name = "МТС Банк", shortName = "МТС Банк", isPopular = true),
        Bank(id = "100000000015", name = "Банк Открытие", shortName = "Открытие", isPopular = false),
        Bank(id = "100000000026", name = "Банк Уралсиб", shortName = "Уралсиб", isPopular = false),
        Bank(id = "100000000012", name = "Росбанк", shortName = "Росбанк", isPopular = false),
        Bank(id = "100000000030", name = "Почта Банк", shortName = "Почта Банк", isPopular = false),
        Bank(id = "100000000014", name = "Банк Санкт-Петербург", shortName = "БСПБ", isPopular = false),
        Bank(id = "100000000043", name = "Банк ДОМ.РФ", shortName = "ДОМ.РФ", isPopular = false),
        Bank(id = "100000000027", name = "Ак Барс Банк", shortName = "Ак Барс", isPopular = false),
        Bank(id = "100000000082", name = "Ренессанс Банк", shortName = "Ренессанс", isPopular = false),
        Bank(id = "100000000049", name = "Хоум Банк", shortName = "Хоум Банк", isPopular = false),
        Bank(id = "100000000029", name = "Новикомбанк", shortName = "Новикомбанк", isPopular = false),
        Bank(id = "100000000044", name = "Абсолют Банк", shortName = "Абсолют", isPopular = false),
        Bank(id = "100000000031", name = "Банк Русский Стандарт", shortName = "Русский Стандарт", isPopular = false),
        Bank(id = "other_bank", name = "Другой банк (СБП)", shortName = "Другой банк", isPopular = false)
    )

    fun findById(id: String?): Bank? {
        if (id == null) return null
        return banks.find { it.id == id }
    }

    fun search(query: String): List<Bank> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return banks
        return banks.filter {
            it.name.lowercase().contains(trimmed) ||
            it.shortName.lowercase().contains(trimmed) ||
            it.id.contains(trimmed)
        }
    }
}
