# omniOne Frontend Design System

This document describes the visual design language of the omniOne frontend. Follow these rules when adding new pages, components, or CSS classes so the UI stays consistent.

---

## Typography

Fonts are loaded via Google Fonts and declared in `index.css`.

| Role | Font | Weight |
|---|---|---|
| UI / body | Space Grotesk | 400, 500, 600, 700 |
| Display headings | Libre Baskerville | 400, 700 |

- Base font-size: `16px` (browser default)
- Body color: `var(--color-slate-900)`
- Muted text: `var(--color-slate-500)`
- Secondary text: `var(--color-slate-600)`
- Small labels: `0.8rem`, `0.85rem`, `0.88rem`

---

## Color Palette

All colors are now defined as CSS custom properties in `:root` (see **CSS Variables** section below). Use `var(--color-*)` in CSS — never raw hex values.

| Token | Variable | Hex | Used for |
|---|---|---|---|
| `primary` | `--color-slate-900` | `#0f172a` | Text, buttons, nav, borders |
| `primary-hover` | `--color-slate-800` | `#1e293b` | Button hover states |
| `muted` | `--color-slate-500` | `#64748b` | Secondary text, placeholders |
| `border` | `--color-slate-200` | `#e2e8f0` | Card/input borders |
| `border-mid` | `--color-gray` | `#d1d5db` | Form input borders |
| `border-strong` | `--color-slate-300` | `#cbd5e1` | Editing state borders |
| `surface-light` | `--color-slate-50` | `#f8fafc` | Card backgrounds, subtle rows |
| `surface-nav` | `--color-slate-100` | `#f1f5f9` | Hover backgrounds |
| `white` | `--color-white` | `#ffffff` | Card bg, input bg |
| `blue-soft` | `--color-light-blue-100` | `#dbeafe` | Active/badge background |
| `blue-text` | `--color-blue-700` | `#1d4ed8` | Active/badge text |
| `blue-nav` | `--color-light-blue-3` | `#e0f2fe` | Active nav link bg |
| `blue-nav-text` | `--color-sky-900` | `#0c4a6e` | Active nav link text |
| `danger` | `--color-red-600` | `#dc2626` | Danger/destructive buttons |
| `error-text` | `--color-red-700` | `#b91c1c` | Error message text |
| `card-value` | `--color-slate-700` | `#334155` | Read-only field values |

### Background

The app shell uses a `#f6f4ef` base with two subtle radial gradients (orange/amber at top, sky-blue at 20%/20%). All surfaces layer on top of this.

---

## Spacing

| Name | Value | Common use |
|---|---|---|
| `xs` | `4–6px` | Inner badge padding, tight gaps |
| `sm` | `8–10px` | Chip padding, small gaps |
| `md` | `12–16px` | Card padding components, form gaps |
| `lg` | `18–20px` | Card padding, section spacing |
| `xl` | `24–28px` | Panel padding, app body gap |
| `2xl` | `32–48px` | Panel inner padding, page padding |

---

## Border Radius Scale

| Shape | Radius | Used on |
|---|---|---|
| Full pill | `999px` | Ghost buttons, user chip, back button, nav active, pills |
| Large panel | `24px` | `.panel` (page-level containers) |
| Card | `18px` | `.card`, `.side-nav` |
| Medium | `14–16px` | `.stat`, `.chat-thread`, history items |
| Form element | `12px` | Inputs, select, small buttons, list links |
| Small | `10px` | Error/success banners |
| Circle | `50%` | Icon-only circular buttons |

---

## Shadows & Elevation

| Level | Value | Used on |
|---|---|---|
| Panel | `0 18px 40px rgba(15,23,42,0.08)` | `.panel` |
| Chat dock | `0 24px 50px rgba(15,23,42,0.2)` | `.chat-dock-panel` |
| Card hover | `0 10px 24px rgba(15,23,42,0.08)` | `.client-card-link:hover` |

---

## Component Patterns

### Panel (page container)

Every route renders a single page panel. Panels have a white-ish glassy background, 24px radius, soft drop shadow, and a `fadeUp` entrance animation. **Use the `PagePanel` component** — it handles the wrapper, header, loading, and error states automatically.

```jsx
<PagePanel
  title="Page Title"
  subtitle="Subtitle text"
  loading={loading}
  error={error}
  headerRight={
    <Link className="back-button" to="/...">
      <span className="button-label">Back</span>
      <CaretRight size={22} weight="bold" />
    </Link>
  }
>
  {/* content */}
</PagePanel>
```

