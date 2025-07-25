/*
 * Copyright (c) 2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ray3k.tgmt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.github.tommyettinger.textra.BitmapFontSupport;
import com.github.tommyettinger.textra.FWSkin;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Styles;

/**
 * A sublass of {@link Skin} (via {@link FWSkin}) that includes a serializer for FreeType fonts from JSON. These JSON
 * files are typically exported by Skin Composer. This can also load Font and BitmapFont objects from .fnt, .json, .dat,
 * .ubj, .json.lzma, and .ubj.lzma files made by FontWriter. Because this extends FWSkin, it is also important when
 * using the styles in {@link Styles}, since it allows reading in a skin JSON's styles both as the scene2d.ui format and
 * as styles for TextraTypist widgets. See the
 * <a href="https://github.com/raeleus/skin-composer/wiki/Creating-FreeType-Fonts#using-a-custom-serializer">Skin Composer documentation</a>.
 */
public class FreeTypistSkin extends FWSkin {
    /** Creates an empty skin. */
    public FreeTypistSkin() {
    }

    /** Creates a skin containing the resources in the specified skin JSON file. If a file in the same directory with a ".atlas"
     * extension exists, it is loaded as a {@link TextureAtlas} and the texture regions added to the skin. The atlas is
     * automatically disposed when the skin is disposed.
     * @param  skinFile The JSON file to be read.
     */
    public FreeTypistSkin(FileHandle skinFile) {
        super(skinFile);

    }

    /** Creates a skin containing the resources in the specified skin JSON file and the texture regions from the specified atlas.
     * The atlas is automatically disposed when the skin is disposed.
     * @param skinFile The JSON file to be read.
     * @param atlas The texture atlas to be associated with the {@link Skin}.
     */
    public FreeTypistSkin(FileHandle skinFile, TextureAtlas atlas) {
        super(skinFile, atlas);
    }

    /** Creates a skin containing the texture regions from the specified atlas. The atlas is automatically disposed when the skin
     * is disposed.
     * @param atlas The texture atlas to be associated with the {@link Skin}.
     */
    public FreeTypistSkin(TextureAtlas atlas) {
        super(atlas);
    }

