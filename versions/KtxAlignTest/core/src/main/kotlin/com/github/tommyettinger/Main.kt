package com.github.tommyettinger

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import com.github.tommyettinger.freetypist.FreeTypistSkin
import com.github.tommyettinger.textra.FWSkin
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TextraLabel
import ktx.app.clearScreen
import ktx.scene2d.*
import ktx.style.SkinDsl
import ktx.style.get
import ktx.style.label
import ktx.style.set

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
//        val ht = skin.get("bitter-fnt", BitmapFont::class.java).data.capHeight
//        println(ht)
//        skin.get("bitter-fnt", BitmapFont::class.java).data.scale(20f / ht)
        skin.get("bitter-fnt", Font::class.java).setTextureFilter().scaleHeightTo(20f)
        stage.addActor(scene2d {
            table {
                align(Align.left)
                width = 100f

                val componentHeight = 100f
                height = componentHeight

                label("A-Starry fnt BM", style = "astarry", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("A-Starry fnt TT", style = "astarry", skin) { labelCell ->
////                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                label("A-Starry FT BM") { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("A-Starry FT TT", style = "default", skin) { labelCell ->
//                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                row()

                label("OpenSans FT BM", style = "opensans", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("OpenSans FT TT", style = "opensans", skin) { labelCell ->
//                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                label("Inconsolata FT BM", style = "inconsolata", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("Inconsolata FT TT", style = "inconsolata", skin) { labelCell ->
//                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                row()

                label("Bitter fnt BM", style = "bitter", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("Bitter fnt TT", style = "bitter", skin) { labelCell ->
//                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                row()
                setFillParent(true)
                center()
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
