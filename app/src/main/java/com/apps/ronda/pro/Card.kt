package com.apps.ronda.pro

// data class تقوم تلقائياً بإنشاء الخصائص المطلوبة للبطاقة
data class Card(val suit: Int, val value: Int) {
    
    // دالة لطباعة اسم الورقة لتسهيل اختبار الكود
    override fun toString(): String {
        val suitName = when (suit) {
            1 -> "الدورو"
            2 -> "الكوبا"
            3 -> "السيف"
            4 -> "الباستو"
            else -> "مجهول"
        }
        return "$value $suitName"
    }
}