Pass `className="panel-narrow"` (440px max) or `className="panel-wide"` (720px max) for centered auth/form layouts.

---

### Card

A white rounded box inside a panel. Used for grouped information.

```jsx
<div className="card">
  <div className="card-title">Section Name</div>
  {/* content */}
</div>
```

Cards used as links get `.client-card-link` for hover elevation. The `.card-value` class styles read-only data (`#334155`, `0.95rem`).

---

### Split Grid

Two (or more) side-by-side cards. Uses CSS `auto-fit` so it collapses to single-column on narrow viewports.

```jsx
<div className="split-grid">
  <div className="card">…</div>
  <div className="card">…</div>
</div>
```

Add `.client-detail-plan-stack` alongside `.split-grid` to force a single-column stack regardless of viewport (used on plan management pages where forms are tall).

---

### Form

Forms use a CSS grid with 16px gaps. **Use `FormField` for fields, `Button` for submission, and `useFormState` for state management.** This is the standard pattern for all form pages.

```jsx
const form = useFormState()

async function handleSubmit(e) {
  e.preventDefault()
  form.startSaving()
  try {
    await apiCall(payload)
    form.setSuccess('Saved.')
  } catch (err) {
    form.setFailure(err)
  }
}

return (
  <form className="form" onSubmit={handleSubmit}>
    <StatusMessage status={form.status} error={form.error} />
    <FormField label="Field Name" error={form.fieldErrors?.fieldName}>
      <input type="text" value={…} onChange={…} />
    </FormField>
    <Button type="submit" loading={form.saving}>Submit</Button>
  </form>
)
```

Textareas inside `FormField` get the same styling as inputs (via `.field textarea`).

Submit buttons that contain icons use `.nutrition-plan-submit` to inline-flex align icon and label text.

---

### Buttons

**Use the `Button` component** for all standard buttons. It handles variant classes and loading/disabled state.

```jsx
<Button loading={form.saving}>Save</Button>                     // primary (dark bg)
<Button variant="ghost" onClick={handleEdit}>
  <PencilSimple size={22} weight="bold" /> Edit
</Button>                                                        // ghost (outline pill)
<Button variant="danger" onClick={handleDelete}>Delete</Button>  // danger (red)
```

**Variants:**
- **Primary** (`variant="primary"`, default): dark background `var(--color-slate-900)`, white text, 12px radius.
- **Ghost** (`variant="ghost"`): transparent fill, `1px solid` border, dark text, 999px pill radius. Used for secondary/toolbar actions.
- **Danger** (`variant="danger"`): red `var(--color-red-600)`, full-width by default. Used for destructive actions.

**Back button (`.back-button`):** dark pill, includes a caret icon. Always placed in the `panel-header` via `PagePanel`'s `headerRight` prop.

Icon-only circular buttons: add `padding: 0; width: 30px; height: 30px; border-radius: 50%; display: grid; place-items: center;` — see `.supplement-remove-button` as a pattern.

Buttons are disabled with the native `disabled` attribute or `Button`'s `loading` prop; CSS applies `opacity: 0.6; cursor: not-allowed`.

---

### Plan History List

The standard pattern for versioned records (nutrition plans, supplement plans):

```jsx
<ul className="card-list plan-history-list">
  {plans.map((plan, i) => (
    <li key={plan.id} className={`list-item plan-history-item${editing ? ' is-editing' : ''}`}>
      <div>
        <div className="plan-history-header">
          <div className="card-title">
            Title
            {i === 0 ? <span className="active-badge">Active</span> : null}
          </div>
          <div className="card-title plan-history-timestamp">
            {/* formatted timestamp */}
          </div>
        </div>
        {/* read-only view or inline edit form */}
      </div>
      <div className="plan-history-actions">
        {/* Edit / Delete or Save / Cancel buttons */}
      </div>
    </li>
  ))}
</ul>
```

The first item in the list (index 0) is always the **active** record and receives the `.active-badge` inline label.

The `.is-editing` modifier gives the item a subtle `#f8fafc` background with a stronger border, and switches it to a two-column grid so the form and action buttons sit side by side.

Save/confirm action buttons inside history items reuse `.plan-history-action-button`. The "Save" variant is `.plan-history-save-button` (red, to signal a commit action).

