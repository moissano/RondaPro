package com.apps.ronda.pro

class Player(val name: String) {
    // الأوراق التي يحملها اللاعب في يده حالياً
    val hand = mutableListOf<Card>()
    
    // الأوراق التي أكلها اللاعب (الرصيد لحساب النقاط في نهاية الجولة)
    val capturedCards = mutableListOf<Card>()

    // متغيرات الروندة والترينكة
    var secretMatch: Pair<Int, Int> = Pair(0, 0) // لتخزين نوع وقيمة الروندة سراً
    var hasDeclaredMatch: Boolean = false        // هل أعلن عنها في الرمية الأولى؟

    // استلام ورقة من الموزع وإضافتها لليد
    fun receiveCard(card: Card) {
        if (hand.size < 3) {
            hand.add(card)
        }
    }

    // لعب ورقة (يتم سحبها من يد اللاعب ورميها على الطاولة)
    fun playCard(index: Int): Card? {
        if (index in 0 until hand.size) {
            return hand.removeAt(index)
        }
        return null // في حال تم تمرير رقم خاطئ
    }

    // إضافة الأوراق المأكولة من الطاولة إلى رصيد اللاعب
    fun captureCards(cards: List<Card>) {
        capturedCards.addAll(cards)
    }

    // إفراغ اليد والرصيد (استعداداً لطرح جديد أو لعبة جديدة)
    fun resetPlayer() {
        hand.clear()
        capturedCards.clear()
        secretMatch = Pair(0, 0)
        hasDeclaredMatch = false
    }
}