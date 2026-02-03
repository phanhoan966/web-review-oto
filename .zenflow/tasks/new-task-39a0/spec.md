# Technical Specification

## Technical Context
- Frontend uses Vue 3 + TypeScript with Vite. Feed page (`FeedView.vue`) renders posts via `ReviewCard.vue`.
- `buildAssetUrl` returns an empty string when no path is provided; the card currently renders an `<img>` with an empty `src`, leading to broken image icons when a post lacks a thumbnail.

## Implementation Approach (complexity: easy)
- Add a guard so `ReviewCard` only renders the hero image block when a valid `heroImageUrl` exists after URL building, preventing `<img>` with an empty `src`.
- Optionally render a styled placeholder or adjust spacing when the image is absent to maintain layout consistency without showing a broken image icon.
- Reuse existing utilities (`buildAssetUrl`, `slugify`) and component structure.

## Source Code Changes
- `frontend/src/public/components/ReviewCard.vue`: add a computed flag for presence of a hero image, conditionally render the hero `<img>` or a placeholder/no-image state, and adjust styling if needed to keep the card layout consistent when the thumbnail is missing.

## Data Model / API / Interface Changes
- None. Thumbnail remains optional; no backend or type changes required beyond handling absence gracefully in the UI.

## Verification
- Run `npm run build` in `frontend` to ensure TypeScript and Vite build succeed.
- Manual check: open the main feed and view a post without a `heroImageUrl`; confirm no broken image/error icon is shown and other cards with thumbnails remain unaffected.