---

### Supplement Entry Row (form)

Used when building or editing a supplement plan. Each entry has Name, Brand, Dosage, Timing (2×2 grid) and Notes (full-width), with an optional remove button.

```jsx
<div className="supplement-entry-row">
  <div className="supplement-entry-fields">
    <label className="field"><span>Name *</span><input … /></label>
    <label className="field"><span>Brand</span><input … /></label>
    <label className="field"><span>Dosage *</span><input … /></label>
    <label className="field"><span>Timing</span><input … /></label>
    <label className="field supplement-entry-notes-field">
      <span>Notes</span>
      <textarea … />
    </label>
  </div>
  {/* optional remove button */}
  <button type="button" className="ghost-button supplement-remove-button">
    <X size={18} weight="bold" />
  </button>
</div>
```

The "Add supplement" ghost button uses `.supplement-add-entry-button` for proper inline-flex alignment.

---

### Supplement Entry List (read-only)

Used to display the contents of an active/historical supplement plan.

```jsx
<ul className="supplement-entry-list">
  {entries.map((entry, i) => (
    <li key={entry.id ?? i} className="supplement-entry-item">
      <div className="supplement-entry-name">
        {entry.supplementName}
        {entry.brand && <span className="supplement-entry-brand"> — {entry.brand}</span>}
      </div>
      <div className="supplement-entry-meta">
        <span className="card-value">{entry.dosage}</span>
        {entry.timing && <span className="card-value">{entry.timing}</span>}
      </div>
      {entry.notes && <div className="supplement-entry-notes card-value">{entry.notes}</div>}
    </li>
  ))}
</ul>
```

---

### Active Plan Cards

When showing the currently-active plan in a summary/dashboard card, use:

```jsx
<div className="card active-plan-card active-plan-card--featured">
  <div className="active-plan-header">
    <div className="card-title active-plan-title">Current Plan</div>
    <span className="active-plan-marker">Active</span>
  </div>
  {/* plan details */}
</div>
```

`.active-plan-card--featured` applies a warm gradient background (`sky-blue → orange`). Inside this variant the `.active-plan-marker` pill is hidden (the gradient itself signals "active").

---

### Status Messages

Always displayed inside the relevant card or form, never in a modal. **Use the `StatusMessage` component** — it handles `formatErrorMessage` internally.

```jsx
<StatusMessage status={form.status} error={form.error} />
```

`StatusMessage` accepts both string and error-object types for the `error` prop.

---

### Navigation

**Side nav (`.side-nav`):** sticky column on the left at ≥1200px; slides in as a drawer on smaller screens. Nav items use `NavLink` with `.nav-link` (active state: `#e0f2fe` background, slight translateX).

**Top bar (`.top-bar`):** blurred glass header with logo, nav toggle (mobile), and user chip.

---

## Responsive Breakpoints

| Breakpoint | Behavior |
|---|---|
| `≤1200px` | Side nav becomes a slide-in drawer; app-body grid collapses to 1 column |
| `≤1100px` | `.client-detail-grid` drops from 3 → 2 columns |
| `≤1000px` | User action labels hidden; buttons shrink to icon-only |
| `≤720px` | Single-column client-detail-grid; smaller app-body/panel padding |
| `≤650px` | Profile button collapses to icon-only |
| `≤600px` | Supplement entry fields collapse to 1 column |

---

## Animations

| Name | Description | Used on |
|---|---|---|
| `fadeUp` | Fade in + slide up 10px, 0.5s ease | All `.panel` on mount |
| `float` | Gentle vertical oscillation, 6s loop | `.hero-orb` decorative element |

---

## Icons

All icons come from `phosphor-react` (already installed). Preferred weights are `"bold"` for action icons and `"regular"` for decorative. Common sizes: `18`, `22`.

```jsx
import { Plus, Trash, PencilSimple, FloppyDisk, X, CaretRight } from 'phosphor-react'
```

Never use emoji or other icon libraries.

---

## Component Recipes

These are the exact class combinations needed for the most common multi-component patterns. Copy them directly rather than reconstructing from first principles.

---

### Recipe 1 — Plan management page (e.g. NutritionPlans, SupplementPlans)

A full coach page with a create form on one side and a scrollable history list on the other. Both cards stack in a single column regardless of viewport. Uses `useLoadData`, `useFormState`, and shared UI components.

