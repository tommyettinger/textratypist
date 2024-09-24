package com.github.tommyettinger

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import com.github.tommyettinger.textra.*

import ktx.scene2d.*

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Main : ApplicationAdapter() {
    lateinit var stage : Stage
    lateinit var skin : FWSkin
    override fun create() {
        skin = FWSkin(Gdx.files.internal("uiskin.json"))
        stage = Stage()
        table {
            background = skin.getDrawable("tree-minus")

            it.align(Align.left)
            align(Align.left)


            it.width(width)
            val componentHeight = 60f
            it.height(componentHeight)

            table {
                it.padLeft(10f)
                it.align(Align.left)
                it.height(componentHeight)

                table {
                    it.align(Align.left)
                    align(Align.left)

                    image(skin.getDrawable("tree-plus"))

                    label("") { labelCell ->
                        labelCell.height(componentHeight)
                    }
                }
            }
        }
    }
}
