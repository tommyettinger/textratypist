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
        var lh = skin.get("bitter-fnt", BitmapFont::class.java).data.lineHeight
        var ch = skin.get("bitter-fnt", BitmapFont::class.java).data.capHeight
//        println(lh)
        skin.get("bitter-fnt", BitmapFont::class.java).data.setScale(20f / lh * ch / lh)
        skin.get("bitter-fnt", Font::class.java).setTextureFilter()
        lh = skin.get("gentium-fnt", BitmapFont::class.java).data.lineHeight
        ch = skin.get("gentium-fnt", BitmapFont::class.java).data.capHeight
//        println(lh)
        skin.get("gentium-fnt", BitmapFont::class.java).data.setScale(20f / lh * ch / lh)
        skin.get("gentium-fnt", Font::class.java).setTextureFilter()
        lh = skin.get("gentium-fnt", BitmapFont::class.java).data.lineHeight
        ch = skin.get("gentium-fnt", BitmapFont::class.java).data.capHeight
        skin.get("gentium-fnt", BitmapFont::class.java).setUseIntegerPositions(false)
        println(lh)
        println(ch)
        skin.get("gentium-dat", BitmapFont::class.java).data.setScale(20f / lh * ch / lh)
        stage.addActor(scene2d {
            table {
                align(Align.left)
                width = 100f

                val componentHeight = 100f
                height = componentHeight

                label("A-Starry fnt BM\n${skin.getFont("astarry-fnt").descent}", style = "astarry", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("A-Starry fnt TT\n${skin.get("astarry-fnt", Font::class.java).descent}", style = "astarry", skin) { labelCell ->
                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                label("A-Starry FT BM\n${skin.getFont("default-font").descent}") { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("A-Starry FT TT\n${skin.get("default-font", Font::class.java).descent}", style = "default", skin) { labelCell ->
                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                row()

                label("OpenSans FT BM\n${skin.getFont("opensans").descent}", style = "opensans", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("OpenSans FT TT\n${skin.get("opensans", Font::class.java).descent}", style = "opensans", skin) { labelCell ->
                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                label("Inconsolata FT BM\n${skin.getFont("inconsolata").descent}", style = "inconsolata", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("Inconsolata FT TT\n${skin.get("inconsolata", Font::class.java).descent}", style = "inconsolata", skin) { labelCell ->
                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                row()

                label("GentiumUI fnt BM\n${skin.getFont("gentium-fnt").descent}", style = "gentium-fnt", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("GentiumUI fnt TT\n${skin.get("gentium-fnt", Font::class.java).descent}", style = "gentium-fnt", skin) { labelCell ->
                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                label("GentiumUI FT BM\n${skin.getFont("gentium").descent}", style = "gentium", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("GentiumUI FT TT\n${skin.get("gentium", Font::class.java).descent}", style = "gentium", skin) { labelCell ->
                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                row()


                label("GentiumUI dat BM\n${skin.getFont("gentium-dat").descent}", style = "gentium-dat", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("GentiumUI dat TT\n${skin.get("gentium-dat", Font::class.java).descent}", style = "gentium-dat", skin) { labelCell ->
                    alignment = Align.center
                    labelCell.height(componentHeight)
                }

                label("Bitter fnt BM\n${skin.getFont("gentium-fnt").descent}", style = "bitter", skin) { labelCell ->
                    labelCell.height(componentHeight)
                }

                textraLabel("Bitter fnt TT\n${skin.get("gentium-fnt", Font::class.java).descent}", style = "bitter", skin) { labelCell ->
                    alignment = Align.center
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
