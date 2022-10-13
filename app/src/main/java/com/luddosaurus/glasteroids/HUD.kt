package com.luddosaurus.glasteroids

private const val PADDING_X = 8f
private const val PADDING_Y = 6f
class HUD {
    private val texts = ArrayList<Text>()
    private var scaling = TEXT_SIZE_NORMAL
    private var color = floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f)


    fun update(score: Int, health: Int, fps: Float, gameState: GameState) {
        texts.clear()
        if (gameState == GameState.Active) {
            color = floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f)
            scaling = TEXT_SIZE_NORMAL
            addText("FPS:${fps}")
            addText("HEALTH:$health")
            addText("SCORE:$score")
        } else {
            color = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
            scaling = TEXT_SIZE_LARGE
            addText("GAME OVER!")
            addText("SCORE:$score")
            addText("PRESS TO RESTART")
        }

    }

    private fun addText(infoText : String) {
        val text = Text(infoText, PADDING_X, PADDING_Y + PADDING_Y * texts.size)
        text.setScaling(scaling)
        text.color = color
        texts.add(text)
    }

    fun render(viewportMatrix: FloatArray) {
        for (t in texts) {
            t.render(viewportMatrix)
        }
    }

}