```jsx
const form = useFormState()
const [plans, setPlans] = useState([])
const { loading, error } = useLoadData(async () => {
  const data = await getPlans(clientId)
  setPlans(data || [])
}, [clientId])

return (
  <PagePanel
    title="Plan name"
    subtitle="Submitting creates a new plan and makes it active."
    loading={loading}
    error={error}
    headerRight={<Link className="back-button" to="...">…</Link>}
  >
    <div className="split-grid client-detail-plan-stack">

      {/* Left / top card — create form */}
      <div className="card">
        <div className="card-title">New plan</div>
        <form className="form" onSubmit={handleSubmit}>
          <StatusMessage status={form.status} error={form.error} />
          <FormField label="Field" error={form.fieldErrors?.field}>
            <input ... />
          </FormField>
          <Button type="submit" loading={form.saving}>Create plan</Button>
        </form>
      </div>

      {/* Right / bottom card — history */}
      <div className="card">
        <div className="card-title">History</div>
        <ul className="card-list plan-history-list">
          {plans.map((plan, i) => (
            <li key={plan.id} className="list-item plan-history-item">
              <div>
                <div className="plan-history-header">
                  <div className="card-title">
                    Title {i === 0 && <span className="active-badge">Active</span>}
                  </div>
                  <span className="muted plan-history-timestamp">{date}</span>
                </div>
                {/* read-only values */}
              </div>
              <div className="plan-history-actions">
                <Button variant="ghost" className="plan-history-action-button">Edit</Button>
                <Button variant="danger" className="plan-history-action-button">Delete</Button>
              </div>
            </li>
          ))}
        </ul>
      </div>

    </div>
  </PagePanel>
)
```

**CSS sections involved:** Content & Panels · Split Grid, Plans & Nutrition · Chips, Pills & List Items

---

### Recipe 2 — Inline edit row inside plan history

When the user clicks "Edit" on a history item, add `is-editing` to the `<li>` and switch the content to an edit form.

```jsx
<li className={`list-item plan-history-item${editing ? ' is-editing' : ''}`}>
  <div>
    {editing ? (
      <div className="plan-history-values-editing">
        {/* compact inline inputs using .plan-history-inline-field */}
        <label className="field plan-history-inline-field"><span>Label</span><input … /></label>
      </div>
    ) : (
      <div className="plan-history-values">
        {/* read-only spans */}
      </div>
    )}
  </div>
  <div className="plan-history-actions">
    {editing ? (
      <>
        <button className="plan-history-action-button plan-history-save-button" onClick={save}>Save</button>
        <button className="ghost-button plan-history-action-button" onClick={cancel}>Cancel</button>
      </>
    ) : (
      <button className="ghost-button plan-history-action-button" onClick={startEdit}>Edit</button>
    )}
  </div>
</li>
```

**CSS sections involved:** Split Grid, Plans & Nutrition

---

### Recipe 3 — Active plan summary card (dashboard / client detail)

Used in `CoachClientDetail` to preview the current active plan for a feature.

```jsx
<div className="card active-plan-card active-plan-card--featured">
  <div className="active-plan-header">
    <div className="card-title active-plan-title">Current Plan</div>
    {/* active-plan-marker is hidden inside --featured; shown in non-featured cards */}
    <span className="active-plan-marker">Active</span>
  </div>
  <div className="plan-grid">
    <div><div className="label">Calories</div><div className="value">2400</div></div>
    <div><div className="label">Protein</div><div className="value">180 g</div></div>
    {/* … */}
  </div>
</div>
```

To show the marker pill (not hidden), drop `active-plan-card--featured` and keep only `card active-plan-card`.

**CSS sections involved:** Split Grid, Plans & Nutrition · Stats & Cards

---

### Recipe 4 — Client detail overview grid

The 3-up card grid on `CoachClientDetail`. Collapses to 2-col at 1100px and 1-col at 720px automatically.

```jsx
<div className="client-detail-grid">
  <Link className="card client-card-link" to="...">
    <div className="card-title">Section title</div>
    <p className="card-value">Preview text or data</p>
  </Link>
  {/* repeat */}
</div>
```

For a card that should look like a danger zone (remove client, etc.) add `.client-detail-danger-zone` alongside `.card`.

**CSS sections involved:** Client Lists & Detail

---

### Recipe 5 — Stat row inside a card

Small labeled metrics displayed in a wrapping grid.

