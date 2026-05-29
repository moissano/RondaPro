package com.apps.ronda.pro

import android.graphics.Color
import com.apps.ronda.pro.R
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // استدعاء مدير اللعبة
    private val gameManager = GameManager()

    // تعريف متغيرات الواجهة
    private lateinit var playerHandLayout: LinearLayout
    private lateinit var opponentHandLayout: LinearLayout
    private lateinit var tableArea: FrameLayout
    private lateinit var playerScoreText: TextView
    private lateinit var opponentScoreText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ربط المتغيرات بعناصر الـ XML
        playerHandLayout = findViewById(R.id.playerHandLayout)
        opponentHandLayout = findViewById(R.id.opponentHandLayout)
        tableArea = findViewById(R.id.tableArea)
        playerScoreText = findViewById(R.id.playerScoreText)
        opponentScoreText = findViewById(R.id.opponentScoreText)

        // بدء لعبة جديدة عند فتح التطبيق
        gameManager.startNewGame()
        
        // تحديث الشاشة لرسم الأوراق
        updateUI()
    }

    // دالة لتحديث الشاشة بالكامل بناءً على بيانات GameManager
    private fun updateUI() {
        // 1. تنظيف الواجهة القديمة
        playerHandLayout.removeAllViews()
        opponentHandLayout.removeAllViews()
        tableArea.removeAllViews()

        // 2. تحديث النقاط
        playerScoreText.text = "النقاط: ${gameManager.team1Score}"
        opponentScoreText.text = "النقاط: ${gameManager.team2Score}"

        // 3. رسم أوراق الخصم (مقلوبة)
        for (i in 0 until gameManager.player2.hand.size) {
            val cardView = createCardView(null, isFaceUp = false)
            opponentHandLayout.addView(cardView)
        }

        // 4. رسم أوراق اللاعب (مكشوفة وقابلة للمس)
        for (card in gameManager.player1.hand) {
            val cardView = createCardView(card, isFaceUp = true)
            
            // إضافة خاصية اللمس لرمي الورقة
            cardView.setOnClickListener {
                // رمي الورقة إلى الطاولة
                gameManager.playCardToTable(gameManager.player1, card)
                // سحب الورقة من يد اللاعب
                gameManager.player1.hand.remove(card)
                
                // تحديث الشاشة بعد الرمي
                updateUI()
                
                // هنا مستقبلاً سنجعل البوت يرد عليك!
            }
            playerHandLayout.addView(cardView)
        }

        // 5. رسم أوراق الطاولة (الطبسيل)
        // سنقوم بتوزيعها برمجياً بشكل عشوائي قليلاً لتبدو مرمية على الطاولة
        for ((index, card) in gameManager.tableCards.withIndex()) {
            val cardView = createCardView(card, isFaceUp = true)
            
            // إعدادات وضع الورقة في الطبسيل (FrameLayout)
            val params = FrameLayout.LayoutParams(160, 240)
            params.gravity = Gravity.CENTER
            // إزاحة بسيطة لكي لا تتطابق الأوراق فوق بعضها تماماً
            params.leftMargin = (index * 40) - 60 
            cardView.layoutParams = params
            
            tableArea.addView(cardView)
        }
    }

    // دالة لصناعة "مستطيل" يمثل الورقة برمجياً
    private fun createCardView(card: Card?, isFaceUp: Boolean): TextView {
        val cardView = TextView(this)
        
        // حجم الورقة (العرض 160 بكسل، الطول 240 بكسل)
        val params = LinearLayout.LayoutParams(160, 240)
        params.setMargins(8, 8, 8, 8) // هوامش بين الأوراق
        cardView.layoutParams = params
        cardView.gravity = Gravity.CENTER
        cardView.textSize = 14f

        if (isFaceUp && card != null) {
            // وجه الورقة المكشوف
            cardView.setBackgroundColor(Color.WHITE)
            
            // تحديد لون النص حسب نوع الورقة (لتسهيل التمييز)
            val suitColor = when (card.suit) {
                1 -> Color.parseColor("#FFC107") // الدورو (أصفر)
                2 -> Color.parseColor("#F44336") // الكوبا (أحمر)
                3 -> Color.parseColor("#2196F3") // السيف (أزرق)
                4 -> Color.parseColor("#4CAF50") // الباستو (أخضر)
                else -> Color.BLACK
            }
            cardView.setTextColor(suitColor)
            cardView.text = "${card.value}\n" + getSuitName(card.suit)
        } else {
            // ظهر الورقة المقلوبة (للخصم)
            cardView.setBackgroundColor(Color.parseColor("#B71C1C")) // أحمر داكن
            cardView.text = "روندة"
            cardView.setTextColor(Color.WHITE)
        }

        return cardView
    }

    // دالة مساعدة لجلب اسم النوع
    private fun getSuitName(suit: Int): String {
        return when (suit) {
            1 -> "دورو"
            2 -> "كوبا"
            3 -> "سيف"
            4 -> "باستو"
            else -> ""
        }
    }
}