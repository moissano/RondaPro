package com.apps.ronda.pro

class Deck {
    // قائمة قابلة للتعديل لتخزين الأوراق
    private val cards = mutableListOf<Card>()

    // دالة init تعمل تلقائياً بمجرد استدعاء الفئة لتوليد الأوراق
    init {
        val values = listOf(1, 2, 3, 4, 5, 6, 7, 10, 11, 12)
        
        for (suit in 1..4) {
            for (value in values) {
                cards.add(Card(suit, value))
            }
        }
    }

    // دالة لخلط الأوراق
    fun shuffle() {
        cards.shuffle()
    }

    // دالة لسحب ورقة من الحزمة
    fun drawCard(): Card? {
        return if (cards.isNotEmpty()) {
            cards.removeAt(cards.size - 1)
        } else {
            null // إذا انتهت الأوراق
        }
    }

    // دالة لمعرفة عدد الأوراق المتبقية
    fun getRemainingCardsCount(): Int = cards.size
}