```jsx
<div className="stat-grid">
  <div className="stat">
    <div className="label">Weight</div>
    <div className="value">82 kg</div>
  </div>
  <div className="stat">
    <div className="label">Body fat</div>
    <div className="value">14 %</div>
  </div>
</div>
```

**CSS sections involved:** Stats & Cards

---

### Recipe 6 — Page with no sidebar (auth / onboarding)

Auth pages render inside `.app-body.solo`. Use `PagePanel` with `className="panel-narrow"` or `"panel-wide"`.

```jsx
// Auth pages don't use the app shell — they render directly inside a solo layout.
// The router handles the solo wrapper. The page itself just returns:
<PagePanel title="Sign in" className="panel-narrow">
  <form className="form" onSubmit={handleSubmit}>
    <StatusMessage status={form.status} error={form.error} />
    <FormField error={form.fieldErrors?.email}>
      <input type="email" placeholder="Email" ... />
    </FormField>
    <Button type="submit" loading={form.saving}>Sign in</Button>
  </form>
</PagePanel>
```

**CSS sections involved:** App Body · Content Area & Panels

---

## CSS Conventions

- No CSS frameworks — all styles live in `App.css` (components) and `index.css` (base/reset/fonts).
- Class names use kebab-case. Component-scoped classes are prefixed with the component name (e.g. `supplement-entry-*`, `plan-history-*`, `nutrition-plan-*`).
- New classes go at the bottom of `App.css`, grouped under a `/* ── Section name ── */` comment.
- Always follow existing border-radius, color, and spacing tokens — do not introduce new raw values without updating this document.
- Media queries for new components go immediately after their related rules (not in a single global media block).

---

## CSS Variables

All colors are defined as CSS custom properties in `:root` at the top of `App.css`. Never use raw hex values — always reference these variables.

| Variable | Hex | Maps to |
|---|---|---|
| `--color-slate-900` | `#0f172a` | Primary text, buttons, nav |
| `--color-slate-800` | `#1e293b` | Button hover states |
| `--color-slate-700` | `#334155` | Card values, secondary dark |
| `--color-slate-600` | `#475569` | Secondary text |
| `--color-slate-500` | `#64748b` | Muted text, placeholders |
| `--color-slate-400` | `#94a3b8` | Subtle icons/borders |
| `--color-slate-300` | `#cbd5e1` | Editing-state borders |
| `--color-slate-200` | `#e2e8f0` | Card/input borders |
| `--color-slate-100` | `#f1f5f9` | Hover backgrounds |
| `--color-slate-50` | `#f8fafc` | Light surface bg |
| `--color-sky-900` | `#0c4a6e` | Active nav text |
| `--color-sky-800` | `#075985` | Deep sky accent |
| `--color-sky-500` | `#0ea5e9` | Sky accent |
| `--color-blue-700` | `#1d4ed8` | Badge/active text |
| `--color-blue-600` | `#2563eb` | Link blue |
| `--color-red-900` | `#7f1d1d` | Deep error |
| `--color-red-700` | `#b91c1c` | Error text |
| `--color-red-600` | `#dc2626` | Danger buttons |
| `--color-red-500` | `#ef4444` | Lighter red accent |
| `--color-green-500` | `#22c55e` | Success accent |
| `--color-amber-500` | `#f59e0b` | Warning/amber accent |
| `--color-white` | `#ffffff` | Card/input bg |
| `--color-light-blue-100` | `#dbeafe` | Badge background |
| `--color-light-blue-3` | `#e0f2fe` | Active nav bg |
| `--color-gray` | `#d1d5db` | Form input borders |

When adding new colors, add a variable to `:root` first, then reference it.

---

## Shared Hooks

### `useFormState` (`hooks/useFormState.js`)

Eliminates repeated `status` / `error` / `fieldErrors` / `saving` boilerplate. Every page with a form should use this instead of declaring those four states manually.

```jsx
import { useFormState } from '../hooks/useFormState'

const form = useFormState()

async function handleSubmit(e) {
  e.preventDefault()
  form.startSaving()
  try {
    await apiCall(payload)
    form.setSuccess('Saved.')
  } catch (err) {
    form.setFailure(err)   // auto-extracts field errors
  }
}

// In JSX:
<StatusMessage status={form.status} error={form.error} />
<FormField label="Name" error={form.fieldErrors?.name}>
  <input ... />
</FormField>
<Button loading={form.saving}>Save</Button>
```

