package com.luddosaurus.glasteroids

import android.graphics.Color

private const val PADDING_X = 8f
private const val PADDING_Y = 6f
class HUD {
    private val texts = ArrayList<Text>()
    private var scaling = TEXT_SIZE_NORMAL
    private var color = Color.MAGENTA


    fun update(score: Int, health: Int, fps: Float, gameState: GameState, lvl : Int) {
        texts.clear()

        when (gameState) {
            GameState.Active -> showActiveState(score,health, fps)
            GameState.GameOver -> showGameOverState(score)
            GameState.LevelFinish ->showVictoryState(score, health, lvl)
        }

    }

    private fun showVictoryState(score: Int, health: Int, lvl: Int) {
        scaling = TEXT_SIZE_LARGE
        addText("LEVEL $lvl CLEARED!!")
        addText("SCORE:$score")
        addText("HEALTH:$health")
        addText("PRESS TO START LVL${lvl+1}")
    }


    private fun showGameOverState(score : Int) {
        scaling = TEXT_SIZE_LARGE
        addText("GAME OVER!")
        addText("SCORE:$score")
        addText("PRESS TO RESTART")
    }

    private fun showActiveState(score : Int, health: Int, fps : Float) {
        scaling = TEXT_SIZE_NORMAL
        addText("FPS:${fps}")
        addText("HEALTH:$health")
        addText("SCORE:$score")
    }

    private fun addText(infoText : String) {
        val text = Text(infoText, PADDING_X, PADDING_Y + PADDING_Y * texts.size)
        text.setScaling(scaling)
        text.setColor(color)
        texts.add(text)
    }

    fun render(viewportMatrix: FloatArray) {
        for (t in texts) {
            t.render(viewportMatrix)
        }
    }

    fun setColor(color: Int) {
        this.color = color
    }

}