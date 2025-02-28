package com.unciv.ui.screens.multiplayerscreens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.logic.IdChecker
import com.unciv.models.translations.tr
import com.unciv.ui.screens.pickerscreens.PickerScreen
import com.unciv.ui.popups.Popup
import com.unciv.ui.popups.ToastPopup
import com.unciv.ui.screens.savescreens.LoadGameScreen
import com.unciv.ui.components.UncivTextField
import com.unciv.ui.components.extensions.enable
import com.unciv.ui.components.extensions.onClick
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.components.extensions.toTextButton
import com.unciv.utils.Concurrency
import com.unciv.utils.launchOnGLThread
import java.util.UUID

class AddMultiplayerGameScreen : PickerScreen() {
    init {
        val gameNameTextField = UncivTextField.create("Game name")
        val gameIDTextField = UncivTextField.create("GameID")
        val pasteGameIDButton = "Paste gameID from clipboard".toTextButton()
        pasteGameIDButton.onClick {
            gameIDTextField.text = Gdx.app.clipboard.contents
        }

        topTable.add("GameID".toLabel()).row()
        val gameIDTable = Table()
        gameIDTable.add(gameIDTextField).pad(10f).width(2 * stage.width / 3 - pasteGameIDButton.width)
        gameIDTable.add(pasteGameIDButton)
        topTable.add(gameIDTable).padBottom(30f).row()

        topTable.add("Game name".toLabel()).row()
        topTable.add(gameNameTextField).pad(10f).padBottom(30f).width(stage.width / 2).row()

        //CloseButton Setup
        closeButton.setText("Back".tr())
        closeButton.onClick {
            game.popScreen()
        }

        //RightSideButton Setup
        rightSideButton.setText("Save game".tr())
        rightSideButton.enable()
        rightSideButton.onClick {
            try {
                UUID.fromString(IdChecker.checkAndReturnGameUuid(gameIDTextField.text))
            } catch (_: Exception) {
                ToastPopup("Invalid game ID!", this)
                return@onClick
            }

            val popup = Popup(this)
            popup.addGoodSizedLabel("Working...")
            popup.open()

            Concurrency.run("AddMultiplayerGame") {
                try {
                    game.onlineMultiplayer.addGame(gameIDTextField.text.trim(), gameNameTextField.text.trim())
                    launchOnGLThread {
                        popup.close()
                        game.popScreen()
                    }
                } catch (ex: Exception) {
                    val (message) = LoadGameScreen.getLoadExceptionMessage(ex)
                    launchOnGLThread {
                        popup.reuseWith(message, true)
                    }
                }
            }
        }
    }
}
