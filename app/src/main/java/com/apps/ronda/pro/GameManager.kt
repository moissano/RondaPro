package com.apps.ronda.pro
// تطبيق تم إنشاؤه من طرف bn omar

import android.os.CountDownTimer

class GameManager {
    val deck = Deck()
    
    // اللاعبون (دعم للعب الثنائي والرباعي)
    val player1 = Player("اللاعب 1")
    val player2 = Player("اللاعب 2")
    val player3 = Player("اللاعب 3")
    val player4 = Player("اللاعب 4")
    val players = listOf(player1, player2, player3, player4)

    // الطاولة والنقاط
    val tableCards = mutableListOf<Card>()
    var team1Score = 0 // فريق (اللاعب 1 و 3)
    var team2Score = 0 // فريق (اللاعب 2 و 4)
    
    // متغيرات الموزع وقواعد الضرب (الحبل)
    var currentDealerIndex = 0
    var lastPlayedValue = -1
    var strikeStreak = 0

    // المؤقت
    private var turnTimer: CountDownTimer? = null
    private val TURN_DURATION = 30000L // 30 ثانية

    // --------------------------------------------------------
    // 1. إعداد اللعبة والقرعة
    // --------------------------------------------------------
    
    fun cutDeckForDealer() {
        println("--- بدء قرعة اختيار الموزع ---")
        val tempDeck = Deck()
        tempDeck.shuffle()
        var lowestValue = 13
        var startingDealer = 0

        for (i in players.indices) {
            val drawnCard = tempDeck.drawCard()
            if (drawnCard != null) {
                if (drawnCard.value < lowestValue) {
                    lowestValue = drawnCard.value
                    startingDealer = i
                }
            }
        }
        currentDealerIndex = startingDealer
        println("أصغر ورقة كانت لـ ${players[currentDealerIndex].name}، هو الموزع!")
    }

    fun startNewGame() {
        deck.shuffle()
        tableCards.clear()
        players.forEach { it.resetPlayer() }
        strikeStreak = 0
        lastPlayedValue = -1

        // توزيع 4 أوراق في الطاولة أولاً
        for (i in 0 until 4) {
            deck.drawCard()?.let { tableCards.add(it) }
        }
        dealCardsToPlayers()
    }

    fun dealCardsToPlayers(): Boolean {
        if (deck.getRemainingCardsCount() >= players.size * 3) {
            for (i in 0 until 3) {
                players.forEach { player ->
                    deck.drawCard()?.let { player.receiveCard(it) }
                }
            }
            detectSecretMatches() // كشف الروندة سراً بعد التوزيع مباشرة
            return true
        }
        return false
    }

    fun moveToNextDealer() {
        currentDealerIndex = (currentDealerIndex + 1) % players.size
        println("انتهى الطرح. الموزع القادم: ${players[currentDealerIndex].name}")
    }

    // --------------------------------------------------------
    // 2. المؤقت واللعب
    // --------------------------------------------------------

    fun startTurnTimer(player: Player) {
        turnTimer?.cancel()
        turnTimer = object : CountDownTimer(TURN_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                // سيتم التحديث في الواجهة لاحقاً
            }
            override fun onFinish() {
                forceAutoPlay(player)
            }
        }.start()
    }

    private fun forceAutoPlay(player: Player) {
        if (player.hand.isNotEmpty()) {
            playCardToTable(player, player.hand[0]) 
        }
    }

    fun playCardToTable(player: Player, card: Card) {
        turnTimer?.cancel() // إيقاف الوقت لأن اللاعب رمى الورقة
        
        val playerIndex = players.indexOf(player)
        val currentTeam = if (playerIndex == 0 || playerIndex == 2) 1 else 2
        val opposingTeam = if (currentTeam == 1) 2 else 1

        // التحقق من قاعدة الضرب (بونت، حبل، جوج حبال)
        if (card.value == lastPlayedValue) {
            strikeStreak++
            when (strikeStreak) {
                1 -> addScore(currentTeam, 1) // بونت
                2 -> { addScore(opposingTeam, -1); addScore(currentTeam, 5) } // حبل
                3 -> { addScore(opposingTeam, -5); addScore(currentTeam, 10) } // جوج حبال
            }
            // منطق الأكل يتم تفصيله لاحقاً بناءً على الأوراق المطابقة
            val captured = listOf(card) // افتراضياً يأكل الورقة المطابقة
            handleCapture(player, currentTeam, captured)
            
        } else {
            strikeStreak = 0
            lastPlayedValue = card.value
            tableCards.add(card) // تبقى في الطاولة
        }
    }

    private fun handleCapture(player: Player, team: Int, capturedCardsList: List<Card>) {
        if (capturedCardsList.isNotEmpty()) {
            player.captureCards(capturedCardsList)
            tableCards.removeAll(capturedCardsList)

            // التحقق من الميسة (إذا خلت الطاولة ولم ينتهِ الطرح)
            if (tableCards.isEmpty() && deck.getRemainingCardsCount() > 0) {
                addScore(team, 1)
                println("ميسة! ${player.name} حصل على نقطة.")
            }
        }
    }

    private fun addScore(team: Int, points: Int) {
        if (team == 1) team1Score += points else team2Score += points
    }

    // --------------------------------------------------------
    // 3. قواعد الروندة والترينكة
    // --------------------------------------------------------

    private fun checkHandMatch(hand: List<Card>): Pair<Int, Int> {
        if (hand.size < 3) return Pair(0, 0)
        val v1 = hand[0].value; val v2 = hand[1].value; val v3 = hand[2].value

        if (v1 == v2 && v2 == v3) return Pair(2, v1) // ترينكة
        if (v1 == v2) return Pair(1, v1) // روندة
        if (v2 == v3) return Pair(1, v2)
        if (v1 == v3) return Pair(1, v1)
        return Pair(0, 0)
    }

    fun detectSecretMatches() {
        players.forEach { it.secretMatch = checkHandMatch(it.hand) }
    }

    fun declareMatch(player: Player) {
        if (player.secretMatch.first > 0) {
            player.hasDeclaredMatch = true
        }
    }

    fun executeKhalas() {
        // يتم حساب الخلاص في نهاية الطرح بين الفرق هنا
        // (سيتم ربطها بدقة عند معالجة أدوار اللعب الجماعي)
        println("حساب الخلاص للروندات المُعلنة...")
    }
}