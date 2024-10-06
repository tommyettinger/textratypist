package com.github.tommyettinger

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.github.tommyettinger.freetypist.FreeTypistSkin
import com.github.tommyettinger.textra.FWSkin
import com.github.tommyettinger.textra.Styles
import com.github.tommyettinger.textra.TextraLabel
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TypingLabel
import ktx.app.clearScreen
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.actors
import ktx.scene2d.defaultStyle
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table
import ktx.style.SkinDsl
import ktx.style.addStyle
import ktx.style.defaultStyle
import ktx.style.label
import ktx.style.skin
import ktx.style.set
import ktx.style.get
import kotlin.contracts.ExperimentalContracts

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Main : ApplicationAdapter() {
    private val stage by lazy { Stage() }

    companion object {
        private const val DEFAULT_FONT_SKIN_KEY = "default-font"
    }

    override fun create() {
        super.create()

        val skin = FreeTypistSkin(Gdx.files.internal("uiskin2.json"))
        skin.loadLabelSkin(skin)

        Scene2DSkin.defaultSkin = skin

        stage.addActor(scene2d {
            table {
                align(Align.left)
                width = 100f

                val componentHeight = 100f
                height = componentHeight

                label("Hello, world!") { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("Hello, world!", style = "default", skin)// { labelCell ->
//                    alignment = Align.center
//                    labelCell.height(componentHeight)
//                }
            }
        })
        stage.isDebugAll = true
    }

    override fun render() {
        clearScreen(0f, 0f, 0f, 1f)

        stage.act()
        stage.draw()
    }

    private fun @SkinDsl FWSkin.loadLabelSkin(skin: FWSkin) {
        label {
            font = skin[DEFAULT_FONT_SKIN_KEY]
        }
        label("default") {
            font = skin[DEFAULT_FONT_SKIN_KEY]
        }
    }

    // not used currently; FreeTypistSkin loads TTF fonts from config in the skin.
    private fun loadFontSkin(skin: Skin) {
        // loading the bitmap fonts here because using the asset manager to load and retrieve them
        // is difficult, since they have the same filepath

        val fontPath = "font_main.ttf"
        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal(fontPath))

        val params = FreeTypeFontParameter()
        params.size = 16

        val generatedFont = fontGenerator.generateFont(params).apply {
            data.markupEnabled = true
        }

        generatedFont.getRegion().texture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        )

        skin[DEFAULT_FONT_SKIN_KEY] = generatedFont

        fontGenerator.dispose()
    }
}


@Scene2dDsl
inline fun <S> KWidget<S>.textraLabel(
    text: String,
    style: String = "default",
    skin: Skin = Scene2DSkin.defaultSkin,
    init: (@Scene2dDsl TextraLabel).(S) -> Unit = {},
): TextraLabel {
    return actor(TextraLabel(text, skin, style), init)
}
