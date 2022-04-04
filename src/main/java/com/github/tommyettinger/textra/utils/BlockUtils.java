/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.tommyettinger.textra.utils;

public class BlockUtils {

    public static final float THIN_START = 0.45f;
    public static final float THIN_END = 0.55f;
    public static final float THIN_ACROSS = 0.1f;

    public static final float THICK_START = 0.4f;
    public static final float THICK_END = 0.6f;
    public static final float THICK_ACROSS = 0.6f;

    public static final float DOUBLE_START1 = 0.35f;
    public static final float DOUBLE_END1 = 0.45f;
    public static final float DOUBLE_START2 = 0.55f;
    public static final float DOUBLE_END2 = 0.65f;
    public static final float DOUBLE_ACROSS = 0.1f;

    /**
     * Returns true if the given char can be handled by the box drawing data here, or false if the Font should try to
     * handle that char itself.
     * @param c a char (or int in the char range, 0-65535) that could potentially be a box drawing char
     * @return true if c is a box drawing char that this can handle
     */
    public static boolean isBoxDrawing(int c){
        return false; // TODO: enable this when we can draw using the float[] items in BOX_DRAWING.
//        return (c >= '\u2500' && c <= '\u2503');
    }

    // So, what's the plan here...
    // Each glyph that isn't commented out here can be drawn using only axis-aligned rectangles.
    // The idea is to list the rectangles needed to draw this character, measured in fractions of a cell, so monospace
    // fonts that don't otherwise line up correctly can be made to fill a square (or other) cell.
    // The format is left x, lower y, width, height, ... (may have more rectangles in groups of four).
    public static final float[][] BOX_DRAWING = new float[][]{
            /* ─ : u2500 */ {0, THIN_START, 1, THIN_ACROSS},
            /* ━ : u2501 */ {0, THICK_START, 1, THICK_ACROSS},
            /* │ : u2502 */ {THIN_START, 0, THIN_ACROSS, 1},
            /* ┃ : u2503 */ {THICK_START, 0, THICK_ACROSS, 1},
            /* ┄ : u2504 */ {},
            /* ┅ : u2505 */ {},
            /* ┆ : u2506 */ {},
            /* ┇ : u2507 */ {},
            /* ┈ : u2508 */ {},
            /* ┉ : u2509 */ {},
            /* ┊ : u250A */ {},
            /* ┋ : u250B */ {},
            /* ┌ : u250C */ {},
            /* ┍ : u250D */ {},
            /* ┎ : u250E */ {},
            /* ┏ : u250F */ {},
            /* ┐ : u2510 */ {},
            /* ┑ : u2511 */ {},
            /* ┒ : u2512 */ {},
            /* ┓ : u2513 */ {},
            /* └ : u2514 */ {},
            /* ┕ : u2515 */ {},
            /* ┖ : u2516 */ {},
            /* ┗ : u2517 */ {},
            /* ┘ : u2518 */ {},
            /* ┙ : u2519 */ {},
            /* ┚ : u251A */ {},
            /* ┛ : u251B */ {},
            /* ├ : u251C */ {},
            /* ┝ : u251D */ {},
            /* ┞ : u251E */ {},
            /* ┟ : u251F */ {},
            /* ┠ : u2520 */ {},
            /* ┡ : u2521 */ {},
            /* ┢ : u2522 */ {},
            /* ┣ : u2523 */ {},
            /* ┤ : u2524 */ {},
            /* ┥ : u2525 */ {},
            /* ┦ : u2526 */ {},
            /* ┧ : u2527 */ {},
            /* ┨ : u2528 */ {},
            /* ┩ : u2529 */ {},
            /* ┪ : u252A */ {},
            /* ┫ : u252B */ {},
            /* ┬ : u252C */ {},
            /* ┭ : u252D */ {},
            /* ┮ : u252E */ {},
            /* ┯ : u252F */ {},
            /* ┰ : u2530 */ {},
            /* ┱ : u2531 */ {},
            /* ┲ : u2532 */ {},
            /* ┳ : u2533 */ {},
            /* ┴ : u2534 */ {},
            /* ┵ : u2535 */ {},
            /* ┶ : u2536 */ {},
            /* ┷ : u2537 */ {},
            /* ┸ : u2538 */ {},
            /* ┹ : u2539 */ {},
            /* ┺ : u253A */ {},
            /* ┻ : u253B */ {},
            /* ┼ : u253C */ {},
            /* ┽ : u253D */ {},
            /* ┾ : u253E */ {},
            /* ┿ : u253F */ {},
            /* ╀ : u2540 */ {},
            /* ╁ : u2541 */ {},
            /* ╂ : u2542 */ {},
            /* ╃ : u2543 */ {},
            /* ╄ : u2544 */ {},
            /* ╅ : u2545 */ {},
            /* ╆ : u2546 */ {},
            /* ╇ : u2547 */ {},
            /* ╈ : u2548 */ {},
            /* ╉ : u2549 */ {},
            /* ╊ : u254A */ {},
            /* ╋ : u254B */ {},
            /* ╌ : u254C */ {},
            /* ╍ : u254D */ {},
            /* ╎ : u254E */ {},
            /* ╏ : u254F */ {},
            /* ═ : u2550 */ {},
            /* ║ : u2551 */ {},
            /* ╒ : u2552 */ {},
            /* ╓ : u2553 */ {},
            /* ╔ : u2554 */ {},
            /* ╕ : u2555 */ {},
            /* ╖ : u2556 */ {},
            /* ╗ : u2557 */ {},
            /* ╘ : u2558 */ {},
            /* ╙ : u2559 */ {},
            /* ╚ : u255A */ {},
            /* ╛ : u255B */ {},
            /* ╜ : u255C */ {},
            /* ╝ : u255D */ {},
            /* ╞ : u255E */ {},
            /* ╟ : u255F */ {},
            /* ╠ : u2560 */ {},
            /* ╡ : u2561 */ {},
            /* ╢ : u2562 */ {},
            /* ╣ : u2563 */ {},
            /* ╤ : u2564 */ {},
            /* ╥ : u2565 */ {},
            /* ╦ : u2566 */ {},
            /* ╧ : u2567 */ {},
            /* ╨ : u2568 */ {},
            /* ╩ : u2569 */ {},
            /* ╪ : u256A */ {},
            /* ╫ : u256B */ {},
            /* ╬ : u256C */ {},
            /* ╭ : u256D */ {}, // NOT USED
            /* ╮ : u256E */ {}, // NOT USED
            /* ╯ : u256F */ {}, // NOT USED
            /* ╰ : u2570 */ {}, // NOT USED
            /* ╱ : u2571 */ {}, // NOT USED
            /* ╲ : u2572 */ {}, // NOT USED
            /* ╳ : u2573 */ {}, // NOT USED
            /* ╴ : u2574 */ {},
            /* ╵ : u2575 */ {},
            /* ╶ : u2576 */ {},
            /* ╷ : u2577 */ {},
            /* ╸ : u2578 */ {},
            /* ╹ : u2579 */ {},
            /* ╺ : u257A */ {},
            /* ╻ : u257B */ {},
            /* ╼ : u257C */ {},
            /* ╽ : u257D */ {},
            /* ╾ : u257E */ {},
            /* ╿ : u257F */ {},
    };
}
