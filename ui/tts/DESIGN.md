# Design System Specification: The Scholar’s Manuscript

## 1. Overview & Creative North Star
The Creative North Star for this design system is **"The Digital Manuscript."** This system moves away from the "app-like" density of modern web design, instead drawing inspiration from the prestigious, well-structured layouts of high-end academic journals and technical manuscripts. 

To break the "template" look, we utilize **Intentional Asymmetry**. Rather than a perfectly centered grid, we lean into generous, "breathable" left-hand margins (using our `24` spacing token) and offset content blocks. This creates an editorial flow that guides the reader through complex information with scholarly authority and modern lightness.

## 2. Colors & Surface Architecture
The color palette is rooted in a deep, intellectual blue (`primary: #003f87`) set against a pristine, tiered neutral foundation.

### The "No-Line" Rule
Explicitly prohibit the use of 1px solid borders for sectioning. Structural boundaries must be defined solely through background shifts. Use `surface-container-low` for secondary content areas sitting atop a `surface` background. This creates a "wash" of color that feels organic rather than mechanical.

### Surface Hierarchy & Nesting
Treat the UI as a series of stacked, physical sheets of fine paper.
- **Base Layer:** `surface` (#f7f9fb)
- **Content Blocks:** `surface-container-lowest` (#ffffff) to provide a "bright" focus area for text.
- **Inset Elements:** Use `surface-container` (#eceef0) for code blocks or blockquotes to create a recessed, tactile feel.

### The "Glass & Signature Texture" Rule
To add a premium "soul" to the manuscript:
- **Floating Navigation:** Use Glassmorphism. Apply `surface-container-lowest` at 80% opacity with a `20px` backdrop-blur.
- **Signature CTAs:** Do not use flat fills. Use a subtle linear gradient from `primary` (#003f87) to `primary_container` (#0056b3) at a 135-degree angle. This adds a "silk" sheen to interactive elements.

## 3. Typography: The Bilingual Hierarchy
The system balances the geometric precision of **Inter** for UI elements with the optimized legibility of **PingFang SC** (or Noto Sans SC) for long-form Chinese text.

- **Display Scale (`display-lg` to `display-sm`):** Reserved for article titles and major section headers. Use a `font-weight: 600` for Inter and `font-weight: 500` for Chinese characters to ensure they don't appear "muddy."
- **The Scholarly Body (`body-lg`):** Our standard for reading. Chinese characters require more breathing room; set `line-height: 1.8` and `letter-spacing: 0.02em`.
- **Labels & Captions (`label-md`):** Used for metadata (dates, tags). Use `on_surface_variant` (#424752) to keep these secondary to the main narrative.

## 4. Elevation & Depth
Depth is achieved through **Tonal Layering** rather than structural lines.

- **The Layering Principle:** Place a `surface-container-lowest` card on a `surface-container-low` section. The contrast in "whiteness" provides enough visual separation without needing a stroke.
- **Ambient Shadows:** For floating modals or "elevated" states, use a shadow with a `40px` blur and `4%` opacity. The shadow color should be tinted with `#191c1e` (on-surface) to ensure it feels like a natural shadow on paper, not a digital drop-shadow.
- **The "Ghost Border" Fallback:** If a border is required for accessibility, use the `outline_variant` (#c2c6d4) at **15% opacity**. High-contrast, 100% opaque borders are strictly forbidden.

## 5. Components

### Buttons
- **Primary:** Gradient fill (`primary` to `primary_container`), `roundness: md` (0.375rem). No border.
- **Secondary:** `surface-container-high` fill with `on_surface` text. 
- **Tertiary:** Pure text with `primary` color. Use for low-emphasis actions like "Read More."

### Input Fields
- **Styling:** Use `surface-container-lowest` as the fill. Instead of a 4-sided border, use a 2px bottom-bar of `outline_variant` that transitions to `primary` on focus. This mimics the "underline" of a signature on a document.

### Cards & Lists
- **The Divider Rule:** Forbid the use of horizontal divider lines. Use the `spacing scale: 6` (2rem) to separate list items. Use a slight background shift (`surface-container-low`) on hover to define the interactive area.

### Technical Callouts (Special Component)
- **Manuscript Note:** A card using `tertiary_fixed` (#ffdbcc) background with a left-accent bar of `tertiary` (#722b00). Use this for "Pro-Tips" or "Key Takeaways" to break the blue/grey monotony.

## 6. Do’s and Don’ts

### Do:
- **Use "White Space" as a Tool:** If a section feels crowded, increase the margin using the `spacing: 16` or `20` tokens.
- **Optimize for Chinese Characters:** Ensure all Chinese text has a higher line-height than the English equivalent to prevent the "dense block" effect.
- **Align to a Soft Grid:** While layout is asymmetrical, elements should still snap to the `0.7rem` (token `2`) incremental grid.

### Don't:
- **Never use 100% Black:** Use `on_surface` (#191c1e) for text to maintain a sophisticated, ink-on-paper feel.
- **Avoid Sharp Corners:** Never use `roundness: none`. Even technical layouts benefit from the `sm` (0.125rem) or `md` (0.375rem) radii to soften the scholarly tone.
- **No Heavy Shadows:** If the shadow is clearly visible at a glance, it is too heavy. It should be felt, not seen.