# Foldable Feature PRs

PRs to merge when creating a fold-optimized build from `yellow-accent`.

## Ready to merge

| PR | Feature | Status | Lines |
|----|---------|--------|-------|
| [#21](https://github.com/yncyrydybyl/element-x-android/pull/21) | Tabletop media viewer (info + actions in bottom half) | Tested on Pixel 10 Pro Fold | +218 |
| [#22](https://github.com/yncyrydybyl/element-x-android/pull/22) | Dual-pane thread book (timeline + thread placeholder) | Tested on Pixel 10 Pro Fold | +96 |

## Scaffolds (UI built, needs wiring)

| PR | Feature | Status | Lines |
|----|---------|--------|-------|
| [#11](https://github.com/yncyrydybyl/element-x-android/pull/11) | Cover screen walkie-talkie | UI + presenter | +343 |
| [#13](https://github.com/yncyrydybyl/element-x-android/pull/13) | Tabletop video call layout | UI scaffold | +40 |
| [#14](https://github.com/yncyrydybyl/element-x-android/pull/14) | Dual-screen translator | UI scaffold | +548 |
| [#15](https://github.com/yncyrydybyl/element-x-android/pull/15) | Flex voice studio | UI + strings | +598 |
| [#16](https://github.com/yncyrydybyl/element-x-android/pull/16) | Photo director mode | UI scaffold | +388 |
| [#17](https://github.com/yncyrydybyl/element-x-android/pull/17) | Ambient presence display | UI scaffold | +387 |
| ~~#18~~ | ~~Thread book dual-pane~~ | Replaced by #22 | — |
| [#19](https://github.com/yncyrydybyl/element-x-android/pull/19) | Campfire dual-perspective | UI scaffold | +337 |
| [#20](https://github.com/yncyrydybyl/element-x-android/pull/20) | Rear camera selfie share | UI scaffold | +275 |

## How to build a fold fork

```bash
git checkout yellow-accent
git checkout -b fold-release

# Merge ready PRs
gh pr list --repo yncyrydybyl/element-x-android --json number,headRefName \
  | jq -r '.[] | .headRefName' \
  | while read branch; do git merge fork/$branch --no-edit; done

# Build
./scripts/build-android.sh :app:assembleDebug
```
