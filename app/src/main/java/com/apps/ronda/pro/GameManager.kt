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

    // نظام توزيع الروندة الأصلي
    var isFirstDeal = true // لتحديد هل نحن في التفريقة الأولى (4 أوراق) أم المعاودات (3 أوراق)

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
        isFirstDeal = true // إعادة ضبط التوزيعة لتكون الأولى

        // 1. وضع 4 أوراق في الطاولة أولاً
        for (i in 0 until 4) {
            deck.drawCard()?.let { tableCards.add(it) }
        }
        
        // 2. توزيع أول 4 أوراق لكل لاعب (التفريقة الأولى: 16 ورقة)
        dealCardsToPlayers()
    }

    fun dealCardsToPlayers(): Boolean {
        val remainingCards = deck.getRemainingCardsCount()
        
        // إذا كان هذا التوزيع الأول، نوزع 4 أوراق لكل لاعب
        if (isFirstDeal && remainingCards >= players.size * 4) {
            for (i in 0 until 4) {
                players.forEach { player ->
                    deck.drawCard()?.let { player.receiveCard(it) }
                }
            }
            isFirstDeal = false // التوزيعات القادمة ستكون معاودات (3 أوراق)
            detectSecretMatches()
            return true
        } 
        // إذا كانت "معاودة"، نوزع 3 أوراق لكل لاعب
        else if (!isFirstDeal && remainingCards >= players.size * 3) {
            for (i in 0 until 3) {
                players.forEach { player ->
                    deck.drawCard()?.let { player.receiveCard(it) }
                }
            }
            detectSecretMatches()
            return true
        }
        
        // إذا لم يتبقَ أوراق في الكارطة
        return false
    }

    // دالة مهمة يتم استدعاؤها بعدما يرمي كل لاعب ورقته
    // لتتحقق هل انتهت أوراق اليد لجميع اللاعبين لتبدأ "المعاودة" تلقائياً
    fun checkAndTriggerNextDeal() {
        val allHandsEmpty = players.all { it.hand.isEmpty() }
        
        if (allHandsEmpty) {
            val hasMoreCards = dealCardsToPlayers()
            if (hasMoreCards) {
                println("تمت المعاودة وتوزيع أوراق جديدة تلقائياً!")
                // هنا نقوم بتحديث الواجهة (UI) في مشروعك ليرى اللاعب أوراقه الجديدة
            } else {
                println("انتهت الكارطة بالكامل! حان وقت حساب النقاط ونهاية الطرح.")
                executeKhalas()
                moveToNextDealer()
            }
        }
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
        player.hand.remove(card) // إزالة الورقة الملعوبة من يد اللاعب يدوياً
        
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
            val captured = listOf(card) 
            handleCapture(player, currentTeam, captured)
            
        } else {
            strikeStreak = 0
            lastPlayedValue = card.value
            tableCards.add(card) // تبقى في الطاولة
        }

        // فحص هل يجب توزيع أوراق جديدة بعد هذه الرمية
        checkAndTriggerNextDeal()
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
        
        // حساب التكرارات للأوراق الموجودة باليد
        val valueCounts = hand.groupBy { it.value }.mapValues { it.value.size }
        
        // إذا كانت هناك 3 أو 4 أوراق متشابهة (ترينكة)
        val triple = valueCounts.filter { it.value >= 3 }.keys.firstOrNull()
        if (triple != null) return Pair(2, triple)
        
        // إذا كانت هناك ورقتان متشابهتان (روندة)
        val pair = valueCounts.filter { it.value == 2 }.keys.firstOrNull()
        if (pair != null) return Pair(1, pair)
        
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
        println("حساب الخلاص للروندات المُعلنة...")
    }
}