**API:** `startSaving()`, `setSuccess(msg)`, `setFailure(err)`, `setErrorManual(msg)`, `reset()`, plus direct setters (`setStatus`, `setError`, `setFieldErrors`).

### `useLoadData` (`hooks/useLoadData.js`)

Eliminates the mounted-flag + loading + error pattern for data fetching on mount or when dependencies change.

```jsx
import { useLoadData } from '../hooks/useLoadData'

const [clients, setClients] = useState([])
const { loading, error } = useLoadData(async () => {
  const data = await getClients()
  setClients(data || [])
}, [])

// With dependencies (re-fetches when clientId changes):
const { loading, error } = useLoadData(async () => {
  const [client, plans] = await Promise.all([getClient(clientId), getPlans(clientId)])
  setClient(client)
  setPlans(plans || [])
}, [clientId])
```

---

## Shared UI Components

All exported from `components/index.js` as named exports. Import via:

```jsx
import { FormField, Button, StatusMessage, PagePanel } from '../components'
```

Or import individually:

```jsx
import PagePanel from '../components/PagePanel'
```

### `FormField`

Wraps a label + input + optional field-level error. Renders the existing `.field` CSS class.

```jsx
<FormField label="Email" error={form.fieldErrors?.email}>
  <input type="email" value={email} onChange={...} required />
</FormField>

// Without label:
<FormField error={form.fieldErrors?.email}>
  <input type="email" placeholder="Email" ... />
</FormField>
```

**Props:** `label` (string), `error` (string), `className` (string), `children`.

### `Button`

Wraps CSS button classes with a consistent API. Handles loading/disabled state automatically.

```jsx
<Button loading={form.saving}>Save</Button>
<Button variant="ghost" onClick={handleCancel}>Cancel</Button>
<Button variant="danger" onClick={handleDelete}>Delete</Button>
```

**Props:** `variant` (`'primary'` | `'ghost'` | `'danger'`), `loading` (bool), `loadingText` (string, default `'Saving...'`), `className`, plus all native button attributes.

### `StatusMessage`

Renders success and/or error messages. Uses `formatErrorMessage` internally.

```jsx
<StatusMessage status={form.status} error={form.error} />
```

**Props:** `status` (string), `error` (string or error object).

### `PagePanel`

Standard page wrapper with title, subtitle, loading/error/status states. Renders the `.panel` class.

```jsx
<PagePanel
  title="Your clients"
  subtitle="Invite new clients and manage active coaching."
  loading={loading}
  error={error}
  headerRight={<Link className="back-button" to="/coach/clients">Back <CaretRight /></Link>}
  className="panel-narrow"
>
  {/* page content — only rendered when not loading */}
</PagePanel>
```

**Props:** `title`, `subtitle`, `loading`, `error`, `status`, `loadingText` (default `'Loading...'`), `headerRight` (JSX for back button or actions), `headerClassName`, `className` (e.g. `'panel-narrow'`, `'panel-wide'`), `children`.

---

## ChatDock Architecture

`ChatDock.jsx` is the real-time messaging dock. It was split into focused modules under `components/chat/`:

| File | Purpose |
|---|---|
| `ChatDock.jsx` | Orchestrator — composes hooks and subcomponents |
| `chat/useChatWebSocket.js` | STOMP WebSocket connection, subscriptions, message send/receive, unread tracking |
| `chat/useChatDockLayout.js` | Dock drag, resize, divider resize, panel width observation, localStorage persistence |
| `chat/ChatConversationList.jsx` | Conversation list sidebar, dock actions, list toggle |
| `chat/ChatMessageThread.jsx` | Message thread display + input form |
| `chat/chatUtils.js` | Pure utilities: `buildWebSocketUrl`, `formatChatTimestamp`, `formatMessageTime`, `formatMessageDay` |

When modifying chat behavior, edit the relevant module rather than the orchestrator. New chat features should follow this separation (e.g. typing indicators → `useChatWebSocket.js`, message search → new `ChatMessageSearch.jsx`).

---

## Reference Page

`CoachQuestionnaire.jsx` is the designated **reference page**. New pages should follow its structure:

- `useLoadData` for initial data fetching
- `useFormState` for form submission state
- `FormField` / `Button` / `StatusMessage` for UI
- `PagePanel` as the page wrapper

The file contains a `// REFERENCE PAGE` comment at the top with this guidance.