    /**
     * Overrides the default JSON loader to process FreeType fonts from a Skin JSON.
     * This also allows loading both standard scene2d.ui styles and styles for TextraTypist
     * widgets from the same styles a skin JSON file normally uses.
     *
     * @param skinFile The JSON file to be processed.
     * @return The {@link Json} used to read the file.
     */
    @Override
    protected Json getJsonLoader(final FileHandle skinFile) {
        Json json = super.getJsonLoader(skinFile);
        final Skin skin = this;

        json.setSerializer(Font.class, new Json.ReadOnlySerializer<Font>() {
            @Override
            public Font read(Json json, JsonValue jsonData, Class type) {
                String path = json.readValue("file", String.class, jsonData);

                FileHandle fontFile = skinFile.sibling(path);
                if (!fontFile.exists()) fontFile = Gdx.files.internal(path);
                if (!fontFile.exists()) throw new SerializationException("Font file not found: " + fontFile);

                path = fontFile.path();

                boolean lzb = "dat".equalsIgnoreCase(fontFile.extension());
                boolean js = "json".equalsIgnoreCase(fontFile.extension());
                boolean ubj = "ubj".equalsIgnoreCase(fontFile.extension());
                boolean jslzma = fontFile.name().length() > 10 && ".json.lzma".equalsIgnoreCase(fontFile.name().substring(fontFile.name().length() - 10));
                boolean ublzma = fontFile.name().length() > 9 && ".ubj.lzma".equalsIgnoreCase(fontFile.name().substring(fontFile.name().length() - 9));
                boolean fw = lzb || js || ubj || jslzma || ublzma;

                float scaledSize = json.readValue("scaledSize", float.class, -1f, jsonData);
                float xAdjust = json.readValue("xAdjust", float.class, 0f, jsonData);
                float yAdjust = json.readValue("yAdjust", float.class, 0f, jsonData);
                float widthAdjust = json.readValue("widthAdjust", float.class, 0f, jsonData);
                float heightAdjust = json.readValue("heightAdjust", float.class, 0f, jsonData);
                // This defaults to false, which is not what Skin normally defaults to.
                Boolean useIntegerPositions = json.readValue("useIntegerPositions", Boolean.class, false, jsonData);
                // This defaults to true, because anything FontWriter produces is compatible with makeGridGlyphs.
                Boolean makeGridGlyphs = json.readValue("makeGridGlyphs", Boolean.class, true, jsonData);


                // Use a region with the same name as the font, else use a PNG file in the same directory as the font file.
                int nameStart = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'))+1;
                String regionName = path.substring(nameStart, Math.max(0, path.indexOf('.', nameStart)));
                try {
                    Font font;
                    Array<TextureRegion> regions = skin.getRegions(regionName);
                    if (regions != null && regions.notEmpty()) {
                        if(fw)
                            font = new Font(fontFile, regions.first(), xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, true);
                        else
                            font = new Font(path, regions, Font.DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
                    } else {
                        TextureRegion region = skin.optional(regionName, TextureRegion.class);
                        if (region != null)
                        {
                            if(fw)
                                font = new Font(fontFile, region, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, true);
                            else
                                font = new Font(path, region, Font.DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
                        }
                        else {
                            FileHandle imageFile = Gdx.files.internal(path).sibling(regionName + ".png");
                            if (imageFile.exists()) {
                                if(fw)
                                    font = new Font(fontFile, new TextureRegion(new Texture(imageFile)), xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, true);
                                else
                                    font = new Font(path, new TextureRegion(new Texture(imageFile)), Font.DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
                            } else {
                                if(fw)
                                    throw new RuntimeException("Missing image file or TextureRegion.");
                                else
                                    font = new Font(path);
                            }
                        }
                    }
                    font.useIntegerPositions(useIntegerPositions);
                    // Scaled size is the desired cap height to scale the font to.
                    if (scaledSize != -1) font.scaleHeightTo(scaledSize);
                    return font;
                } catch (RuntimeException ex) {
                    throw new SerializationException("Error loading bitmap font: " + path, ex);
                }
            }
        });

        json.setSerializer(BitmapFont.class, new Json.ReadOnlySerializer<BitmapFont>() {
            public BitmapFont read (Json json, JsonValue jsonData, Class type) {
                String path = json.readValue("file", String.class, jsonData);

                FileHandle fontFile = skinFile.sibling(path);
                if (!fontFile.exists()) fontFile = Gdx.files.internal(path);
                if (!fontFile.exists()) throw new SerializationException("Font file not found: " + fontFile);

                boolean lzb = "dat".equalsIgnoreCase(fontFile.extension());
                boolean js = "json".equalsIgnoreCase(fontFile.extension());
                boolean ubj = "ubj".equalsIgnoreCase(fontFile.extension());
                boolean jslzma = fontFile.name().length() > 10 && ".json.lzma".equalsIgnoreCase(fontFile.name().substring(fontFile.name().length() - 10));
                boolean ublzma = fontFile.name().length() > 9 && ".ubj.lzma".equalsIgnoreCase(fontFile.name().substring(fontFile.name().length() - 9));
                boolean fw = lzb || js || ubj || jslzma || ublzma;

                float scaledSize = json.readValue("scaledSize", float.class, -1f, jsonData);
                Boolean flip = json.readValue("flip", Boolean.class, false, jsonData);
                Boolean markupEnabled = json.readValue("markupEnabled", Boolean.class, true, jsonData);
                // This defaults to false, which is not what Skin normally defaults to.
                // You can set it to true if you expect a BitmapFont to be used at pixel-perfect 100% zoom only.
                Boolean useIntegerPositions = json.readValue("useIntegerPositions", Boolean.class, false, jsonData);
                float xAdjust = json.readValue("xAdjust", float.class, 0f, jsonData);
                float yAdjust = json.readValue("yAdjust", float.class, 0f, jsonData);
                float widthAdjust = json.readValue("widthAdjust", float.class, 0f, jsonData);
                float heightAdjust = json.readValue("heightAdjust", float.class, 0f, jsonData);
                // This defaults to true, because anything FontWriter produces is compatible with makeGridGlyphs.
                Boolean makeGridGlyphs = json.readValue("makeGridGlyphs", Boolean.class, true, jsonData);

                // Use a region with the same name as the font, else use a PNG file in the same directory as the font file.
                int nameStart = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'))+1;
                String regionName = path.substring(nameStart, Math.max(0, path.indexOf('.', nameStart)));
                try {
                    BitmapFont bitmapFont;
                    Font font;
                    Array<TextureRegion> regions = skin.getRegions(regionName);
                    if (regions != null && regions.notEmpty()) {
                        if(fw) {
                            bitmapFont = BitmapFontSupport.loadStructuredJson(fontFile, regions.first(), flip);
                            font = new Font(fontFile, regions.first(), xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, true);
                        }
                        else {
                            bitmapFont = new BitmapFont(new BitmapFont.BitmapFontData(fontFile, flip), regions, true);
                            font = new Font(fontFile, regions, Font.DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
                        }
                    } else {
                        TextureRegion region = skin.optional(regionName, TextureRegion.class);
                        if (region != null)
                        {
                            if(fw) {
                                bitmapFont = BitmapFontSupport.loadStructuredJson(fontFile, region, flip);
                                font = new Font(fontFile, region, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, true);
                            }
                            else {
                                bitmapFont = new BitmapFont(fontFile, region, flip);
                                font = new Font(fontFile, region, Font.DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
                            }
                        }
                        else {
                            FileHandle imageFile = fontFile.sibling(regionName + ".png");
                            if (imageFile.exists()) {
                                region = new TextureRegion(new Texture(imageFile));
                                if(fw) {
                                    bitmapFont = BitmapFontSupport.loadStructuredJson(fontFile, region, flip);
                                    font = new Font(fontFile, region, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs, true);
                                } else {
                                    bitmapFont = new BitmapFont(fontFile, region, flip);
                                    font = new Font(path, region, Font.DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs);
                                }
                            } else {
                                if(fw)
                                    throw new RuntimeException("Missing image file or TextureRegion.");
                                else {
                                    bitmapFont = new BitmapFont(fontFile, flip);
                                    font = new Font(path);
                                }
                            }
                        }
                    }
                    bitmapFont.getData().markupEnabled = markupEnabled;
                    bitmapFont.setUseIntegerPositions(useIntegerPositions);
                    font.useIntegerPositions(useIntegerPositions);
                    // For BitmapFont, scaled size is the desired cap height to scale the font to.
                    // For Font, scaled size is the desired line height to scale the font to.
                    // These generally do not exactly agree, but we can get close enough by using a
                    // smaller scale for the BitmapFont.
                    if (scaledSize != -1) {
                        bitmapFont.getData().setScale(scaledSize / bitmapFont.getLineHeight());
                        font.scaleHeightTo(scaledSize);
                    }

                    skin.add(jsonData.name, font, Font.class);

                    return bitmapFont;
                } catch (RuntimeException ex) {
                    throw new SerializationException("Error loading bitmap font: " + fontFile, ex);
                }
            }
        });

        json.setSerializer(FreeTypeFontGenerator.class, new Json.ReadOnlySerializer<FreeTypeFontGenerator>() {
            @Override
            public FreeTypeFontGenerator read(Json json,
                                              JsonValue jsonData, Class type) {
                String path = json.readValue("font", String.class, jsonData);
                jsonData.remove("font");

                FreeTypeFontGenerator.Hinting hinting = FreeTypeFontGenerator.Hinting.valueOf(json.readValue("hinting",
                        String.class, "Medium", jsonData));
                jsonData.remove("hinting");

                Texture.TextureFilter minFilter = Texture.TextureFilter.valueOf(
                        json.readValue("minFilter", String.class, "Nearest", jsonData));
                jsonData.remove("minFilter");

                Texture.TextureFilter magFilter = Texture.TextureFilter.valueOf(
                        json.readValue("magFilter", String.class, "Linear", jsonData));
                jsonData.remove("magFilter");

                Boolean markupEnabled = json.readValue("markupEnabled", Boolean.class, true, jsonData);
                jsonData.remove("markupEnabled");
                // This defaults to false, which is not what Skin normally defaults to.
                // You can set it to true if you expect a BitmapFont to be used at pixel-perfect 100% zoom only.
                Boolean useIntegerPositions = json.readValue("useIntegerPositions", Boolean.class, false, jsonData);
                jsonData.remove("useIntegerPositions");
                float xAdjust = json.readValue("xAdjust", float.class, 0f, jsonData);
                jsonData.remove("xAdjust");
                float yAdjust = json.readValue("yAdjust", float.class, 0f, jsonData);
                jsonData.remove("yAdjust");
                float widthAdjust = json.readValue("widthAdjust", float.class, 0f, jsonData);
                jsonData.remove("widthAdjust");
                float heightAdjust = json.readValue("heightAdjust", float.class, 0f, jsonData);
                jsonData.remove("heightAdjust");
                Boolean makeGridGlyphs = json.readValue("makeGridGlyphs", Boolean.class, true, jsonData);
                jsonData.remove("makeGridGlyphs");

                FreeTypeFontGenerator.FreeTypeFontParameter parameter = json.readValue(FreeTypeFontGenerator.FreeTypeFontParameter.class, jsonData);
                parameter.hinting = hinting;
                parameter.minFilter = minFilter;
                parameter.magFilter = magFilter;
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(skinFile.sibling(path));
                FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);
                BitmapFont font = generator.generateFont(parameter);
                font.getData().markupEnabled = markupEnabled;
                font.setUseIntegerPositions(useIntegerPositions);
                skin.add(jsonData.name, font);
                skin.add(jsonData.name, new Font(font, Font.DistanceFieldType.STANDARD, xAdjust, yAdjust, widthAdjust, heightAdjust, makeGridGlyphs));
                if (parameter.incremental) {
                    generator.dispose();
                    return null;
                } else {
                    return generator;
                }
            }
        });

        json.setSerializer(Label.LabelStyle.class, new Json.ReadOnlySerializer<Label.LabelStyle>() {
            @Override
            public Label.LabelStyle read(Json json, JsonValue jsonData, Class type) {
                Label.LabelStyle s2d = new Label.LabelStyle();
                json.readFields(s2d, jsonData);
                Styles.LabelStyle stt = new Styles.LabelStyle(skin.get(json.readValue("font", String.class, "default-font", jsonData), Font.class),
                        s2d.fontColor);
                stt.background = s2d.background;
                skin.add(jsonData.name, stt, Styles.LabelStyle.class);
                return s2d;
            }
        });

        json.setSerializer(TextButton.TextButtonStyle.class, new Json.ReadOnlySerializer<TextButton.TextButtonStyle>() {
            @Override
            public TextButton.TextButtonStyle read(Json json, JsonValue jsonData, Class type) {
                TextButton.TextButtonStyle s2d = new TextButton.TextButtonStyle();
                json.readFields(s2d, jsonData);
                Styles.TextButtonStyle stt = new Styles.TextButtonStyle(s2d.up, s2d.down, s2d.checked,
                        skin.get(json.readValue("font", String.class, "default-font", jsonData), Font.class));

                if (s2d.fontColor != null) stt.fontColor = new Color(s2d.fontColor);
                if (s2d.downFontColor != null) stt.downFontColor = new Color(s2d.downFontColor);
                if (s2d.overFontColor != null) stt.overFontColor = new Color(s2d.overFontColor);
                if (s2d.focusedFontColor != null) stt.focusedFontColor = new Color(s2d.focusedFontColor);
                if (s2d.disabledFontColor != null) stt.disabledFontColor = new Color(s2d.disabledFontColor);

                if (s2d.checkedFontColor != null) stt.checkedFontColor = new Color(s2d.checkedFontColor);
                if (s2d.checkedDownFontColor != null) stt.checkedDownFontColor = new Color(s2d.checkedDownFontColor);
                if (s2d.checkedOverFontColor != null) stt.checkedOverFontColor = new Color(s2d.checkedOverFontColor);
                if (s2d.checkedFocusedFontColor != null) stt.checkedFocusedFontColor = new Color(s2d.checkedFocusedFontColor);
                skin.add(jsonData.name, stt, Styles.TextButtonStyle.class);
                return s2d;
            }
        });

        json.setSerializer(ImageTextButton.ImageTextButtonStyle.class, new Json.ReadOnlySerializer<ImageTextButton.ImageTextButtonStyle>() {
            @Override
            public ImageTextButton.ImageTextButtonStyle read(Json json, JsonValue jsonData, Class type) {
                ImageTextButton.ImageTextButtonStyle s2d = new ImageTextButton.ImageTextButtonStyle();
                json.readFields(s2d, jsonData);
                Styles.ImageTextButtonStyle stt = new Styles.ImageTextButtonStyle(s2d.up, s2d.down, s2d.checked,
                        skin.get(json.readValue("font", String.class, "default-font", jsonData), Font.class));
                if (s2d.fontColor != null) stt.fontColor = new Color(s2d.fontColor);
                if (s2d.downFontColor != null) stt.downFontColor = new Color(s2d.downFontColor);
                if (s2d.overFontColor != null) stt.overFontColor = new Color(s2d.overFontColor);
                if (s2d.focusedFontColor != null) stt.focusedFontColor = new Color(s2d.focusedFontColor);
                if (s2d.disabledFontColor != null) stt.disabledFontColor = new Color(s2d.disabledFontColor);

                if (s2d.checkedFontColor != null) stt.checkedFontColor = new Color(s2d.checkedFontColor);
                if (s2d.checkedDownFontColor != null) stt.checkedDownFontColor = new Color(s2d.checkedDownFontColor);
                if (s2d.checkedOverFontColor != null) stt.checkedOverFontColor = new Color(s2d.checkedOverFontColor);
                if (s2d.checkedFocusedFontColor != null) stt.checkedFocusedFontColor = new Color(s2d.checkedFocusedFontColor);

                stt.imageUp = s2d.imageUp;
                stt.imageDown = s2d.imageDown;
                stt.imageOver = s2d.imageOver;
                stt.imageDisabled = s2d.imageDisabled;
                stt.imageChecked = s2d.imageChecked;
                stt.imageCheckedDown = s2d.imageCheckedDown;
                stt.imageCheckedOver = s2d.imageCheckedOver;

                skin.add(jsonData.name, stt, Styles.ImageTextButtonStyle.class);
                return s2d;
            }
        });

        json.setSerializer(CheckBox.CheckBoxStyle.class, new Json.ReadOnlySerializer<CheckBox.CheckBoxStyle>() {
            @Override
            public CheckBox.CheckBoxStyle read(Json json, JsonValue jsonData, Class type) {
                CheckBox.CheckBoxStyle s2d = new CheckBox.CheckBoxStyle();
                json.readFields(s2d, jsonData);

                Styles.CheckBoxStyle stt = new Styles.CheckBoxStyle(s2d.checkboxOff, s2d.checkboxOn,
                        skin.get(json.readValue("font", String.class, "default-font", jsonData), Font.class), s2d.fontColor);

                if (s2d.fontColor != null) stt.fontColor = new Color(s2d.fontColor);
                if (s2d.downFontColor != null) stt.downFontColor = new Color(s2d.downFontColor);
                if (s2d.overFontColor != null) stt.overFontColor = new Color(s2d.overFontColor);
                if (s2d.focusedFontColor != null) stt.focusedFontColor = new Color(s2d.focusedFontColor);
                if (s2d.disabledFontColor != null) stt.disabledFontColor = new Color(s2d.disabledFontColor);

                if (s2d.checkedFontColor != null) stt.checkedFontColor = new Color(s2d.checkedFontColor);
                if (s2d.checkedDownFontColor != null) stt.checkedDownFontColor = new Color(s2d.checkedDownFontColor);
                if (s2d.checkedOverFontColor != null) stt.checkedOverFontColor = new Color(s2d.checkedOverFontColor);
                if (s2d.checkedFocusedFontColor != null) stt.checkedFocusedFontColor = new Color(s2d.checkedFocusedFontColor);

                stt.checkboxOnOver = s2d.checkboxOnOver;
                stt.checkboxOver = s2d.checkboxOver;
                stt.checkboxOnDisabled = s2d.checkboxOnDisabled;
                stt.checkboxOffDisabled = s2d.checkboxOffDisabled;

                skin.add(jsonData.name, stt, Styles.CheckBoxStyle.class);
                return s2d;
            }
        });

        json.setSerializer(Window.WindowStyle.class, new Json.ReadOnlySerializer<Window.WindowStyle>() {
            @Override
            public Window.WindowStyle read(Json json, JsonValue jsonData, Class type) {
                Window.WindowStyle s2d = new Window.WindowStyle();
                json.readFields(s2d, jsonData);
                Styles.WindowStyle stt = new Styles.WindowStyle(skin.get(json.readValue("titleFont", String.class, "default-font", jsonData), Font.class),
                        s2d.titleFontColor, s2d.background);
                stt.stageBackground = s2d.stageBackground;
                skin.add(jsonData.name, stt, Styles.WindowStyle.class);
                return s2d;
            }
        });


        json.setSerializer(TextTooltip.TextTooltipStyle.class, new Json.ReadOnlySerializer<TextTooltip.TextTooltipStyle>() {
            @Override
            public TextTooltip.TextTooltipStyle read(Json json, JsonValue jsonData, Class type) {
                TextTooltip.TextTooltipStyle s2d = new TextTooltip.TextTooltipStyle();
                json.readFields(s2d, jsonData);
                String labelStyleName = json.readValue("label", String.class, "default", jsonData);
                if (labelStyleName == null) {
                    Label.LabelStyle style = json.readValue("label", Label.LabelStyle.class, jsonData);
                    Styles.TextTooltipStyle tt = new Styles.TextTooltipStyle(style, s2d.background);
                    tt.wrapWidth = s2d.wrapWidth;
                    skin.add(jsonData.name, tt, Styles.TextTooltipStyle.class);
                } else {
                    Styles.TextTooltipStyle tt = new Styles.TextTooltipStyle(skin.get(labelStyleName, Styles.LabelStyle.class),
                            s2d.background);
                    tt.wrapWidth = s2d.wrapWidth;
                    skin.add(jsonData.name, tt, Styles.TextTooltipStyle.class);
                }
                return s2d;
            }
        });

        json.setSerializer(List.ListStyle.class, new Json.ReadOnlySerializer<List.ListStyle>() {
            @Override
            public List.ListStyle read(Json json, JsonValue jsonData, Class type) {
                List.ListStyle s2d = new List.ListStyle();
                json.readFields(s2d, jsonData);
                Styles.ListStyle stt = new Styles.ListStyle(skin.get(json.readValue("font", String.class, "default-font", jsonData), Font.class),
                        s2d.fontColorSelected, s2d.fontColorUnselected, s2d.selection);
                stt.background = s2d.background;
                stt.down = s2d.down;
                stt.over = s2d.over;
                skin.add(jsonData.name, stt, Styles.ListStyle.class);
                return s2d;
            }
        });


        json.setSerializer(SelectBox.SelectBoxStyle.class, new Json.ReadOnlySerializer<SelectBox.SelectBoxStyle>() {
            @Override
            public SelectBox.SelectBoxStyle read(Json json, JsonValue jsonData, Class type) {
                SelectBox.SelectBoxStyle s2d = new SelectBox.SelectBoxStyle();
                json.readFields(s2d, jsonData);
                String scrollStyleName = json.readValue("scrollStyle", String.class, "default", jsonData);
                ScrollPane.ScrollPaneStyle sps;
                if (scrollStyleName == null) {
                    sps = json.readValue("scrollStyle", ScrollPane.ScrollPaneStyle.class, jsonData);
                } else {
                    sps = skin.get(scrollStyleName, ScrollPane.ScrollPaneStyle.class);
                }

                String listStyleName = json.readValue("listStyle", String.class, "default", jsonData);
                Styles.ListStyle ls;
                if (listStyleName == null) {
                    List.ListStyle ls2d = new List.ListStyle();
                    json.readFields(ls2d, jsonData.get("listStyle"));
                    ls = new Styles.ListStyle(skin.get(json.readValue("font", String.class, "default-font", jsonData), Font.class),
                            ls2d.fontColorSelected, ls2d.fontColorUnselected, ls2d.selection);
                    ls.background = ls2d.background;
                    ls.down = ls2d.down;
                    ls.over = ls2d.over;
                } else {
                    ls = skin.get(listStyleName, Styles.ListStyle.class);
                }

                Styles.SelectBoxStyle stt = new Styles.SelectBoxStyle(skin.get(json.readValue("font", String.class, "default-font", jsonData), Font.class),
                        s2d.fontColor, s2d.background, sps, ls);
                stt.background = s2d.background;
                stt.backgroundDisabled = s2d.backgroundDisabled;
                stt.backgroundOpen = s2d.backgroundOpen;
                stt.backgroundOver = s2d.backgroundOver;
                stt.disabledFontColor = s2d.disabledFontColor;
                stt.overFontColor = s2d.overFontColor;
                skin.add(jsonData.name, stt, Styles.SelectBoxStyle.class);
                return s2d;
            }
        });

        return json;
    }
}
