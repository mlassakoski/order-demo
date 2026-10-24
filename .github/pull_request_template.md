<!-- Summary, evidence and merge-danger: let the agent fill these with the /pr skill. -->

## Spec / ticket
`ORD-NN` (jira/ORD-board.md) · spec `docs/specs/ORD-NN-<slug>.md`

## AI first-pass acceptance
How much of the AI output did you keep? (feeds our first-pass acceptance metric)
- [ ] `ai-first-pass`: accepted as generated (formatting at most)
- [ ] `ai-small-fixes`: small manual fixes (< 15 min)
- [ ] `ai-reworked`: significant rework or rewrite
- [ ] `no-ai`: written by hand

If not first-pass, what was missing? (glossary term, ticket detail, coding standard, check...) → feed it to `/retro`.

## Checks
- [ ] `./mvnw verify` green (CI)
- [ ] `/code-review` Standards and Spec: no open findings
