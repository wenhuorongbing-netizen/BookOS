export type NavigationMode = 'READER' | 'NOTE_TAKER' | 'GAME_DESIGNER' | 'RESEARCHER' | 'COMMUNITY' | 'ADVANCED'

export const navigationModes: Array<{ value: NavigationMode; label: string }> = [
  { value: 'READER', label: 'Reader Mode' },
  { value: 'NOTE_TAKER', label: 'Note-Taker Mode' },
  { value: 'GAME_DESIGNER', label: 'Game Designer Mode' },
  { value: 'RESEARCHER', label: 'Researcher Mode' },
  { value: 'COMMUNITY', label: 'Community Mode' },
  { value: 'ADVANCED', label: 'Advanced Mode' },
]

export function normalizeNavigationMode(...values: Array<string | null | undefined>): NavigationMode {
  const knownModes = new Set(navigationModes.map((mode) => mode.value))
  const match = values.find((value): value is NavigationMode => Boolean(value && knownModes.has(value as NavigationMode)))
  return match ?? 'READER'
}

export function navigationModeLabel(mode: NavigationMode) {
  return navigationModes.find((item) => item.value === mode)?.label ?? 'Reader Mode'
}
