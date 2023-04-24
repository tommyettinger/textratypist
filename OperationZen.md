# The Problem

It is too hard right now to configure the many various aspects of a font.

Because anything you might want to adjust is affected by shared metrics, it's nearly impossible to make a small change
without causing a chain reaction.

Strikethrough and underline are stuck together and update in lockstep. They're affected by the x and y offsets, but
those also affect inline image (usually emoji) positions. Emoji may need different position adjustments than the
game-icons.net images, and right now there's no way for a Font to specify that emoji should be some distance away.

The measurement systems for fonts are a total mess, with many parts operating on the numbers as used in a .fnt file
(without any scaling), but some operating on the current scaled size or working with a multiplier on one of those sizes.

# Commencing Operation: Zen

The goal here is to keep the old ways of adjusting fonts where possible, but to allow additional configuration on every
little detail of a font's metrics, and to have the new configuration consistently use fractions of the displayed size
as its unit of measure.

I feel like I need a fancy codename here because this is a large and important section for the code changes soon.

A checklist:
 - I think we should separate strikethrough and underline configuration.
   - Allow each to have x and y positions and sizes configured, including thickness.
 - Store configurations for atlases on a per-atlas basis, using a keyword to identify an atlas.
   - Only a small amount of configuration is needed for each atlas, probably x and y offsets, plus x advance.
 - General font metrics should be configurable by normal human beings, not just experts in this weird system.
 - We may want to look into making a new file format to save this information outside of code.
   - This isn't necessary, probably, but would allow storing more data than .fnt might permit.
   - It's possible .fnt is extensible enough already that we wouldn't need this.
   - Using a standard file format, such as JSON, may have real advantages.
     - AngelCode BMFont is fairly standard already, though. 

