# omniOne — Frontend

React SPA for the omniOne bodybuilding coaching platform.

## Stack

- **React 19** with functional components and hooks
- **React Router 7** for client-side routing
- **Vite 7** for dev server and bundling
- **STOMP.js** over WebSockets for real-time chat
- **Phosphor React** for icons

## Getting Started

```bash
npm install
npm run dev      # http://localhost:5173
npm run build    # production build
npm run preview  # preview production build locally
npm run lint     # run ESLint
```

## Project Structure

```
src/
├── pages/           # One file per route/screen
├── components/      # Shared UI components
├── assets/          # Static files (images, SVGs)
├── api.js           # All API calls — use this, never fetch() directly
├── auth.js          # Token storage and auth helpers
├── authContext.js   # React context for auth state
├── chatDockEvents.js
├── errorUtils.js    # Consistent error formatting
├── passwordUtils.js
├── profileUtils.js
├── App.jsx          # Router setup and route definitions
├── App.css          # Global styles
└── main.jsx         # Entry point
```

## Pages

| Page | Route | Role |
|---|---|---|
| Login | `/login` | Public |
| Register | `/register` | Public |
| ForgotPassword | `/forgot-password` | Public |
| ResetPassword | `/reset` | Public |
| ActivateAccount | `/activate` | Public |
| AcceptInvitation | `/invite` | Public |
| CompleteProfile | `/complete-profile` | Auth |
| Profile | `/profile` | Auth |
| CoachDashboard | `/coach/dashboard` | Coach |
| CoachClients | `/coach/clients` | Coach |
| CoachClientDetail | `/coach/clients/:id` | Coach |
| CoachClientNutritionPlans | `/coach/clients/:id/nutrition-plans` | Coach |
| CoachClientQuestionnaireResponses | `/coach/clients/:id/questionnaire-responses` | Coach |
| CoachQuestionnaire | `/coach/questionnaire` | Coach |
| ClientDashboard | `/client/dashboard` | Client |
| ClientCoach | `/client/coach` | Client |
| ClientNutritionPlans | `/client/nutrition-plans` | Client |
| ClientQuestionnaire | `/client/questionnaire` | Client |

## Conventions

- All API calls go through `src/api.js` — never use `fetch` directly in components
- Auth state comes from `authContext.js` — never duplicate auth logic
- Use `errorUtils.js` for all error display
- Icons: `phosphor-react` only
- No CSS frameworks — styles in `App.css` / `index.css`
- Keep pages lean; extract reusable logic into `utils/` or `components/`

## Environment

The dev server proxies API calls to the backend at `http://localhost:8080`. See `vite.config.js` for proxy configuration and `.env` for environment variables.
